package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ExpressionEvaluator
import com.example.ui.components.ButtonType
import com.example.ui.components.CalcButton
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(
    onSaveCalculation: (String, String) -> Unit,
    onOpenHistory: () -> Unit,
    restoredExpression: String? = null,
    onClearRestoredExpression: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    var expression by remember { mutableStateOf("") }
    var isDegreeMode by remember { mutableStateOf(true) }
    var showScientific by remember { mutableStateOf(false) }
    var memoryValue by remember { mutableStateOf(0.0) }
    var hasMemory by remember { mutableStateOf(false) }
    var copyIndicatorVisible by remember { mutableStateOf(false) }

    // Blinking cursor state
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    LaunchedEffect(restoredExpression) {
        if (!restoredExpression.isNullOrBlank()) {
            expression = restoredExpression
            onClearRestoredExpression()
        }
    }

    val evaluator = remember(isDegreeMode) { ExpressionEvaluator(isDegreeMode) }
    val previewResult by remember(expression, isDegreeMode) {
        derivedStateOf { evaluator.quickPreview(expression) }
    }

    val exprScrollState = rememberScrollState()

    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    fun copyCurrentResult() {
        val target = previewResult ?: expression.ifBlank { "0" }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("OmniCalc Result", target))
        try {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        } catch (_: Exception) {}
        Toast.makeText(context, "[ RESULT COPIED: $target ]", Toast.LENGTH_SHORT).show()
        copyIndicatorVisible = true
    }

    LaunchedEffect(copyIndicatorVisible) {
        if (copyIndicatorVisible) {
            delay(1500)
            copyIndicatorVisible = false
        }
    }

    fun onAppend(str: String) {
        expression += str
    }

    fun onClear() {
        expression = ""
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            val multiChars = listOf("asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "log(", "ln(", "sqrt(", "cbrt(")
            val found = multiChars.firstOrNull { expression.endsWith(it) }
            expression = if (found != null) {
                expression.dropLast(found.length)
            } else {
                expression.dropLast(1)
            }
        }
    }

    fun onCalculate() {
        if (expression.isBlank()) return
        val res = evaluator.evaluate(expression)
        if (!res.isError) {
            onSaveCalculation(expression, res.formatted)
            expression = res.formatted
        } else {
            Toast.makeText(context, "[ ERR: ${res.errorMessage} ]", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleNegate() {
        if (expression.isEmpty()) {
            expression = "-"
            return
        }
        if (expression.startsWith("-(") && expression.endsWith(")")) {
            expression = expression.substring(2, expression.length - 1)
        } else if (expression.startsWith("-")) {
            expression = expression.substring(1)
        } else {
            expression = "-($expression)"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("calculator_screen"),
        verticalArrangement = Arrangement.Bottom
    ) {
        // --- Retro Terminal Bezel Display Area ---
        RetroTerminalBox(
            title = "SYS://TERMINAL_V3.2 // CRT_MATH_CORE",
            borderColor = MaterialTheme.colorScheme.outline,
            backgroundColor = MaterialTheme.colorScheme.surface,
            padding = 10.dp
        ) {
            // Status Badges Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // DEG/RAD Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                            .background(if (isDegreeMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .combinedClickable(onClick = { isDegreeMode = !isDegreeMode })
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("deg_rad_toggle")
                    ) {
                        Text(
                            text = if (isDegreeMode) "[DEG]" else "[RAD]",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDegreeMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (hasMemory) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "M=${evaluator.formatResult(memoryValue)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Persistent History button
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.size(32.dp).testTag("open_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Room History Logs",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- Display Result Area (With Long-Press & Floating Action Button) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF040704))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { copyCurrentResult() }
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("display_result_area")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    // Expression input line
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(exprScrollState),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "> ",
                            fontFamily = Vt323Family,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            fontFamily = Vt323Family,
                            fontSize = if (expression.length > 14) 34.sp else 42.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            letterSpacing = 1.sp,
                            modifier = Modifier.testTag("calc_expression_text")
                        )
                        Text(
                            text = if (cursorVisible) "_" else " ",
                            fontFamily = Vt323Family,
                            fontSize = 38.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Live Preview Result
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ LONG-PRESS DISPLAY TO COPY ]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        Text(
                            text = if (previewResult != null) "OUT: $previewResult" else "OUT: --",
                            fontFamily = Vt323Family,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.testTag("calc_preview_text")
                        )
                    }
                }

                // --- Floating Action Button Inside Display Area for 1-Tap Copy ---
                FloatingActionButton(
                    onClick = { copyCurrentResult() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(36.dp)
                        .testTag("copy_result_fab"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy result",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy Flash Indicator
                if (copyIndicatorVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "[ COPIED TO CLIPBOARD ]",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = Vt323Family
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Memory Bank Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val memModifier = Modifier.weight(1f)
            CalcButton("[MC]", {
                memoryValue = 0.0
                hasMemory = false
            }, memModifier, ButtonType.FUNCTION, 16.sp, 36.dp, testTag = "btn_mc")
            CalcButton("[MR]", {
                if (hasMemory) onAppend(evaluator.formatResult(memoryValue))
            }, memModifier, ButtonType.FUNCTION, 16.sp, 36.dp, testTag = "btn_mr")
            CalcButton("[M+]", {
                val currentVal = evaluator.evaluate(expression.ifBlank { "0" }).value
                if (!currentVal.isNaN()) {
                    memoryValue += currentVal
                    hasMemory = true
                }
            }, memModifier, ButtonType.FUNCTION, 16.sp, 36.dp, testTag = "btn_m_plus")
            CalcButton("[M-]", {
                val currentVal = evaluator.evaluate(expression.ifBlank { "0" }).value
                if (!currentVal.isNaN()) {
                    memoryValue -= currentVal
                    hasMemory = true
                }
            }, memModifier, ButtonType.FUNCTION, 16.sp, 36.dp, testTag = "btn_m_minus")
            CalcButton(if (showScientific) "[SCI▲]" else "[SCI▼]", {
                showScientific = !showScientific
            }, memModifier, ButtonType.OPERATOR, 16.sp, 36.dp, testTag = "btn_toggle_sci")
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- Collapsible Scientific Panel ---
        AnimatedVisibility(visible = showScientific) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("sin", { onAppend("sin(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("cos", { onAppend("cos(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("tan", { onAppend("tan(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("ln", { onAppend("ln(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("log", { onAppend("log(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("asin", { onAppend("asin(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("acos", { onAppend("acos(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("atan", { onAppend("atan(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("√", { onAppend("sqrt(") }, Modifier.weight(1f), ButtonType.FUNCTION, 20.sp, 40.dp)
                    CalcButton("^", { onAppend("^") }, Modifier.weight(1f), ButtonType.FUNCTION, 20.sp, 40.dp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("π", { onAppend("π") }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 40.dp)
                    CalcButton("e", { onAppend("e") }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 40.dp)
                    CalcButton("n!", { onAppend("!") }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 40.dp)
                    CalcButton("1/x", { onAppend("1/(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                    CalcButton("|x|", { onAppend("abs(") }, Modifier.weight(1f), ButtonType.FUNCTION, 16.sp, 40.dp)
                }
            }
        }

        // --- Retro Terminal Mechanical Keypad ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: AC, ( ), %, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("CLR", { onClear() }, Modifier.weight(1f), ButtonType.ACTION, 20.sp, testTag = "btn_ac")
                CalcButton("( )", {
                    val open = expression.count { it == '(' }
                    val close = expression.count { it == ')' }
                    if (open > close && expression.isNotEmpty() && (expression.last().isDigit() || expression.last() == ')')) {
                        onAppend(")")
                    } else {
                        onAppend("(")
                    }
                }, Modifier.weight(1f), ButtonType.OPERATOR, 20.sp, testTag = "btn_parentheses")
                CalcButton("%", { onAppend("%") }, Modifier.weight(1f), ButtonType.OPERATOR, 20.sp, testTag = "btn_percent")
                CalcButton("÷", { onAppend("÷") }, Modifier.weight(1f), ButtonType.OPERATOR, 22.sp, testTag = "btn_divide")
            }

            // Row 2: 7, 8, 9, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("7", { onAppend("7") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("8", { onAppend("8") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("9", { onAppend("9") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("×", { onAppend("×") }, Modifier.weight(1f), ButtonType.OPERATOR, 22.sp, testTag = "btn_multiply")
            }

            // Row 3: 4, 5, 6, -
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("4", { onAppend("4") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("5", { onAppend("5") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("6", { onAppend("6") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("-", { onAppend("-") }, Modifier.weight(1f), ButtonType.OPERATOR, 22.sp, testTag = "btn_minus")
            }

            // Row 4: 1, 2, 3, +
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("1", { onAppend("1") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("2", { onAppend("2") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("3", { onAppend("3") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton("+", { onAppend("+") }, Modifier.weight(1f), ButtonType.OPERATOR, 22.sp, testTag = "btn_plus")
            }

            // Row 5: ±, 0, ., DEL, =
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CalcButton("±", { toggleNegate() }, Modifier.weight(1f), ButtonType.NUMBER, 20.sp, testTag = "btn_negate")
                CalcButton("0", { onAppend("0") }, Modifier.weight(1f), ButtonType.NUMBER)
                CalcButton(".", { onAppend(".") }, Modifier.weight(1f), ButtonType.NUMBER, testTag = "btn_dot")
                CalcButton("DEL", { onBackspace() }, Modifier.weight(1f), ButtonType.ACTION, 18.sp, testTag = "btn_backspace")
                CalcButton("EXEC", { onCalculate() }, Modifier.weight(1f), ButtonType.EQUALS, 18.sp, testTag = "btn_equals")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
