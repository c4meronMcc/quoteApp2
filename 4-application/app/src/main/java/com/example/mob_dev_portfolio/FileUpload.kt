package com.example.mob_dev_portfolio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mob_dev_portfolio.QuoteExtractor.extractQuoteDataAsJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun QuoteCard(quote: JSONObject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quote #${quote.optString("quoteNumber", "-")}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Date: ${quote.optString("date", "-")}")
            Text(text = "Total: ${quote.optString("totalAmount", "-")}")
        }
    }
}

@Composable
fun FilePicker(onFileSelected: (Uri) -> Unit) {

    val context = LocalContext.current
    var selectedFileName by remember { mutableStateOf("No File Selected Yet") }
    val scope = rememberCoroutineScope()
    val quoteArray = remember { JSONArray() }

    var quoteList by remember { mutableStateOf<List<JSONObject>>(emptyList()) }

    val db = remember { QuoteDatabase.getDatabase(context) }
    val dao = remember { db.quoteDao() }

    var savedProfileSummary by remember { mutableStateOf<List<QuoteProfileSummary>>(emptyList()) }
    var selectedProfileQuotes by remember { mutableStateOf<List<JSONObject>?>(emptyList()) }
    var selectedProfileName by remember { mutableStateOf("Untitled") }

    LaunchedEffect(Unit) {
        savedProfileSummary = withContext(Dispatchers.IO) { dao.getAllProfiles() }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->

        scope.launch(Dispatchers.IO) {
            repeat(quoteArray.length()) { quoteArray.remove(0) }

            for (file in uris ?: return@launch) {
                selectedFileName = getFileNameFromUri(context, file)
                onFileSelected(file)
                quoteArray.put(extractQuoteDataAsJson(convertPdfToTxt(context, file)))
            }

            println("MASTER JSON ARRAY:\n${quoteArray.toString(4)}")

            dao.insertQuote(
                QuoteEntity(
                    profileName = selectedProfileName,
                    quoteList = quoteArray.toString(),
                    createdAt = System.currentTimeMillis().toString()
                )
            )

            val updatedProfiles = dao.getAllProfiles()
            withContext(Dispatchers.Main) {
                quoteList = List(quoteArray.length()) { quoteArray.getJSONObject(it) }
                savedProfileSummary = updatedProfiles
            }


        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                Text("Select File from Device")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = selectedFileName,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (quoteArray.length() == 0) {
                        Toast.makeText(context, "No quotes loaded yet", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch(Dispatchers.IO) {
                        generatePdfFromQuoteArray(context, quoteArray)
                    }
                }
            ) {
                Text("Export to PDF")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (quoteList.isNotEmpty()) {
                Text("%{quoteList.size} quote(s) imported", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth() .padding(16.dp)
                ) {
                    items(quoteList) { quote ->
                        QuoteCard(quote)
                    }
                }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Saved Profiles", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (savedProfileSummary.isEmpty()) {
                Text(
                    text = "No saved profiles yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(savedProfileSummary) { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch(Dispatchers.IO) {
                                        val entity = dao.getQuoteById(profile.id)
                                        val quotes = JSONArray(entity.quoteList)
                                        val quoteObjects = List(quotes.length()) { quotes.getJSONObject(it) }
                                        withContext(Dispatchers.Main) {
                                            selectedProfileName = profile.profileName
                                            selectedProfileQuotes = quoteObjects
                                        }
                                    }
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(profile.profileName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Created: ${profile.createdAt}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            selectedProfileQuotes?.let { quotes ->
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Quotes in \"$selectedProfileName\"", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quotes) { quote -> QuoteCard(quote) }
                }
            }
        }
    }
}






private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = cursor.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) result = result?.substring(cut + 1)
    }
    return result ?: "Unknown File"
}