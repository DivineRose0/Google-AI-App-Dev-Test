package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.pow

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier
) {
    var selectedFinanceTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("[01:TIP/SPLIT]", "[02:DISCOUNT]", "[03:LOAN/EMI]")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("finance_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedFinanceTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {},
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedFinanceTab == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedFinanceTab = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("fin_tab_$index")
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedFinanceTab) {
            0 -> TipAndSplitView()
            1 -> DiscountAndTaxView()
            2 -> LoanEmiView()
        }
    }
}

@Composable
fun TipAndSplitView() {
    var billStr by remember { mutableStateOf("85.50") }
    var tipPercent by remember { mutableFloatStateOf(18f) }
    var splitCountStr by remember { mutableStateOf("3") }
    var roundUp by remember { mutableStateOf(false) }

    val bill = billStr.toDoubleOrNull() ?: 0.0
    val splitCount = (splitCountStr.toIntOrNull() ?: 1).coerceAtLeast(1)

    val rawTip = bill * (tipPercent / 100.0)
    val rawTotal = bill + rawTip
    val totalBill = if (roundUp) ceil(rawTotal) else rawTotal
    val actualTip = totalBill - bill
    val perPerson = totalBill / splitCount

    val fmt = DecimalFormat("$#,##0.00")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            RetroTerminalBox(title = "PER PERSON YIELD") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        fmt.format(perPerson),
                        fontFamily = Vt323Family,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TIP TOTAL: ${fmt.format(actualTip)}", style = MaterialTheme.typography.labelSmall)
                        Text("GROSS BILL: ${fmt.format(totalBill)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        item {
            RetroTerminalBox(title = "PARAMETERS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = billStr,
                        onValueChange = { billStr = it },
                        label = { Text("BILL AMOUNT ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("TIP: ${tipPercent.toInt()}%", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = tipPercent,
                        onValueChange = { tipPercent = it },
                        valueRange = 0f..40f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(10f, 15f, 18f, 20f, 25f).forEach { p ->
                            val isSel = tipPercent == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .clickable { tipPercent = p }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${p.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = splitCountStr,
                            onValueChange = { splitCountStr = it.filter { ch -> ch.isDigit() } },
                            label = { Text("PERSON COUNT") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = roundUp, onCheckedChange = { roundUp = it })
                            Text("ROUND", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DiscountAndTaxView() {
    var originalPriceStr by remember { mutableStateOf("120.00") }
    var discountPercentStr by remember { mutableStateOf("25") }
    var salesTaxPercentStr by remember { mutableStateOf("8.5") }

    val originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
    val discountPercent = (discountPercentStr.toDoubleOrNull() ?: 0.0) / 100.0
    val taxPercent = (salesTaxPercentStr.toDoubleOrNull() ?: 0.0) / 100.0

    val discountAmount = originalPrice * discountPercent
    val discountedPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)
    val taxAmount = discountedPrice * taxPercent
    val finalPrice = discountedPrice + taxAmount
    val totalSavings = discountAmount

    val fmt = DecimalFormat("$#,##0.00")

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            RetroTerminalBox(title = "NET CHARGE // AFTER TAX") {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        fmt.format(finalPrice),
                        fontFamily = Vt323Family,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "SAVINGS: ${fmt.format(totalSavings)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item {
            RetroTerminalBox(title = "INPUT PRICING") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originalPriceStr,
                        onValueChange = { originalPriceStr = it },
                        label = { Text("ORIGINAL PRICE ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = discountPercentStr,
                        onValueChange = { discountPercentStr = it },
                        label = { Text("DISCOUNT (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = salesTaxPercentStr,
                        onValueChange = { salesTaxPercentStr = it },
                        label = { Text("SALES TAX (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LoanEmiView() {
    var principalStr by remember { mutableStateOf("250000") }
    var interestRateStr by remember { mutableStateOf("6.5") }
    var tenureYearsStr by remember { mutableStateOf("30") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val annualRate = (interestRateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val tenureYears = tenureYearsStr.toDoubleOrNull() ?: 30.0

    val monthlyRate = annualRate / 12.0
    val totalMonths = tenureYears * 12.0

    val emi = remember(principal, monthlyRate, totalMonths) {
        if (principal <= 0.0 || totalMonths <= 0.0) 0.0
        else if (monthlyRate <= 0.0) principal / totalMonths
        else {
            val factor = (1.0 + monthlyRate).pow(totalMonths)
            (principal * monthlyRate * factor) / (factor - 1.0)
        }
    }

    val totalPayment = emi * totalMonths
    val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)

    val fmt = DecimalFormat("$#,##0.00")

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            RetroTerminalBox(title = "MONTHLY INSTALLMENT (EMI)") {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        fmt.format(emi),
                        fontFamily = Vt323Family,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL INTEREST: ${fmt.format(totalInterest)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("PAYMENT: ${fmt.format(totalPayment)}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item {
            RetroTerminalBox(title = "LOAN REGISTERS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it },
                        label = { Text("PRINCIPAL ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text("ANNUAL RATE (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tenureYearsStr,
                        onValueChange = { tenureYearsStr = it },
                        label = { Text("TERM (YEARS)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
