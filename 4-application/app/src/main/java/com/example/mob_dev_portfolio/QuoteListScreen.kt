package com.example.mob_dev_portfolio

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun QuoteListScreen(
    profileId: Int,
    viewModel: QuoteViewModel,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()
    val quoteList    = viewModel.quoteList
    val profileName  = viewModel.profileName
    val isLoading = viewModel.isLoading

    var editingQuoteIndex by remember { mutableIntStateOf(-1) }
    var selectedFilter by remember { mutableStateOf("Overall") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var quoteToDelete by remember { mutableStateOf<JSONObject?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val deleteTargetId by remember { mutableIntStateOf(-1) }

    val recommendedQuote by remember(quoteList, selectedFilter) {
        derivedStateOf {
            if (quoteList.isEmpty()) null
            else when (selectedFilter) {
                "Cheapest"    -> getCheapestQuote(quoteList)
                "Most Recent" -> getMostRecentQuote(quoteList)
                "Overall"     -> getBestOverallQuote(quoteList)
                else          -> null
            }
        }
    }

    if (quoteToDelete != null) {
        AlertDialog(
            onDismissRequest = { quoteToDelete = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = Paper,
            title = {
                Text(
                    "Delete Quote?",
                    fontFamily = DisplayFont,
                    fontSize = 20.sp,
                    color = Ink
                )
            },
            text = { Text("This action cannot be undone.", fontFamily = BodyFont) },
            confirmButton = {
                TextButton(onClick = {
                    val updated = quoteList.filter { it != quoteToDelete }
                    viewModel.updateQuotes(updated)
                    quoteToDelete = null
                }) {
                    Text(
                        "Delete",
                        color = QuoteRed,
                        fontFamily = BodyFont,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { quoteToDelete = null }) {
                    Text("Cancel", color = Muted, fontFamily = BodyFont)
                }
            }
        )
    }

    val sortedQuotes by remember(quoteList) {
        derivedStateOf {
            quoteList.sortedBy { parseAmount(it.optString("totalAmount", "0")) }
        }
    }

    val bestAmount by remember(quoteList) {
        derivedStateOf {
            if (quoteList.isEmpty()) "—"
            else getCheapestQuote(quoteList)?.optString("totalAmount", "—") ?: "—"
        }
    }
    val priceRange by remember(quoteList) {
        derivedStateOf {
            if (quoteList.size < 2) "—"
            else {
                val amounts = quoteList.map { parseAmount(it.optString("totalAmount", "0")) }
                    .filter { it != Double.MAX_VALUE }
                if (amounts.size < 2) "—"
                else {
                    val diff = amounts.max() - amounts.min()
                    "£${diff.toLong()}"
                }
            }
        }
    }

    // Load profile
    LaunchedEffect(profileId) {
        viewModel.loadQuoteProfile(profileId)
    }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            shape            = RoundedCornerShape(16.dp),
            containerColor   = Paper,
            title = {
                Text(
                    "Rename Profile",
                    fontFamily = DisplayFont,
                    fontSize   = 20.sp,
                    color      = Ink
                )
            },
            text = {
                OutlinedTextField(
                    value          = renameText,
                    onValueChange  = { renameText = it },
                    label          = { Text("Profile Name", fontFamily = BodyFont) },
                    singleLine     = true,
                    shape          = RoundedCornerShape(10.dp),
                    colors         = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Amber,
                        focusedLabelColor  = Amber,
                        cursorColor        = Amber
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        viewModel.renameProfile(renameText)
                        showRenameDialog = false
                    }
                }) {
                    Text("Save", color = Amber, fontFamily = BodyFont, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Muted, fontFamily = BodyFont)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Paper,
            title = { Text("Delete Profile?", fontFamily = DisplayFont, fontSize = 20.sp, color = Ink) },
            text = { Text("Are you sure you want to permanently delete this entire profile and all its quotes?", fontFamily = BodyFont) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProfile(deleteTargetId)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = QuoteRed, fontFamily = BodyFont, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Muted, fontFamily = BodyFont)
                }
            }
        )
    }

    // Edit dialog
    if (editingQuoteIndex != -1) {
        EditQuoteDialog(
            quote     = quoteList[editingQuoteIndex],
            onDismiss = { editingQuoteIndex = -1 },
            onSave    = { updatedQuote ->
                scope.launch(Dispatchers.IO) {
                    val updatedList        = quoteList.toMutableList()
                    updatedList[editingQuoteIndex] = updatedQuote
                    viewModel.updateQuotes(updatedList)
                    withContext(Dispatchers.Main) { editingQuoteIndex = -1 }
                }
            }
        )
    }



    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importPdfs(context, uris)
        }
    }

    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val exportArray = JSONArray()
                viewModel.quoteList.forEach { exportArray.put(it) }

                generatePdfFromQuoteArray(
                    context = context,
                    quoteArray = exportArray,
                    uri = uri,
                    recommendation = recommendedQuote,
                    recommendationType = selectedFilter
                )
            }
        }
    }

    //  Root
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {

        // Dark header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)
        ) {
            // Nav row
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            viewModel.clearCurrentProfile()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint     = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Profile name
                Text(
                    text       = profileName,
                    fontFamily = DisplayFont,
                    fontSize   = 20.sp,
                    color      = Color.White,
                    letterSpacing = (-0.3).sp,
                    modifier   = Modifier.weight(1f)
                )

                // Rename button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            renameText       = profileName
                            showRenameDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint     = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Stats strip
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox(label = "QUOTES", value = quoteList.size.toString(), modifier = Modifier.weight(1f))
                StatBox(label = "RANGE",  value = priceRange,  highlight = true, modifier = Modifier.weight(1f))
                StatBox(label = "BEST",   value = bestAmount,  highlight = true, modifier = Modifier.weight(1f))
            }
        }

        //  Filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Overall", "Cheapest", "Most Recent").forEach { filter ->
                FilterPill(
                    label    = filter,
                    selected = selectedFilter == filter,
                    onClick  = { selectedFilter = filter }
                )
            }
        }

        //  Scrollable content
        LazyColumn(
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (quoteList.isEmpty()) {
                item {
                    Box(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        contentAlignment  = Alignment.Center
                    ) {
                        Text(
                            "No quotes yet — tap Import PDF below",
                            fontFamily = BodyFont,
                            fontSize   = 14.sp,
                            color      = Muted
                        )
                    }
                }
            } else {
                // Recommendation card
                recommendedQuote?.let { rec ->
                    item {
                        Column(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        ) {
                            SectionTag("TOP RECOMMENDATION")
                            Spacer(Modifier.height(10.dp))
                            RecommendationCard(
                                quote       = rec,
                                onEdit      = { editingQuoteIndex = quoteList.indexOf(rec) }
                            )
                            Spacer(Modifier.height(20.dp))
                            SectionTag("ALL QUOTES")
                            Spacer(Modifier.height(4.dp))
                        }

                    }
                }

                itemsIndexed(sortedQuotes) { index, quote ->
                    QuoteRow(
                        rank     = index + 1,
                        quote    = quote,
                        onEdit   = { editingQuoteIndex = quoteList.indexOf(quote) },
                        onDelete = {
                            val updated = quoteList.filter { it != quote }
                            viewModel.updateQuotes(updated)
                        }

                    )
                }
            }

        }

        //  Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Paper)
                .border(
                    width = 1.dp,
                    color = Border,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Import PDF
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Amber)
                    .clickable { filePickerLauncher.launch(arrayOf("application/pdf")) },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Import PDF",
                    fontFamily    = BodyFont,
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 13.sp,
                    color         = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            // Export
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Cream)
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .clickable {
                        val safeProfileName = profileName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                        pdfSaveLauncher.launch("${safeProfileName}_Quotes.pdf")
                    },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint     = Ink,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Export",
                    fontFamily    = BodyFont,
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 13.sp,
                    color         = Ink,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Amber)
        }
    }
}

// Stat box (inside dark header)
@Composable
private fun StatBox(
    label: String,
    value: String,
    highlight: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text          = label,
            fontFamily    = BodyFont,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color         = Color.White.copy(alpha = 0.35f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = value,
            fontFamily = DisplayFont,
            fontSize   = 17.sp,
            color      = if (highlight) AmberLight else Color.White,
            letterSpacing = (-0.3).sp
        )
    }
}

// Filter pill
@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Ink else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (selected) Ink else Border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(
            text          = label.uppercase(),
            fontFamily    = BodyFont,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color         = if (selected) AmberLight else Muted
        )
    }
}

// Section tag
@Composable
private fun SectionTag(text: String) {
    Text(
        text          = text,
        fontFamily    = BodyFont,
        fontSize      = 10.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
        color         = Muted
    )
}

// Recommendation card
@Composable
private fun RecommendationCard(quote: JSONObject, onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GreenDeep)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text(
                text          = "REF · ${quote.optString("quoteNumber", "N/A")}",
                fontFamily    = BodyFont,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color         = Color.White.copy(alpha = 0.45f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = quote.optString("totalAmount", "—"),
                fontFamily = DisplayFont,
                fontSize   = 32.sp,
                color      = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text       = "📅 ${quote.optString("date", "—")}",
                    fontFamily = BodyFont,
                    fontSize   = 11.sp,
                    color      = Color.White.copy(alpha = 0.55f)
                )
            }
        }

        // Best pick badge + edit button
        Column(
            modifier          = Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text          = "✦ BEST PICK",
                fontFamily    = BodyFont,
                fontSize      = 9.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color         = GreenLight.copy(alpha = 0.8f)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint     = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// Quote list row
@Composable
private fun QuoteRow(
    rank: Int,
    quote: JSONObject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank number
            Text(
                text       = rank.toString(),
                fontFamily = DisplayFont,
                fontSize   = 20.sp,
                color      = Border,
                modifier   = Modifier.width(24.dp)
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                // Show the extracted supplier name!
                Text(
                    text       = quote.optString("supplier", "Unknown Supplier"),
                    fontFamily = BodyFont,
                    fontWeight = FontWeight.Bold, // Make it stand out
                    fontSize   = 15.sp,
                    color      = Ink
                )
                Text(
                    text       = "REF: " + quote.optString("quoteNumber", "N/A"),
                    fontFamily = BodyFont,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 12.sp,
                    color      = Muted
                )
                Text(
                    text       = quote.optString("date", "—"),
                    fontFamily = BodyFont,
                    fontSize   = 11.sp,
                    color      = Muted
                )
            }

            // Amount
            Text(
                text       = quote.optString("totalAmount", "—"),
                fontFamily = DisplayFont,
                fontSize   = 18.sp,
                color      = Ink,
                letterSpacing = (-0.3).sp
            )

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Cream)
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint     = Muted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(QuoteRed.copy(alpha = 0.1f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontSize = 12.sp, color = QuoteRed)
                }
            }
        }
        HorizontalDivider(color = Border, thickness = 0.5.dp)
    }
}

// Edit dialog
@Composable
fun EditQuoteDialog(
    quote: JSONObject,
    onDismiss: () -> Unit,
    onSave: (JSONObject) -> Unit
) {
    var supplier by remember { mutableStateOf(quote.optString("supplier", "Unknown Supplier")) }
    var quoteNumber by remember { mutableStateOf(quote.optString("quoteNumber", "")) }
    var date by remember { mutableStateOf(quote.optString("date", "")) }
    var totalAmount by remember { mutableStateOf(quote.optString("totalAmount", "")) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Amber,
        focusedLabelColor  = Amber,
        cursorColor        = Amber
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(16.dp),
        containerColor   = Paper,
        title = {
            Text(
                "Edit Quote",
                fontFamily = DisplayFont,
                fontSize   = 20.sp,
                color      = Ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = supplier,
                    onValueChange = { supplier = it },
                    label         = { Text("Supplier", fontFamily = BodyFont) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = fieldColors
                )
                OutlinedTextField(
                    value         = quoteNumber,
                    onValueChange = { quoteNumber = it },
                    label         = { Text("Reference Number", fontFamily = BodyFont) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = fieldColors
                )
                OutlinedTextField(
                    value         = totalAmount,
                    onValueChange = { totalAmount = it },
                    label         = { Text("Total Amount", fontFamily = BodyFont) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = fieldColors
                )
                OutlinedTextField(
                    value         = date,
                    onValueChange = { date = it },
                    label         = { Text("Date", fontFamily = BodyFont) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = fieldColors
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    JSONObject().apply {
                        put("supplier", supplier)
                        put("quoteNumber", quoteNumber)
                        put("totalAmount", totalAmount)
                        put("date", date)
                    }
                )
            }) {
                Text("Save", color = Amber, fontFamily = BodyFont, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Muted, fontFamily = BodyFont)
            }
        }
    )
}
