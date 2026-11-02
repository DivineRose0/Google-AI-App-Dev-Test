package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BitwiseOp
import com.example.engine.ProgrammerCalculatorEngine
import com.example.engine.RadixMode
import com.example.engine.WordSize
import com.example.ui.components.ButtonType
import com.example.ui.components.CalcButton
import com.example.ui.components.FunctionGraphView
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family

@Composable
fun ProgrammerCalculatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedProgTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("[01:RADIX/BITWISE]", "[02:GRAPHING f(x)]")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("programmer_calc_screen")
    ) {
        // Sub-Tab Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSel = selectedProgTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedProgTab = index }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (selectedProgTab == 0) {
            ProgrammerRadixBitwiseView()
        } else {
            FunctionGraphView()
        }
    }
}

@Composable
fun ProgrammerRadixBitwiseView() {
    val context = LocalContext.current
    val engine = remember { ProgrammerCalculatorEngine() }

    var stateTicker by remember { mutableIntStateOf(0) }
    var showBitboard by remember { mutableStateOf(false) }

    fun refresh() {
        stateTicker++
    }

    val currentRadix = engine.currentRadix
    val currentWordSize = engine.wordSize
    val hexStr = remember(stateTicker) { engine.getFormatted(RadixMode.HEX) }
    val decStr = remember(stateTicker) { engine.getFormatted(RadixMode.DEC) }
    val octStr = remember(stateTicker) { engine.getFormatted(RadixMode.OCT) }
    val binStr = remember(stateTicker) { engine.getFormatted(RadixMode.BIN) }
    val signedDecStr = remember(stateTicker) { engine.getSignedDec().toString() }
    val equationText = remember(stateTicker) { engine.equationDisplay }
    val pendingOpSymbol = remember(stateTicker) { engine.pendingOp.symbol }

    fun copyText(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "[ $label COPIED: $text ]", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // --- Retro Terminal Programmer CRT Telemetry ---
        RetroTerminalBox(
            title = "SYS://PROG_CORE // ${currentWordSize.displayName}",
            padding = 6.dp
        ) {
            // Word Size & Bitboard Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WordSize.values().forEach { ws ->
                        val isSel = ws == currentWordSize
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    engine.wordSize = ws
                                    engine.currentValue = engine.maskValue(engine.currentValue)
                                    refresh()
                                }
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                .testTag("ws_${ws.name}")
                        ) {
                            Text(
                                text = ws.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showBitboard = !showBitboard },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (showBitboard) "[HIDE BITS ▲]" else "[VIEW BITS ▼]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Running Equation / Operator indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (equationText.isNotBlank()) "EQ: $equationText" else "EQ: READY",
                    fontFamily = Vt323Family,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (pendingOpSymbol.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "OP: $pendingOpSymbol",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Radix Rows (HEX, DEC, OCT, BIN) - Click any row to switch active input radix!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF040704))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                RadixRow("HEX", hexStr, currentRadix == RadixMode.HEX, { engine.currentRadix = RadixMode.HEX; refresh() }) { copyText("HEX", hexStr) }
                RadixRow("DEC", "$decStr (Signed: $signedDecStr)", currentRadix == RadixMode.DEC, { engine.currentRadix = RadixMode.DEC; refresh() }) { copyText("DEC", decStr) }
                RadixRow("OCT", octStr, currentRadix == RadixMode.OCT, { engine.currentRadix = RadixMode.OCT; refresh() }) { copyText("OCT", octStr) }
                RadixRow("BIN", binStr, currentRadix == RadixMode.BIN, { engine.currentRadix = RadixMode.BIN; refresh() }) { copyText("BIN", binStr) }
            }

            // Interactive Bitboard
            AnimatedVisibility(visible = showBitboard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .padding(4.dp)
                ) {
                    Text(
                        text = "INTERACTIVE BITBOARD (TAP TO TOGGLE):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    val bitsTotal = currentWordSize.bits
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier.heightIn(max = 110.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(bitsTotal) { i ->
                            val bitIndex = bitsTotal - 1 - i
                            val isSet = ((engine.currentValue shr bitIndex) and 1uL) == 1uL
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSet) MaterialTheme.colorScheme.primary else Color(0xFF0D140E))
                                    .border(1.dp, if (isSet) MaterialTheme.colorScheme.primary else Color(0xFF1E2D20), RoundedCornerShape(2.dp))
                                    .clickable {
                                        engine.toggleBit(bitIndex)
                                        refresh()
                                    }
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSet) "1" else "0",
                                    fontSize = 12.sp,
                                    fontFamily = Vt323Family,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSet) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Bitwise Operations Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val bitOps = listOf(
                Pair("AND", BitwiseOp.AND),
                Pair("OR", BitwiseOp.OR),
                Pair("XOR", BitwiseOp.XOR),
                Pair("NOT", null),
                Pair("NAND", BitwiseOp.NAND),
                Pair("NOR", BitwiseOp.NOR),
                Pair("LSH", BitwiseOp.LSH),
                Pair("RSH", BitwiseOp.RSH),
                Pair("ROL", null),
                Pair("ROR", null)
            )

            bitOps.forEach { (label, op) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            when (label) {
                                "NOT" -> engine.executeNot()
                                "ROL" -> engine.executeRol()
                                "ROR" -> engine.executeRor()
                                else -> if (op != null) engine.setOp(op)
                            }
                            refresh()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[$label]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Programmer Hexadecimal / Octal / Decimal / Binary Keypad ---
        val isHex = currentRadix == RadixMode.HEX
        val isDecOrAbove = currentRadix == RadixMode.HEX || currentRadix == RadixMode.DEC
        val isOctOrAbove = currentRadix != RadixMode.BIN

        fun isKeyEnabled(char: Char): Boolean = when (char) {
            in 'A'..'F' -> isHex
            '8', '9' -> isDecOrAbove
            in '2'..'7' -> isOctOrAbove
            '0', '1' -> true
            else -> true
        }

        @Composable
        fun ProgKey(ch: Char, modifier: Modifier = Modifier) {
            val enabled = isKeyEnabled(ch)
            CalcButton(
                text = ch.toString(),
                onClick = {
                    if (enabled) {
                        engine.inputDigit(ch)
                        refresh()
                    }
                },
                modifier = modifier,
                buttonType = if (ch in 'A'..'F') ButtonType.FUNCTION else ButtonType.NUMBER,
                customTextColor = if (!enabled) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else null,
                minHeight = 40.dp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Hex letters row: A, B, C, D, E, F
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ProgKey('A', Modifier.weight(1f))
                ProgKey('B', Modifier.weight(1f))
                ProgKey('C', Modifier.weight(1f))
                ProgKey('D', Modifier.weight(1f))
                ProgKey('E', Modifier.weight(1f))
                ProgKey('F', Modifier.weight(1f))
            }

            // Row 1: 7, 8, 9, CLR, DEL
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ProgKey('7', Modifier.weight(1f))
                ProgKey('8', Modifier.weight(1f))
                ProgKey('9', Modifier.weight(1f))
                CalcButton("CLR", { engine.clear(); refresh() }, Modifier.weight(1f), ButtonType.ACTION, 14.sp, 40.dp)
                CalcButton("DEL", { engine.backspace(); refresh() }, Modifier.weight(1f), ButtonType.ACTION, 14.sp, 40.dp)
            }

            // Row 2: 4, 5, 6, +, -
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ProgKey('4', Modifier.weight(1f))
                ProgKey('5', Modifier.weight(1f))
                ProgKey('6', Modifier.weight(1f))
                CalcButton("+", { engine.setOp(BitwiseOp.ADD); refresh() }, Modifier.weight(1f), ButtonType.OPERATOR, 18.sp, 40.dp, testTag = "prog_btn_add")
                CalcButton("-", { engine.setOp(BitwiseOp.SUB); refresh() }, Modifier.weight(1f), ButtonType.OPERATOR, 18.sp, 40.dp, testTag = "prog_btn_sub")
            }

            // Row 3: 1, 2, 3, ×, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ProgKey('1', Modifier.weight(1f))
                ProgKey('2', Modifier.weight(1f))
                ProgKey('3', Modifier.weight(1f))
                CalcButton("×", { engine.setOp(BitwiseOp.MUL); refresh() }, Modifier.weight(1f), ButtonType.OPERATOR, 18.sp, 40.dp, testTag = "prog_btn_mul")
                CalcButton("÷", { engine.setOp(BitwiseOp.DIV); refresh() }, Modifier.weight(1f), ButtonType.OPERATOR, 18.sp, 40.dp, testTag = "prog_btn_div")
            }

            // Row 4: ± (2's comp), 0, MOD (%), EXEC (=)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CalcButton("±", { engine.toggleNegate(); refresh() }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp, testTag = "prog_btn_neg")
                ProgKey('0', Modifier.weight(1f))
                CalcButton("MOD", { engine.setOp(BitwiseOp.MOD); refresh() }, Modifier.weight(1f), ButtonType.OPERATOR, 14.sp, 40.dp, testTag = "prog_btn_mod")
                CalcButton("EXEC (=)", { engine.evaluate(); refresh() }, Modifier.weight(2f), ButtonType.EQUALS, 16.sp, 40.dp, testTag = "prog_btn_exec")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun RadixRow(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = "$label > ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = Vt323Family,
                fontSize = 17.sp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
