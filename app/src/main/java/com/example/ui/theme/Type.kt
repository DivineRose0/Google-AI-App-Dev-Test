package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val ShareTechMonoFamily = FontFamily(
    Font(R.font.share_tech_mono, FontWeight.Normal)
)

val Vt323Family = FontFamily(
    Font(R.font.vt323, FontWeight.Normal)
)

// Retro Terminal Monospaced Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Vt323Family,
        fontWeight = FontWeight.Normal,
        fontSize = 52.sp,
        lineHeight = 56.sp,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Vt323Family,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 1.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Vt323Family,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
