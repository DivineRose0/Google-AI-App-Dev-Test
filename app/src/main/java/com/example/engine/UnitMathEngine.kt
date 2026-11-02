package com.example.engine

data class UnitMathTerm(
    val id: Long = System.currentTimeMillis(),
    val sign: String = "+", // "+" or "-"
    val value: Double = 1.0,
    val unit: ConversionUnit,
    val category: UnitCategory
)

data class UnitMathResult(
    val outputValue: Double,
    val formattedResult: String,
    val targetUnit: ConversionUnit,
    val category: UnitCategory,
    val equationString: String,
    val isSuccess: Boolean = true,
    val errorMessage: String = ""
)

object UnitMathEngine {

    fun calculate(
        terms: List<UnitMathTerm>,
        targetUnit: ConversionUnit
    ): UnitMathResult {
        if (terms.isEmpty()) {
            return UnitMathResult(0.0, "0", targetUnit, UnitCategory.LENGTH, "", false, "No terms provided")
        }

        val primaryCategory = terms.first().category

        // Validate dimensional consistency
        for (term in terms) {
            if (term.category != primaryCategory) {
                return UnitMathResult(
                    outputValue = 0.0,
                    formattedResult = "ERR",
                    targetUnit = targetUnit,
                    category = primaryCategory,
                    equationString = "",
                    isSuccess = false,
                    errorMessage = "Dimension mismatch: cannot combine ${primaryCategory.displayName} with ${term.category.displayName}"
                )
            }
        }

        // Validate target unit belongs to same category
        val availableUnits = UnitConverterEngine.getUnitsForCategory(primaryCategory)
        val validTarget = availableUnits.firstOrNull { it.id == targetUnit.id } ?: availableUnits.first()

        // Sum in base unit
        var baseTotal = 0.0
        val eqBuilder = StringBuilder()

        terms.forEachIndexed { index, term ->
            val signMultiplier = if (term.sign == "-") -1.0 else 1.0
            if (index > 0) {
                eqBuilder.append(" ${term.sign} ")
            } else if (term.sign == "-") {
                eqBuilder.append("-")
            }
            eqBuilder.append("${UnitConverterEngine.formatConvertedValue(term.value)} ${term.unit.symbol}")

            // Convert term to base
            val inBase = term.value * term.unit.toBaseFactor * signMultiplier
            baseTotal += inBase
        }

        // Convert baseTotal to targetUnit
        val finalVal = baseTotal / validTarget.toBaseFactor
        val formatted = UnitConverterEngine.formatConvertedValue(finalVal)

        eqBuilder.append(" = $formatted ${validTarget.symbol}")

        return UnitMathResult(
            outputValue = finalVal,
            formattedResult = formatted,
            targetUnit = validTarget,
            category = primaryCategory,
            equationString = eqBuilder.toString(),
            isSuccess = true
        )
    }

    // Parse simple string like "1 ft + 2 ft" or "100m + 1.5km"
    fun parseAndCalculate(expression: String, targetUnitQuery: String? = null): UnitMathResult {
        val clean = expression.trim()
        val regex = Regex("([+-]?)\\s*([0-9.]+)\\s*([a-zA-Z°µ/]+)")
        val matches = regex.findAll(clean).toList()

        if (matches.isEmpty()) {
            val defUnit = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH).first()
            return UnitMathResult(0.0, "0", defUnit, UnitCategory.LENGTH, clean, false, "Invalid unit expression format")
        }

        val terms = mutableListOf<UnitMathTerm>()
        for (m in matches) {
            val signStr = m.groupValues[1].ifBlank { "+" }
            val valNum = m.groupValues[2].toDoubleOrNull() ?: 0.0
            val unitStr = m.groupValues[3]

            val found = UnitConverterEngine.findUnit(unitStr)
            if (found == null) {
                val defUnit = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH).first()
                return UnitMathResult(0.0, "0", defUnit, UnitCategory.LENGTH, clean, false, "Unknown unit symbol: '$unitStr'")
            }

            terms.add(
                UnitMathTerm(
                    sign = if (signStr == "-") "-" else "+",
                    value = valNum,
                    unit = found.first,
                    category = found.second
                )
            )
        }

        val cat = terms.first().category
        val targetUnit = if (!targetUnitQuery.isNullOrBlank()) {
            val f = UnitConverterEngine.findUnit(targetUnitQuery)
            if (f != null && f.second == cat) f.first else terms.last().unit
        } else {
            terms.last().unit
        }

        return calculate(terms, targetUnit)
    }
}
