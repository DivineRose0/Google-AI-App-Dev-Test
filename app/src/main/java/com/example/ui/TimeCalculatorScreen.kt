package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimeIntervalItem
import com.example.ui.components.ButtonType
import com.example.ui.components.CalcButton
import com.example.ui.components.InAppRetroKeypad
import com.example.ui.components.RetroTerminalBox
import com.example.ui.theme.Vt323Family
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun TimeCalculatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("[01:DURATION]", "[02:ELAPSED]", "[03:DECIMAL]", "[04:OFFSET]")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("time_calc_screen")
    ) {
        // Retro Sub-Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {},
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedSubTab = index }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("time_tab_$index")
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

        Spacer(modifier = Modifier.height(4.dp))

        when (selectedSubTab) {
            0 -> DurationMathView()
            1 -> ElapsedTimeView()
            2 -> DecimalTimeConverterView()
            3 -> DateOffsetView()
        }
    }
}

enum class DurationField { HOURS, MINUTES, SECONDS }

@Composable
fun DurationMathView() {
    val items = remember {
        mutableStateListOf(
            TimeIntervalItem(sign = "+", hours = 8, minutes = 30, seconds = 0),
            TimeIntervalItem(sign = "+", hours = 4, minutes = 45, seconds = 0)
        )
    }

    var activeTermIndex by remember { mutableIntStateOf(0) }
    var activeField by remember { mutableStateOf(DurationField.HOURS) }

    val totalSeconds = remember(items.toList()) {
        items.fold(0L) { acc, item ->
            val secs = item.hours * 3600L + item.minutes * 60L + item.seconds
            if (item.sign == "+") acc + secs else acc - secs
        }
    }

    val isNegative = totalSeconds < 0
    val absSec = abs(totalSeconds)
    val days = absSec / 86400
    val hours = (absSec % 86400) / 3600
    val minutes = (absSec % 3600) / 60
    val seconds = absSec % 60
    val totalDecimalHours = absSec / 3600.0

    fun onKeyInput(key: String) {
        if (items.isEmpty()) return
        val safeIndex = activeTermIndex.coerceIn(0, items.size - 1)
        val term = items[safeIndex]

        val currentVal = when (activeField) {
            DurationField.HOURS -> term.hours
            DurationField.MINUTES -> term.minutes
            DurationField.SECONDS -> term.seconds
        }

        val newVal: Int = when (key) {
            "CLR" -> 0
            "DEL" -> currentVal / 10
            "+1" -> currentVal + 1
            "-1" -> (currentVal - 1).coerceAtLeast(0)
            "±" -> {
                val nextSign = if (term.sign == "+") "-" else "+"
                items[safeIndex] = term.copy(sign = nextSign)
                return
            }
            "." -> return
            else -> {
                val digit = key.toIntOrNull() ?: return
                val calculated = currentVal * 10 + digit
                if (activeField == DurationField.MINUTES || activeField == DurationField.SECONDS) {
                    calculated.coerceAtMost(59)
                } else {
                    calculated.coerceAtMost(9999)
                }
            }
        }

        items[safeIndex] = when (activeField) {
            DurationField.HOURS -> term.copy(hours = newVal)
            DurationField.MINUTES -> term.copy(minutes = newVal.coerceIn(0, 59))
            DurationField.SECONDS -> term.copy(seconds = newVal.coerceIn(0, 59))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Grand Result Banner
        RetroTerminalBox(title = "TOTAL DURATION TELEMETRY", padding = 6.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildString {
                        if (isNegative) append("- ")
                        if (days > 0) append("${days}d ")
                        append("${hours}h ${minutes}m ${seconds}s")
                    },
                    fontFamily = Vt323Family,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "= ${DecimalFormat("#,##0.000").format(if (isNegative) -totalDecimalHours else totalDecimalHours)} DEC_HOURS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Terms List Carousel
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val isEditing = activeTermIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.5.dp,
                            if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (isEditing) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeTermIndex = index }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.sign,
                            fontFamily = Vt323Family,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.sign == "+") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable {
                                val next = if (item.sign == "+") "-" else "+"
                                items[index] = item.copy(sign = next)
                            }
                        )

                        Text(
                            text = "${item.hours}h",
                            fontFamily = Vt323Family,
                            fontSize = 18.sp,
                            color = if (isEditing && activeField == DurationField.HOURS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isEditing && activeField == DurationField.HOURS) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isEditing && activeField == DurationField.HOURS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    activeTermIndex = index
                                    activeField = DurationField.HOURS
                                }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )

                        Text(
                            text = "${item.minutes}m",
                            fontFamily = Vt323Family,
                            fontSize = 18.sp,
                            color = if (isEditing && activeField == DurationField.MINUTES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isEditing && activeField == DurationField.MINUTES) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isEditing && activeField == DurationField.MINUTES) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    activeTermIndex = index
                                    activeField = DurationField.MINUTES
                                }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )

                        Text(
                            text = "${item.seconds}s",
                            fontFamily = Vt323Family,
                            fontSize = 18.sp,
                            color = if (isEditing && activeField == DurationField.SECONDS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isEditing && activeField == DurationField.SECONDS) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isEditing && activeField == DurationField.SECONDS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable {
                                    activeTermIndex = index
                                    activeField = DurationField.SECONDS
                                }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )

                        if (items.size > 1) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        items.removeAt(index)
                                        if (activeTermIndex >= items.size) activeTermIndex = items.size - 1
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Active Field Target Indicator & Add Term button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(DurationField.HOURS to "HOURS", DurationField.MINUTES to "MINUTES", DurationField.SECONDS to "SECONDS").forEach { (field, label) ->
                    val isSel = activeField == field
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { activeField = field }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "[$label]",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = {
                    items.add(TimeIntervalItem())
                    activeTermIndex = items.size - 1
                    activeField = DurationField.HOURS
                },
                shape = RoundedCornerShape(2.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("TERM", fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // In-App Retro Keypad
        InAppRetroKeypad(
            onKey = { onKeyInput(it) },
            extraRow = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("+1h", {
                        if (items.isNotEmpty()) {
                            val safe = activeTermIndex.coerceIn(0, items.size - 1)
                            items[safe] = items[safe].copy(hours = items[safe].hours + 1)
                        }
                    }, Modifier.weight(1f), ButtonType.FUNCTION, 13.sp, 34.dp)

                    CalcButton("+15m", {
                        if (items.isNotEmpty()) {
                            val safe = activeTermIndex.coerceIn(0, items.size - 1)
                            val nextM = items[safe].minutes + 15
                            items[safe] = items[safe].copy(hours = items[safe].hours + nextM / 60, minutes = nextM % 60)
                        }
                    }, Modifier.weight(1f), ButtonType.FUNCTION, 13.sp, 34.dp)

                    CalcButton("+30m", {
                        if (items.isNotEmpty()) {
                            val safe = activeTermIndex.coerceIn(0, items.size - 1)
                            val nextM = items[safe].minutes + 30
                            items[safe] = items[safe].copy(hours = items[safe].hours + nextM / 60, minutes = nextM % 60)
                        }
                    }, Modifier.weight(1f), ButtonType.FUNCTION, 13.sp, 34.dp)

                    CalcButton("[RESET]", {
                        items.clear()
                        items.add(TimeIntervalItem())
                        activeTermIndex = 0
                    }, Modifier.weight(1f), ButtonType.ACTION, 12.sp, 34.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

enum class ElapsedTargetField { START_TIME, END_TIME, BREAK_MINS }

@Composable
fun ElapsedTimeView() {
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(17) }
    var endMinute by remember { mutableIntStateOf(30) }
    var breakMinutes by remember { mutableIntStateOf(30) }

    var activeTarget by remember { mutableStateOf(ElapsedTargetField.START_TIME) }

    val netMinutes = remember(startHour, startMinute, endHour, endMinute, breakMinutes) {
        val s = startHour * 60 + startMinute
        var e = endHour * 60 + endMinute
        if (e < s) e += 24 * 60
        (e - s - breakMinutes).coerceAtLeast(0)
    }

    val hours = netMinutes / 60
    val minutes = netMinutes % 60
    val decimalHours = netMinutes / 60.0

    fun onKeyInput(key: String) {
        when (activeTarget) {
            ElapsedTargetField.BREAK_MINS -> {
                when (key) {
                    "CLR" -> breakMinutes = 0
                    "DEL" -> breakMinutes /= 10
                    else -> {
                        val digit = key.toIntOrNull() ?: return
                        breakMinutes = (breakMinutes * 10 + digit).coerceAtMost(480)
                    }
                }
            }
            ElapsedTargetField.START_TIME -> {
                when (key) {
                    "+1" -> startHour = (startHour + 1) % 24
                    "-1" -> startHour = if (startHour == 0) 23 else startHour - 1
                    "CLR" -> { startHour = 9; startMinute = 0 }
                    else -> {}
                }
            }
            ElapsedTargetField.END_TIME -> {
                when (key) {
                    "+1" -> endHour = (endHour + 1) % 24
                    "-1" -> endHour = if (endHour == 0) 23 else endHour - 1
                    "CLR" -> { endHour = 17; endMinute = 0 }
                    else -> {}
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        RetroTerminalBox(title = "ELAPSED WORK LOG TELEMETRY", padding = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${hours}h ${minutes}m",
                    fontFamily = Vt323Family,
                    fontSize = 40.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "= ${DecimalFormat("#,##0.00").format(decimalHours)} DEC_HOURS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Control Bezel
        RetroTerminalBox(title = "CLOCK REGISTERS (SELECT TO EDIT)", padding = 6.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Start Time Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeTarget == ElapsedTargetField.START_TIME) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeTarget == ElapsedTargetField.START_TIME) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeTarget = ElapsedTargetField.START_TIME }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("START TIME:", style = MaterialTheme.typography.labelSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatTime(startHour, startMinute), fontFamily = Vt323Family, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = {
                                val nextM = (startMinute + 15) % 60
                                if (nextM == 0) startHour = (startHour + 1) % 24
                                startMinute = nextM
                            },
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("+15m", fontSize = 10.sp)
                        }
                    }
                }

                // End Time Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeTarget == ElapsedTargetField.END_TIME) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeTarget == ElapsedTargetField.END_TIME) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeTarget = ElapsedTargetField.END_TIME }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("END TIME:", style = MaterialTheme.typography.labelSmall)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(formatTime(endHour, endMinute), fontFamily = Vt323Family, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = {
                                val nextM = (endMinute + 15) % 60
                                if (nextM == 0) endHour = (endHour + 1) % 24
                                endMinute = nextM
                            },
                            shape = RoundedCornerShape(2.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("+15m", fontSize = 10.sp)
                        }
                    }
                }

                // Break Minutes Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeTarget == ElapsedTargetField.BREAK_MINS) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeTarget == ElapsedTargetField.BREAK_MINS) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeTarget = ElapsedTargetField.BREAK_MINS }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UNPAID BREAK:", style = MaterialTheme.typography.labelSmall)
                    Text("$breakMinutes MIN", fontFamily = Vt323Family, fontSize = 22.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // In-App Retro Keypad
        InAppRetroKeypad(
            onKey = { onKeyInput(it) },
            extraRow = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("BRK:0m", { breakMinutes = 0 }, Modifier.weight(1f), ButtonType.FUNCTION, 11.sp, 32.dp)
                    CalcButton("BRK:30m", { breakMinutes = 30 }, Modifier.weight(1f), ButtonType.FUNCTION, 11.sp, 32.dp)
                    CalcButton("BRK:45m", { breakMinutes = 45 }, Modifier.weight(1f), ButtonType.FUNCTION, 11.sp, 32.dp)
                    CalcButton("BRK:60m", { breakMinutes = 60 }, Modifier.weight(1f), ButtonType.FUNCTION, 11.sp, 32.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

enum class DecimalTimeTarget { DECIMAL, HOURS, MINUTES, SECONDS }

@Composable
fun DecimalTimeConverterView() {
    var decimalStr by remember { mutableStateOf("7.75") }
    var hoursStr by remember { mutableStateOf("7") }
    var minsStr by remember { mutableStateOf("45") }
    var secsStr by remember { mutableStateOf("0") }

    var activeField by remember { mutableStateOf(DecimalTimeTarget.DECIMAL) }

    fun updateFromDecimal(dec: String) {
        decimalStr = dec
        val d = dec.toDoubleOrNull() ?: 0.0
        val totalSecs = (d * 3600).toLong()
        hoursStr = (totalSecs / 3600).toString()
        minsStr = ((totalSecs % 3600) / 60).toString()
        secsStr = (totalSecs % 60).toString()
    }

    fun updateFromHMS(hStr: String, mStr: String, sStr: String) {
        hoursStr = hStr
        minsStr = mStr
        secsStr = sStr
        val h = hStr.toLongOrNull() ?: 0L
        val m = mStr.toLongOrNull() ?: 0L
        val s = sStr.toLongOrNull() ?: 0L
        val totalSecs = h * 3600 + m * 60 + s
        decimalStr = DecimalFormat("#.####").format(totalSecs / 3600.0)
    }

    fun onKeyInput(ch: String) {
        when (activeField) {
            DecimalTimeTarget.DECIMAL -> {
                var s = decimalStr
                when (ch) {
                    "CLR" -> s = "0"
                    "DEL" -> s = if (s.length <= 1) "0" else s.dropLast(1)
                    "." -> if (!s.contains(".")) s += "."
                    "±" -> s = if (s.startsWith("-")) s.substring(1) else "-$s"
                    else -> s = if (s == "0") ch else s + ch
                }
                updateFromDecimal(s)
            }
            DecimalTimeTarget.HOURS -> {
                var h = hoursStr
                when (ch) {
                    "CLR" -> h = "0"
                    "DEL" -> h = if (h.length <= 1) "0" else h.dropLast(1)
                    else -> if (ch.all { it.isDigit() }) h = if (h == "0") ch else h + ch
                }
                updateFromHMS(h, minsStr, secsStr)
            }
            DecimalTimeTarget.MINUTES -> {
                var m = minsStr
                when (ch) {
                    "CLR" -> m = "0"
                    "DEL" -> m = if (m.length <= 1) "0" else m.dropLast(1)
                    else -> if (ch.all { it.isDigit() }) {
                        val nextM = (m.toIntOrNull() ?: 0) * 10 + (ch.toIntOrNull() ?: 0)
                        m = nextM.coerceAtMost(59).toString()
                    }
                }
                updateFromHMS(hoursStr, m, secsStr)
            }
            DecimalTimeTarget.SECONDS -> {
                var sc = secsStr
                when (ch) {
                    "CLR" -> sc = "0"
                    "DEL" -> sc = if (sc.length <= 1) "0" else sc.dropLast(1)
                    else -> if (ch.all { it.isDigit() }) {
                        val nextS = (sc.toIntOrNull() ?: 0) * 10 + (ch.toIntOrNull() ?: 0)
                        sc = nextS.coerceAtMost(59).toString()
                    }
                }
                updateFromHMS(hoursStr, minsStr, sc)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        RetroTerminalBox(title = "DECIMAL TIME REGISTER", padding = 6.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .border(
                        1.dp,
                        if (activeField == DecimalTimeTarget.DECIMAL) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(2.dp)
                    )
                    .background(if (activeField == DecimalTimeTarget.DECIMAL) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                    .clickable { activeField = DecimalTimeTarget.DECIMAL }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DECIMAL HOURS:", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "$decimalStr hrs",
                    fontFamily = Vt323Family,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        RetroTerminalBox(title = "HH : MM : SS REGISTERS", padding = 6.dp) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Hours Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeField == DecimalTimeTarget.HOURS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeField == DecimalTimeTarget.HOURS) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeField = DecimalTimeTarget.HOURS }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HOURS", style = MaterialTheme.typography.labelSmall)
                        Text(hoursStr, fontFamily = Vt323Family, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Minutes Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeField == DecimalTimeTarget.MINUTES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeField == DecimalTimeTarget.MINUTES) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeField = DecimalTimeTarget.MINUTES }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MINUTES", style = MaterialTheme.typography.labelSmall)
                        Text(minsStr, fontFamily = Vt323Family, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }

                // Seconds Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .border(
                            1.dp,
                            if (activeField == DecimalTimeTarget.SECONDS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(2.dp)
                        )
                        .background(if (activeField == DecimalTimeTarget.SECONDS) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                        .clickable { activeField = DecimalTimeTarget.SECONDS }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SECONDS", style = MaterialTheme.typography.labelSmall)
                        Text(secsStr, fontFamily = Vt323Family, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // In-App Retro Keypad
        InAppRetroKeypad(
            onKey = { onKeyInput(it) }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

enum class DateOffsetField { DAYS, HOURS }

@Composable
fun DateOffsetView() {
    var daysStr by remember { mutableStateOf("30") }
    var hoursStr by remember { mutableStateOf("0") }
    var isAddition by remember { mutableStateOf(true) }

    var activeField by remember { mutableStateOf(DateOffsetField.DAYS) }

    val days = daysStr.toIntOrNull() ?: 0
    val hours = hoursStr.toIntOrNull() ?: 0

    val calculatedDate = remember(days, hours, isAddition) {
        val cal = Calendar.getInstance()
        val sign = if (isAddition) 1 else -1
        cal.add(Calendar.DAY_OF_YEAR, sign * days)
        cal.add(Calendar.HOUR_OF_DAY, sign * hours)
        cal.time
    }

    val dateFormat = SimpleDateFormat("EEE, yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun onKeyInput(ch: String) {
        var s = if (activeField == DateOffsetField.DAYS) daysStr else hoursStr
        when (ch) {
            "CLR" -> s = "0"
            "DEL" -> s = if (s.length <= 1) "0" else s.dropLast(1)
            else -> if (ch.all { it.isDigit() }) s = if (s == "0") ch else s + ch
        }
        if (activeField == DateOffsetField.DAYS) {
            daysStr = s
        } else {
            hoursStr = s
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        RetroTerminalBox(title = "PROJECTED DATE // BIOS CLOCK", padding = 6.dp) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dateFormat.format(calculatedDate).uppercase(),
                    fontFamily = Vt323Family,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "TIME: ${timeFormat.format(calculatedDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Direction & Field Selector
        RetroTerminalBox(title = "OFFSET PARAMETERS (SELECT TO TYPE)", padding = 6.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { isAddition = true },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isAddition) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f).height(30.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("[+ ADD TO DATE]", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { isAddition = false },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!isAddition) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f).height(30.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("[- SUBTRACT FROM DATE]", fontSize = 11.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Days Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .border(
                                1.dp,
                                if (activeField == DateOffsetField.DAYS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(2.dp)
                            )
                            .background(if (activeField == DateOffsetField.DAYS) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                            .clickable { activeField = DateOffsetField.DAYS }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DAYS OFFSET", style = MaterialTheme.typography.labelSmall)
                            Text("$daysStr d", fontFamily = Vt323Family, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Hours Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .border(
                                1.dp,
                                if (activeField == DateOffsetField.HOURS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(2.dp)
                            )
                            .background(if (activeField == DateOffsetField.HOURS) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
                            .clickable { activeField = DateOffsetField.HOURS }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HOURS OFFSET", style = MaterialTheme.typography.labelSmall)
                            Text("$hoursStr h", fontFamily = Vt323Family, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // In-App Retro Keypad
        InAppRetroKeypad(
            onKey = { onKeyInput(it) },
            extraRow = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CalcButton("+1d", { daysStr = ((daysStr.toIntOrNull() ?: 0) + 1).toString() }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)
                    CalcButton("+7d", { daysStr = ((daysStr.toIntOrNull() ?: 0) + 7).toString() }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)
                    CalcButton("+30d", { daysStr = ((daysStr.toIntOrNull() ?: 0) + 30).toString() }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)
                    CalcButton("+1h", { hoursStr = ((hoursStr.toIntOrNull() ?: 0) + 1).toString() }, Modifier.weight(1f), ButtonType.FUNCTION, 12.sp, 32.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.getDefault(), "%02d:%02d %s", h, minute, amPm)
}
