package com.example.lession.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LessionColorScheme = darkColorScheme(
    primary = LessionPurplePrimary,
    secondary = LessionLimeAccent,
    tertiary = LessionLimeAccent,
    background = LessionBackgroundDark,
    surface = LessionCardDark,
    onPrimary = LessionTextWhite,
    onSecondary = LessionBackgroundDark,
    onBackground = LessionTextWhite,
    onSurface = LessionTextWhite,
)

@Composable
fun LessionTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LessionColorScheme,
        typography = Typography,
        content = content,
    )
}
