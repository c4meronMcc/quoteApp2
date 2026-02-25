package com.example.mob_dev_portfolio

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call your new component here!
                    FilePicker (
                         onFileSelected = { selectedUri ->
                            // This block runs when the user finishes picking a file
                            Log.d("FILE_PICKER", "The user selected: $selectedUri")

                            // NEXT STEP: Pass this URI into a reader function
                        }
                    )
                }
            }
        }
    }
}