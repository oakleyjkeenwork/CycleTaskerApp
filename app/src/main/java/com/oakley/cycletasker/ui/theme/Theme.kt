package com.oakley.cycletasker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    secondary = Color(0xFFA5D6A7),
    tertiary = Color(0xFFE0C36A),
    background = Color(0xFF0B0C0E),
    surface = Color(0xFF15171A),
    surfaceVariant = Color(0xFF202329),
    onPrimary = Color(0xFF08111F),
    onSecondary = Color(0xFF0A160D),
    onTertiary = Color(0xFF171203),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFFC7CBD1),
    outline = Color(0xFF3B4048)
)

@Composable
fun CycleTaskerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
