package com.oakley.cycletasker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class UiThemeColors(
    val primary: Long = 0xFF8AB4F8,
    val secondary: Long = 0xFF8AB4F8,
    val tertiary: Long = 0xFFE0C36A,
    val background: Long = 0xFF0B0C0E,
    val surface: Long = 0xFF15171A,
    val surfaceVariant: Long = 0xFF202329,
    val outline: Long = 0xFF3B4048
)

@Composable
fun CycleTaskerTheme(
    colors: UiThemeColors = UiThemeColors(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Color(colors.primary),
        secondary = Color(colors.secondary),
        tertiary = Color(colors.tertiary),
        background = Color(colors.background),
        surface = Color(colors.surface),
        surfaceVariant = Color(colors.surfaceVariant),
        onPrimary = Color(0xFF08111F),
        onSecondary = Color(0xFF08111F),
        onTertiary = Color(0xFF171203),
        onBackground = Color(0xFFF3F4F6),
        onSurface = Color(0xFFF3F4F6),
        onSurfaceVariant = Color(0xFFC7CBD1),
        outline = Color(colors.outline)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
