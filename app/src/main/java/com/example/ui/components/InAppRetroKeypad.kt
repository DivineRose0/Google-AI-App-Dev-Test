package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InAppRetroKeypad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    extraRow: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (extraRow != null) {
            extraRow()
        }

        // Row 1: 7, 8, 9, CLR
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("7", { onKey("7") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("8", { onKey("8") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("9", { onKey("9") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("CLR", { onKey("CLR") }, Modifier.weight(1f), ButtonType.ACTION, 16.sp, 40.dp)
        }

        // Row 2: 4, 5, 6, DEL
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("4", { onKey("4") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("5", { onKey("5") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("6", { onKey("6") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("DEL", { onKey("DEL") }, Modifier.weight(1f), ButtonType.ACTION, 16.sp, 40.dp)
        }

        // Row 3: 1, 2, 3, ±
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("1", { onKey("1") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("2", { onKey("2") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("3", { onKey("3") }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton("±", { onKey("±") }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 40.dp)
        }

        // Row 4: 0, .
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CalcButton("0", { onKey("0") }, Modifier.weight(2f), ButtonType.NUMBER, 20.sp, 40.dp)
            CalcButton(".", { onKey(".") }, Modifier.weight(2f), ButtonType.NUMBER, 20.sp, 40.dp)
        }
    }
}
