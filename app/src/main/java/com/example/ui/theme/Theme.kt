package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SlatyMint,
    onPrimary = SlatyDarkBackground,
    primaryContainer = SlatyPrimary,
    onPrimaryContainer = SlatyTextPrimary,
    secondary = SlatyLavender,
    onSecondary = SlatyDarkBackground,
    background = SlatyDarkBackground,
    onBackground = SlatyTextPrimary,
    surface = SlatyDarkSurface,
    onSurface = SlatyTextPrimary,
    surfaceVariant = SlatySurfaceElevated,
    onSurfaceVariant = SlatyTextSecondary,
    outline = SlatyCardBorder,
    error = SlatyError
  )

private val LightColorScheme = DarkColorScheme // Slaty uses cohesive dark theme as primary

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
