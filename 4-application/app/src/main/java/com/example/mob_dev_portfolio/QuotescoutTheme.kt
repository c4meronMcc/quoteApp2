package com.example.mob_dev_portfolio

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily


val DisplayFont = FontFamily(Font(R.font.dm_serif_display))
val BodyFont = FontFamily(Font(R.font.outfit_regular)) // Or FontFamily.SansSerif if you didn't download it

val Paper = Color(0xFFFAF9F6)
val Ink = Color(0xFF1C1B1F)
val Amber = Color(0xFFFF6B35)
val AmberLight = Color(0xFFFFB085)
val Border = Color(0xFFE0E0E0)
val GreenDeep = Color(0xFF2E7D32)
val GreenLight = Color(0xFF81C784)
val Muted = Color(0xFF757575)
val QuoteRed = Color(0xFFD32F2F)
val Cream = Color(0xFFFFFDD0)


private val LightColorScheme = lightColorScheme(
    primary = Amber,
    background = Paper,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    error = QuoteRed
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberLight,
    background = Ink,
    surface = Color(0xFF2D2C31),
    onPrimary = Ink,
    onBackground = Paper,
    onSurface = Paper,
    error = Color(0xFFEF9A9A)
)

@Composable
fun QuoteScoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // updates the Android status bar at the very top of the phone
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}