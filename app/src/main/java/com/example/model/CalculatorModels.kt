package com.example.model

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TimeIntervalItem(
    val id: Long = System.currentTimeMillis(),
    val sign: String = "+", // "+" or "-"
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0
)

data class DailyShift(
    val dayName: String,
    val isWorking: Boolean = true,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val unpaidBreakMinutes: Int = 30
) {
    val totalHours: Double
        get() {
            if (!isWorking) return 0.0
            val startTotalMinutes = startHour * 60 + startMinute
            var endTotalMinutes = endHour * 60 + endMinute
            if (endTotalMinutes < startTotalMinutes) {
                // cross midnight
                endTotalMinutes += 24 * 60
            }
            val netMinutes = (endTotalMinutes - startTotalMinutes - unpaidBreakMinutes).coerceAtLeast(0)
            return netMinutes / 60.0
        }
}

data class PayBreakdown(
    val regularHours: Double,
    val overtimeHours: Double,
    val doubleTimeHours: Double,
    val regularPay: Double,
    val overtimePay: Double,
    val doubleTimePay: Double,
    val tipsAndBonus: Double,
    val grossPay: Double,
    val taxDeductionAmount: Double,
    val netPay: Double
)
