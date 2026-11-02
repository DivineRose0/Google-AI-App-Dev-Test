package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Vt323Family

@Composable
fun RetroTerminalBox(
    title: String? = null,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    padding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp))
    ) {
        if (!title.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(borderColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "┌─[ $title ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "─┐",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            content()
        }
    }
}
