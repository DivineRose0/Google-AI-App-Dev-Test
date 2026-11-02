package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.ui.components.ButtonType
import com.example.ui.components.CalcButton
import com.example.ui.components.InAppRetroKeypad
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family
import kotlinx.coroutines.delay

@Composable
fun UnitConverterScreen(
    modifier: Modifier = Modifier
) {
    var selectedModeTab by remember { mutableIntStateOf(0) }
    val modeTabs = listOf("[01:CONVERT]", "[02:UNIT MATH]")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("unit_converter_screen")
    ) {
        // Mode Selector Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            modeTabs.forEachIndexed { index, title ->
                val isSel = selectedModeTab == index
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
                        .clickable { selectedModeTab = index }
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

        if (selectedModeTab == 0) {
            DirectUnitConverterView()
        } else {
            FullUnitCalculationView()
        }
    }
}

@Composable
fun DirectUnitConverterView() {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val units = remember(selectedCategory) { UnitConverterEngine.getUnitsForCategory(selectedCategory) }

    var fromUnit by remember(selectedCategory) { mutableStateOf(units[0]) }
    var toUnit by remember(selectedCategory) {
        mutableStateOf(if (units.size > 1) units[1] else units[0])
    }

    var inputValueStr by remember { mutableStateOf("1") }
    val inputValue = inputValueStr.toDoubleOrNull() ?: 0.0

    val convertedValue = remember(inputValue, fromUnit, toUnit, selectedCategory) {
        UnitConverterEngine.convert(inputValue, fromUnit, toUnit, selectedCategory)
    }

    var isSwapped by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isSwapped) 180f else 0f, label = "swapRot")

    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    fun onKeyInput(ch: String) {
        when (ch) {
            "CLR" -> inputValueStr = "0"
            "DEL" -> {
                inputValueStr = if (inputValueStr.length <= 1 || (inputValueStr.length == 2 && inputValueStr.startsWith("-"))) {
                    "0"
                } else {
                    inputValueStr.dropLast(1)
                }
            }
            "." -> {
                if (!inputValueStr.contains(".")) {
                    inputValueStr += "."
                }
            }
            "±" -> {
                inputValueStr = if (inputValueStr.startsWith("-")) {
                    inputValueStr.substring(1)
                } else if (inputValueStr == "0") {
                    "-0"
                } else {
                    "-$inputValueStr"
                }
            }
            "+1" -> {
                val v = (inputValueStr.toDoubleOrNull() ?: 0.0) + 1.0
                inputValueStr = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "-1" -> {
                val v = (inputValueStr.toDoubleOrNull() ?: 0.0) - 1.0
                inputValueStr = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "×10" -> {
                val v = (inputValueStr.toDoubleOrNull() ?: 0.0) * 10.0
                inputValueStr = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "÷10" -> {
                val v = (inputValueStr.toDoubleOrNull() ?: 0.0) / 10.0
                inputValueStr = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            else -> {
                inputValueStr = if (inputValueStr == "0") ch else inputValueStr + ch
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Horizontal Categories Bar (19 Categories)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(UnitConverterEngine.categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable {
                            selectedCategory = cat
                            val catUnits = UnitConverterEngine.getUnitsForCategory(cat)
                            fromUnit = catUnits[0]
                            toUnit = if (catUnits.size > 1) catUnits[1] else catUnits[0]
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("chip_${cat.name}")
                ) {
                    Text(
                        text = "[${cat.code}] ${cat.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Conversion Result Box & Value Display
        RetroTerminalBox(title = "CONVERT // ${selectedCategory.displayName.uppercase()}", padding = 6.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Interactive In-App Input Value Display (NO POP-UP KEYBOARD)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF040704))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IN [${fromUnit.symbol}] >",
                        fontFamily = Vt323Family,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inputValueStr,
                            fontFamily = Vt323Family,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cursorVisible) "_" else " ",
                            fontFamily = Vt323Family,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Output Result Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF040704))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OUT [${toUnit.symbol}] =",
                        fontFamily = Vt323Family,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${UnitConverterEngine.formatConvertedValue(convertedValue)} ${toUnit.symbol}",
                        fontFamily = Vt323Family,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Unit Selector Dropdowns with Swap Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UnitDropdown(
                        label = "FROM",
                        selectedUnit = fromUnit,
                        allUnits = units,
                        onSelect = { fromUnit = it },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            isSwapped = !isSwapped
                            val temp = fromUnit
                            fromUnit = toUnit
                            toUnit = temp
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(rotationAngle)
                            .testTag("swap_units_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    UnitDropdown(
                        label = "TO",
                        selectedUnit = toUnit,
                        allUnits = units,
                        onSelect = { toUnit = it },
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val text = "${UnitConverterEngine.formatConvertedValue(convertedValue)} ${toUnit.symbol}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Unit Conversion", text))
                            Toast.makeText(context, "[ COPIED: $text ]", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- FULL IN-APP RETRO KEYPAD (ZERO POP-UP KEYBOARD) ---
        InAppRetroKeypad(
            onKey = { onKeyInput(it) },
            extraRow = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("+1", { onKeyInput("+1") }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                    CalcButton("-1", { onKeyInput("-1") }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                    CalcButton("×10", { onKeyInput("×10") }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                    CalcButton("÷10", { onKeyInput("÷10") }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun FullUnitCalculationView() {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val categoryUnits = remember(selectedCategory) { UnitConverterEngine.getUnitsForCategory(selectedCategory) }

    val terms = remember(selectedCategory) {
        val u1 = categoryUnits.firstOrNull { it.id == "ft" } ?: categoryUnits[0]
        val u2 = categoryUnits.firstOrNull { it.id == "ft" } ?: categoryUnits[0]
        mutableStateListOf(
            UnitMathTerm(sign = "+", value = 1.0, unit = u1, category = selectedCategory),
            UnitMathTerm(sign = "+", value = 2.0, unit = u2, category = selectedCategory)
        )
    }

    var targetUnit by remember(selectedCategory) {
        mutableStateOf(categoryUnits.firstOrNull { it.id == "yd" } ?: categoryUnits.first())
    }

    var activeTermIndex by remember { mutableIntStateOf(0) }
    val currentTerm = terms.getOrNull(activeTermIndex) ?: terms.firstOrNull()

    var termValStr by remember(activeTermIndex, currentTerm?.value) {
        mutableStateOf(currentTerm?.let { UnitConverterEngine.formatConvertedValue(it.value).replace(",", "") } ?: "1")
    }

    val mathResult = remember(terms.toList(), targetUnit) {
        UnitMathEngine.calculate(terms, targetUnit)
    }

    fun onKeyInput(ch: String) {
        if (terms.isEmpty()) return
        val safeIndex = activeTermIndex.coerceIn(0, terms.size - 1)
        var s = termValStr

        when (ch) {
            "CLR" -> s = "0"
            "DEL" -> s = if (s.length <= 1) "0" else s.dropLast(1)
            "." -> if (!s.contains(".")) s += "."
            "±" -> {
                val nextSign = if (terms[safeIndex].sign == "+") "-" else "+"
                terms[safeIndex] = terms[safeIndex].copy(sign = nextSign)
            }
            "+1" -> {
                val v = (s.toDoubleOrNull() ?: 0.0) + 1.0
                s = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "-1" -> {
                val v = ((s.toDoubleOrNull() ?: 0.0) - 1.0).coerceAtLeast(0.0)
                s = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "×10" -> {
                val v = (s.toDoubleOrNull() ?: 0.0) * 10.0
                s = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            "÷10" -> {
                val v = (s.toDoubleOrNull() ?: 0.0) / 10.0
                s = UnitConverterEngine.formatConvertedValue(v).replace(",", "")
            }
            else -> {
                s = if (s == "0") ch else s + ch
            }
        }
        termValStr = s
        val parsedNum = s.toDoubleOrNull() ?: 0.0
        terms[safeIndex] = terms[safeIndex].copy(value = parsedNum)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Result Output Card
        RetroTerminalBox(title = "UNIT MATH OUTPUT", padding = 6.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mathResult.isSuccess) {
                    Text(
                        text = mathResult.equationString,
                        fontFamily = Vt323Family,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "RESULT: ${mathResult.formattedResult} ${mathResult.targetUnit.symbol} (${mathResult.targetUnit.name.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "[ ERR: ${mathResult.errorMessage} ]",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Target Unit Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("TARGET:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            UnitDropdown(
                label = "OUTPUT UNIT",
                selectedUnit = targetUnit,
                allUnits = categoryUnits,
                onSelect = { targetUnit = it },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val lastUnit = terms.lastOrNull()?.unit ?: categoryUnits.first()
                    terms.add(UnitMathTerm(sign = "+", value = 1.0, unit = lastUnit, category = selectedCategory))
                    activeTermIndex = terms.size - 1
                },
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("TERM", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Terms List (Click any term to edit it using the In-App Keypad)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(terms) { index, term ->
                val isEditing = activeTermIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.5.dp,
                            if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isEditing) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                        .clickable {
                            activeTermIndex = index
                            termValStr = UnitConverterEngine.formatConvertedValue(term.value).replace(",", "")
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${term.sign} ${UnitConverterEngine.formatConvertedValue(term.value)} ${term.unit.symbol}",
                            fontFamily = Vt323Family,
                            fontSize = 18.sp,
                            fontWeight = if (isEditing) FontWeight.Bold else FontWeight.Normal,
                            color = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (terms.size > 1) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        terms.removeAt(index)
                                        if (activeTermIndex >= terms.size) activeTermIndex = terms.size - 1
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Active Term Unit Selector
        if (currentTerm != null && activeTermIndex in terms.indices) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "EDITING TERM ${activeTermIndex + 1} UNIT:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                UnitDropdown(
                    label = "UNIT",
                    selectedUnit = terms[activeTermIndex].unit,
                    allUnits = categoryUnits,
                    onSelect = { u -> terms[activeTermIndex] = terms[activeTermIndex].copy(unit = u) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- FULL IN-APP RETRO KEYPAD (ZERO POP-UP KEYBOARD) ---
        InAppRetroKeypad(
            onKey = { onKeyInput(it) },
            extraRow = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("1 ft + 2 ft = 1 yd", {
                        val lengthUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)
                        val ft = lengthUnits.first { it.id == "ft" }
                        val yd = lengthUnits.first { it.id == "yd" }
                        selectedCategory = UnitCategory.LENGTH
                        terms.clear()
                        terms.add(UnitMathTerm(sign = "+", value = 1.0, unit = ft, category = UnitCategory.LENGTH))
                        terms.add(UnitMathTerm(sign = "+", value = 2.0, unit = ft, category = UnitCategory.LENGTH))
                        targetUnit = yd
                        activeTermIndex = 0
                        termValStr = "1"
                    }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)

                    CalcButton("500m + 2.5km = 3km", {
                        val lengthUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)
                        val m = lengthUnits.first { it.id == "m" }
                        val km = lengthUnits.first { it.id == "km" }
                        selectedCategory = UnitCategory.LENGTH
                        terms.clear()
                        terms.add(UnitMathTerm(sign = "+", value = 500.0, unit = m, category = UnitCategory.LENGTH))
                        terms.add(UnitMathTerm(sign = "+", value = 2.5, unit = km, category = UnitCategory.LENGTH))
                        targetUnit = km
                        activeTermIndex = 0
                        termValStr = "500"
                    }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    label: String,
    selectedUnit: ConversionUnit,
    allUnits: List<ConversionUnit>,
    onSelect: (ConversionUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "${selectedUnit.symbol} (${selectedUnit.name})",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 10.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allUnits.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${unit.name.uppercase()} [${unit.symbol}]",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
