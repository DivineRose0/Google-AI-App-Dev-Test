package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Vt323Family

enum class ButtonType {
    NUMBER,
    OPERATOR,
    ACTION,
    FUNCTION,
    EQUALS
}

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.NUMBER,
    fontSize: TextUnit = 24.sp,
    minHeight: Dp = 54.dp,
    customBgColor: Color? = null,
    customTextColor: Color? = null,
    hapticsEnabled: Boolean = true,
    testTag: String = "btn_$text"
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        label = "btnScale"
    )

    val phosphorColor = MaterialTheme.colorScheme.primary
    val borderDim = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error

    val bgColor = customBgColor ?: when (buttonType) {
        ButtonType.NUMBER -> if (isPressed) phosphorColor.copy(alpha = 0.2f) else surfaceColor
        ButtonType.OPERATOR -> if (isPressed) phosphorColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
        ButtonType.ACTION -> if (isPressed) errorColor.copy(alpha = 0.35f) else Color(0xFF1E0A0C)
        ButtonType.FUNCTION -> if (isPressed) phosphorColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ButtonType.EQUALS -> if (isPressed) surfaceColor else phosphorColor
    }

    val textColor = customTextColor ?: when (buttonType) {
        ButtonType.NUMBER -> phosphorColor
        ButtonType.OPERATOR -> MaterialTheme.colorScheme.secondary
        ButtonType.ACTION -> errorColor
        ButtonType.FUNCTION -> MaterialTheme.colorScheme.secondary
        ButtonType.EQUALS -> if (isPressed) phosphorColor else MaterialTheme.colorScheme.onPrimary
    }

    val currentBorder = when {
        isPressed -> phosphorColor
        buttonType == ButtonType.EQUALS -> phosphorColor
        buttonType == ButtonType.ACTION -> errorColor.copy(alpha = 0.7f)
        buttonType == ButtonType.OPERATOR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        else -> borderDim
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = minHeight)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.5.dp, currentBorder, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                if (hapticsEnabled) {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}
                }
                onClick()
            }
            .testTag(testTag)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontFamily = Vt323Family,
            fontWeight = if (buttonType == ButtonType.EQUALS) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 1.sp
        )
    }
}
