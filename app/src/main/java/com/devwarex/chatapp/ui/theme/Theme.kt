package com.devwarex.chatapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val DarkColorPalette = darkColors(
    primary = Blue200,
    primaryVariant = Blue700,
    secondary = Orange300,
    background = DarkBackground,
    surface = LightBlack,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onBackground = Color.Black,
    onSurface = LightBackground

)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    secondary = Orange700,
    background = LightBackground,
    surface = Color.White,
    onPrimary = LightBackground,
    onSecondary = DarkBackground,
    onSurface = DarkBackground,
    onBackground = Color.White
)

@Composable
fun ChatAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}