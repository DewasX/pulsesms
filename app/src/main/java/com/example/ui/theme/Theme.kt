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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFA8C7FA),
    secondary = GoogleBlueContainerDark,
    tertiary = Pink80,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF2D2F31),
    onPrimary = Color(0xFF062E6F),
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E918F)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GoogleBlue,
    secondary = GoogleBlueContainerLight,
    tertiary = Pink40,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFEAF1FB),
    onPrimary = Color.White,
    onSecondary = Color(0xFF041E49),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFFC4C7C5)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
