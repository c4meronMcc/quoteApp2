package com.example.mob_dev_portfolio

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mob_dev_portfolio.QuoteExtractor.extractQuoteDataAsJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuoteViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = QuoteDatabase.getDatabase(application).quoteDao()


    var savedProfiles by mutableStateOf<List<QuoteProfileSummary>>(emptyList())
        private set

    var quoteList by mutableStateOf<List<JSONObject>>(emptyList())
        private set

    var profileName by mutableStateOf("Untitled")
        private set

    private var currentProfileId by mutableIntStateOf(-1)

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadProfiles()
    }

    private suspend fun persistQuotes(newQuotes: List<JSONObject>) {
        val updatedArray = JSONArray()
        newQuotes.forEach { updatedArray.put(it) }

        if (currentProfileId == -1) {
            val newId = dao.insertQuote(
                QuoteEntity(
                    profileName = "Untitled",
                    quoteList = updatedArray.toString(),
                    createdAt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                )
            )
            currentProfileId = newId.toInt()
        } else {
            val existingEntity = dao.getQuoteById(currentProfileId)
            dao.updateQuote(existingEntity.copy(quoteList = updatedArray.toString()))
        }

        withContext(Dispatchers.Main) { quoteList = newQuotes }
        loadProfiles()
    }

    fun importPdfs(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            val newQuotes = mutableListOf<JSONObject>()
            var extractionWarnings = 0

            for (file in uris) {
                try {
                    yield()
                    val json = extractQuoteDataAsJson(convertPdfToTxt(context, file))
                    if (json.optString("totalAmount", "not found") == "not found") extractionWarnings++
                    newQuotes.add(json)
                } catch (e: Exception) {
                    extractionWarnings++
                }
            }

            persistQuotes(quoteList + newQuotes)

            withContext(Dispatchers.Main) {
                isLoading = false
                if (extractionWarnings > 0) {
                    android.widget.Toast.makeText(
                        context,
                        "Warning: $extractionWarnings file(s) had missing data...",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // Home Screen Actions
    private fun loadProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val profiles = dao.getAllProfiles()
            withContext(Dispatchers.Main) { savedProfiles = profiles }
        }
    }

    fun togglePin(id: Int, isPinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePinned(id, !isPinned)
            loadProfiles()
        }
    }

    fun deleteProfile(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = dao.getQuoteById(id)
            dao.deleteQuote(entity)
            loadProfiles()
        }
    }

    fun renameProfileFromHome(id: Int, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = dao.getQuoteById(id)
            dao.updateQuote(entity.copy(profileName = newName))
            loadProfiles()
        }
    }

    // Quote List Actions
    fun loadQuoteProfile(profileId: Int) {
        currentProfileId = profileId
        if (profileId == -1) {
            profileName = "Untitled"
            quoteList = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = dao.getQuoteById(profileId)
            val quotes = JSONArray(entity.quoteList)
            val parsedList = List(quotes.length()) { quotes.getJSONObject(it) }

            withContext(Dispatchers.Main) {
                profileName = entity.profileName
                quoteList = parsedList
            }
        }
    }

    fun renameProfile(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val existingEntity = dao.getQuoteById(currentProfileId)

            val updatedArray = JSONArray()
            quoteList.forEach { updatedArray.put(it) }


            dao.updateQuote(
                existingEntity.copy(
                    profileName = newName,
                    quoteList = updatedArray.toString()
                )
            )
            withContext(Dispatchers.Main) { profileName = newName }
            loadProfiles()
        }
    }

    fun updateQuotes(newQuotes: List<JSONObject>) {
        viewModelScope.launch(Dispatchers.IO) {
            persistQuotes(newQuotes)
        }
    }

    fun clearCurrentProfile() {
        quoteList = emptyList()
        profileName = "Untitled"
    }
}