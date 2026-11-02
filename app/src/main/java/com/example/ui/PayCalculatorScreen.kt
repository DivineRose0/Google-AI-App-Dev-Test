package com.example.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyShift
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun PayCalculatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedPayTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("[01:WAGE]", "[02:TIMESHEET]", "[03:CONVERTER]")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("pay_calc_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedPayTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {},
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedPayTab == index
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
                        .clickable { selectedPayTab = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("pay_tab_$index")
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

        when (selectedPayTab) {
            0 -> QuickWageView()
            1 -> WeeklyTimesheetView()
            2 -> SalaryConverterView()
        }
    }
}

@Composable
fun QuickWageView() {
    var hourlyRateStr by remember { mutableStateOf("25.00") }
    var regularHoursStr by remember { mutableStateOf("40") }
    var overtimeHoursStr by remember { mutableStateOf("5") }
    var doubleTimeHoursStr by remember { mutableStateOf("0") }
    var tipsStr by remember { mutableStateOf("0") }
    var taxRateStr by remember { mutableStateOf("18") }

    val hourlyRate = hourlyRateStr.toDoubleOrNull() ?: 0.0
    val regularHours = regularHoursStr.toDoubleOrNull() ?: 0.0
    val overtimeHours = overtimeHoursStr.toDoubleOrNull() ?: 0.0
    val doubleTimeHours = doubleTimeHoursStr.toDoubleOrNull() ?: 0.0
    val tips = tipsStr.toDoubleOrNull() ?: 0.0
    val taxRate = (taxRateStr.toDoubleOrNull() ?: 0.0) / 100.0

    val regularPay = regularHours * hourlyRate
    val overtimePay = overtimeHours * (hourlyRate * 1.5)
    val doubleTimePay = doubleTimeHours * (hourlyRate * 2.0)
    val grossPay = regularPay + overtimePay + doubleTimePay + tips
    val taxDeduction = grossPay * taxRate
    val netPay = (grossPay - taxDeduction).coerceAtLeast(0.0)

    val fmt = DecimalFormat("$#,##0.00")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            RetroTerminalBox(title = "NET PAYROLL // TAKE-HOME YIELD") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = fmt.format(netPay),
                        fontFamily = Vt323Family,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GROSS: ${fmt.format(grossPay)}", style = MaterialTheme.typography.labelSmall)
                        Text("HOURS: ${regularHours + overtimeHours + doubleTimeHours}h", style = MaterialTheme.typography.labelSmall)
                        Text("TAX: -${fmt.format(taxDeduction)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            RetroTerminalBox(title = "PAYROLL PARAMETERS") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourlyRateStr,
                        onValueChange = { hourlyRateStr = it },
                        label = { Text("HOURLY RATE ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = regularHoursStr,
                            onValueChange = { regularHoursStr = it },
                            label = { Text("REG_HRS") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = overtimeHoursStr,
                            onValueChange = { overtimeHoursStr = it },
                            label = { Text("OT_HRS (1.5x)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = doubleTimeHoursStr,
                            onValueChange = { doubleTimeHoursStr = it },
                            label = { Text("2.0x_HRS") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tipsStr,
                            onValueChange = { tipsStr = it },
                            label = { Text("BONUS/TIPS ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = taxRateStr,
                        onValueChange = { taxRateStr = it },
                        label = { Text("EST_TAX_DEDUCTIONS (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            RetroTerminalBox(title = "AUDIT BREAKDOWN") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("REGULAR WAGES:", style = MaterialTheme.typography.bodySmall)
                        Text(fmt.format(regularPay), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    if (overtimeHours > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("OVERTIME WAGES:", style = MaterialTheme.typography.bodySmall)
                            Text(fmt.format(overtimePay), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GROSS EARNINGS:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        Text(fmt.format(grossPay), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TAX DEDUCTION (${taxRateStr}%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        Text("-${fmt.format(taxDeduction)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WeeklyTimesheetView() {
    val context = LocalContext.current
    var hourlyRateStr by remember { mutableStateOf("25.00") }
    var weeklyOtThresholdStr by remember { mutableStateOf("40") }

    val daysList = remember {
        mutableStateListOf(
            DailyShift("MON", isWorking = true, startHour = 9, startMinute = 0, endHour = 17, endMinute = 30, unpaidBreakMinutes = 30),
            DailyShift("TUE", isWorking = true, startHour = 9, startMinute = 0, endHour = 17, endMinute = 30, unpaidBreakMinutes = 30),
            DailyShift("WED", isWorking = true, startHour = 9, startMinute = 0, endHour = 17, endMinute = 30, unpaidBreakMinutes = 30),
            DailyShift("THU", isWorking = true, startHour = 9, startMinute = 0, endHour = 17, endMinute = 30, unpaidBreakMinutes = 30),
            DailyShift("FRI", isWorking = true, startHour = 9, startMinute = 0, endHour = 17, endMinute = 0, unpaidBreakMinutes = 30),
            DailyShift("SAT", isWorking = false, startHour = 10, startMinute = 0, endHour = 14, endMinute = 0, unpaidBreakMinutes = 0),
            DailyShift("SUN", isWorking = false, startHour = 10, startMinute = 0, endHour = 14, endMinute = 0, unpaidBreakMinutes = 0)
        )
    }

    val hourlyRate = hourlyRateStr.toDoubleOrNull() ?: 0.0
    val otThreshold = weeklyOtThresholdStr.toDoubleOrNull() ?: 40.0

    val totalHours = daysList.sumOf { it.totalHours }
    val regularHours = totalHours.coerceAtMost(otThreshold)
    val overtimeHours = (totalHours - otThreshold).coerceAtLeast(0.0)

    val regularPay = regularHours * hourlyRate
    val overtimePay = overtimeHours * (hourlyRate * 1.5)
    val totalGrossPay = regularPay + overtimePay

    val currencyFormatter = DecimalFormat("$#,##0.00")
    val decimalFormatter = DecimalFormat("#,##0.00")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            RetroTerminalBox(title = "TIMESHEET // WEEKLY PAY SUMMARY") {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        currencyFormatter.format(totalGrossPay),
                        fontFamily = Vt323Family,
                        fontSize = 44.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Text("TOTAL: ${decimalFormatter.format(totalHours)}h", style = MaterialTheme.typography.labelSmall)
                        Text("REG: ${decimalFormatter.format(regularHours)}h", style = MaterialTheme.typography.labelSmall)
                        Text("OT: ${decimalFormatter.format(overtimeHours)}h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        item {
            RetroTerminalBox(title = "RATE SETUP") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = hourlyRateStr,
                        onValueChange = { hourlyRateStr = it },
                        label = { Text("RATE ($/h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weeklyOtThresholdStr,
                        onValueChange = { weeklyOtThresholdStr = it },
                        label = { Text("OT THRESHOLD (h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        itemsIndexed(daysList) { index, shift ->
            RetroTerminalBox(padding = 6.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = shift.isWorking,
                            onCheckedChange = { isChecked ->
                                daysList[index] = shift.copy(isWorking = isChecked)
                            }
                        )
                        Text(
                            text = shift.dayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (shift.isWorking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (shift.isWorking) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${formatShiftTime(shift.startHour, shift.startMinute)}-${formatShiftTime(shift.endHour, shift.endMinute)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable {
                                    TimePickerDialog(context, { _, h, m ->
                                        daysList[index] = shift.copy(startHour = h, startMinute = m)
                                    }, shift.startHour, shift.startMinute, false).show()
                                }
                            )
                            Text(
                                text = "[${decimalFormatter.format(shift.totalHours)}h]",
                                fontFamily = Vt323Family,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text("[OFF]", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun SalaryConverterView() {
    var hourlyStr by remember { mutableStateOf("30.00") }
    var dailyStr by remember { mutableStateOf("240.00") }
    var weeklyStr by remember { mutableStateOf("1200.00") }
    var biWeeklyStr by remember { mutableStateOf("2400.00") }
    var monthlyStr by remember { mutableStateOf("5200.00") }
    var annualStr by remember { mutableStateOf("62400.00") }

    fun recomputeFromHourly(rate: Double) {
        val daily = rate * 8.0
        val weekly = rate * 40.0
        val biWeekly = weekly * 2.0
        val annual = weekly * 52.0
        val monthly = annual / 12.0

        hourlyStr = DecimalFormat("0.00").format(rate)
        dailyStr = DecimalFormat("0.00").format(daily)
        weeklyStr = DecimalFormat("0.00").format(weekly)
        biWeeklyStr = DecimalFormat("0.00").format(biWeekly)
        monthlyStr = DecimalFormat("0.00").format(monthly)
        annualStr = DecimalFormat("0.00").format(annual)
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RetroTerminalBox(title = "CONVERT // EQUIVALENT PAY PERIODS") {
            Text("BIOS BASELINE: 40 HRS/WK // 2,080 HRS/YR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                OutlinedTextField(
                    value = hourlyStr,
                    onValueChange = { str ->
                        hourlyStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly(it) }
                    },
                    label = { Text("HOURLY ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = dailyStr,
                    onValueChange = { str ->
                        dailyStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly(it / 8.0) }
                    },
                    label = { Text("DAILY (8h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = weeklyStr,
                    onValueChange = { str ->
                        weeklyStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly(it / 40.0) }
                    },
                    label = { Text("WEEKLY (40h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = biWeeklyStr,
                    onValueChange = { str ->
                        biWeeklyStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly(it / 80.0) }
                    },
                    label = { Text("BI-WEEKLY (80h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = monthlyStr,
                    onValueChange = { str ->
                        monthlyStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly((it * 12.0) / 2080.0) }
                    },
                    label = { Text("MONTHLY (12/yr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = annualStr,
                    onValueChange = { str ->
                        annualStr = str
                        str.toDoubleOrNull()?.let { recomputeFromHourly(it / 2080.0) }
                    },
                    label = { Text("ANNUAL SALARY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

private fun formatShiftTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%02d:%02d%s", h, minute, amPm)
}
