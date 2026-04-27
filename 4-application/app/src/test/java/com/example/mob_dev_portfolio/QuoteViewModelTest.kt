package com.example.mob_dev_portfolio

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockDao: QuoteDao

    // We mock the Application context
    private val mockApp: android.app.Application = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockDao = mockk(relaxed = true)

        // CRITICAL FIX: Intercept the Room Database creation so the local test doesn't crash!
        mockkObject(QuoteDatabase.Companion)
        val mockDb = mockk<QuoteDatabase>(relaxed = true)
        every { mockDb.quoteDao() } returns mockDao
        every { QuoteDatabase.getDatabase(any()) } returns mockDb

        // Setup mock responses for the DAO
        coEvery { mockDao.getAllProfiles() } returns listOf(
            QuoteProfileSummary(1, "Test Profile", "01/01/2026", false)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll() // Clean up static mocks
    }

    @Test
    fun `clearCurrentProfile resets quoteList and profileName to defaults`() {
        // Initialize the ViewModel (safely uses the mocked database)
        val viewModel = QuoteViewModel(mockApp)

        // Fire the function
        viewModel.clearCurrentProfile()

        // Assert that the state was correctly wiped
        assertEquals("Profile name should reset to Untitled", "Untitled", viewModel.profileName)
        assertEquals("Quote list should be completely empty", emptyList<org.json.JSONObject>(), viewModel.quoteList)
    }
}