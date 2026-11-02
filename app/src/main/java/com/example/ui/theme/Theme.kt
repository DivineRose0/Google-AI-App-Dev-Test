package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class RetroTerminalProfile(val title: String, val promptPrefix: String) {
    GREEN_PHOSPHOR("P1: Green CRT", "SYS://TERM-01>"),
    AMBER_CRT("P3: Amber CRT", "SYS://AMBER-03>"),
    CYAN_VT100("VT100: Cyan", "SYS://VT100> ")
}

// Retro Terminal Shapes - sharp corners and precise borders
val RetroShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

private val GreenPhosphorScheme = darkColorScheme(
    primary = TermGreenPrimary,
    onPrimary = TermGreenOnPrimary,
    primaryContainer = TermGreenSurfaceVariant,
    onPrimaryContainer = TermGreenPrimary,
    secondary = TermGreenSecondary,
    onSecondary = Color(0xFF00240C),
    secondaryContainer = Color(0xFF102814),
    onSecondaryContainer = TermGreenSecondary,
    tertiary = TermYellow,
    background = TermGreenBg,
    onBackground = TermGreenPrimary,
    surface = TermGreenSurface,
    onSurface = TermGreenPrimary,
    surfaceVariant = TermGreenSurfaceVariant,
    onSurfaceVariant = TermGreenDim,
    outline = TermGreenBorder,
    outlineVariant = TermGreenBorderDim,
    error = TermRed,
    onError = Color.White
)

private val AmberCrtScheme = darkColorScheme(
    primary = TermAmberPrimary,
    onPrimary = TermAmberOnPrimary,
    primaryContainer = TermAmberSurfaceVariant,
    onPrimaryContainer = TermAmberPrimary,
    secondary = TermAmberSecondary,
    onSecondary = Color(0xFF281800),
    secondaryContainer = Color(0xFF2E1F07),
    onSecondaryContainer = TermAmberSecondary,
    tertiary = TermYellow,
    background = TermAmberBg,
    onBackground = TermAmberPrimary,
    surface = TermAmberSurface,
    onSurface = TermAmberPrimary,
    surfaceVariant = TermAmberSurfaceVariant,
    onSurfaceVariant = TermAmberDim,
    outline = TermAmberBorder,
    outlineVariant = TermAmberBorderDim,
    error = TermRed,
    onError = Color.White
)

private val CyanVt100Scheme = darkColorScheme(
    primary = TermCyanPrimary,
    onPrimary = TermCyanOnPrimary,
    primaryContainer = TermCyanSurfaceVariant,
    onPrimaryContainer = TermCyanPrimary,
    secondary = TermCyanSecondary,
    onSecondary = Color(0xFF00222B),
    secondaryContainer = Color(0xFF0C2433),
    onSecondaryContainer = TermCyanSecondary,
    tertiary = TermGreenPrimary,
    background = TermCyanBg,
    onBackground = TermCyanPrimary,
    surface = TermCyanSurface,
    onSurface = TermCyanPrimary,
    surfaceVariant = TermCyanSurfaceVariant,
    onSurfaceVariant = TermCyanDim,
    outline = TermCyanBorder,
    outlineVariant = TermCyanBorderDim,
    error = TermRed,
    onError = Color.White
)

@Composable
fun OmniCalcTheme(
    profile: RetroTerminalProfile = RetroTerminalProfile.GREEN_PHOSPHOR,
    content: @Composable () -> Unit
) {
    val colorScheme = when (profile) {
        RetroTerminalProfile.GREEN_PHOSPHOR -> GreenPhosphorScheme
        RetroTerminalProfile.AMBER_CRT -> AmberCrtScheme
        RetroTerminalProfile.CYAN_VT100 -> CyanVt100Scheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = RetroShapes,
        content = content
    )
}
