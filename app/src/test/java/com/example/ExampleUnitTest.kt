package com.example

import com.example.engine.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBasicMathCalculations() {
        val evaluator = ExpressionEvaluator(isDegreeMode = true)

        val res1 = evaluator.evaluate("2 + 2")
        assertEquals(4.0, res1.value, 1e-6)

        val res2 = evaluator.evaluate("10 - 4 * 2")
        assertEquals(2.0, res2.value, 1e-6)

        val res3 = evaluator.evaluate("(10 - 4) * 2")
        assertEquals(12.0, res3.value, 1e-6)

        val res4 = evaluator.evaluate("2^3")
        assertEquals(8.0, res4.value, 1e-6)

        val res5 = evaluator.evaluate("5!")
        assertEquals(120.0, res5.value, 1e-6)

        val res6 = evaluator.evaluate("sin(90)")
        assertEquals(1.0, res6.value, 1e-6)

        val res7 = evaluator.evaluate("sqrt(16)")
        assertEquals(4.0, res7.value, 1e-6)
    }

    @Test
    fun testUnitConversions() {
        val lengthUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)
        val meter = lengthUnits.first { it.id == "m" }
        val km = lengthUnits.first { it.id == "km" }

        val kmVal = UnitConverterEngine.convert(1000.0, meter, km, UnitCategory.LENGTH)
        assertEquals(1.0, kmVal, 1e-6)

        val tempUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.TEMPERATURE)
        val c = tempUnits.first { it.id == "c" }
        val f = tempUnits.first { it.id == "f" }

        val fVal = UnitConverterEngine.convert(100.0, c, f, UnitCategory.TEMPERATURE)
        assertEquals(212.0, fVal, 1e-6)
    }

    @Test
    fun testFullUnitCalculation_OneFootPlusTwoFeetEqualsOneYard() {
        // User example: 1 ft + 2 ft = 1 yd (1 yard is 3 feet)
        val lengthUnits = UnitConverterEngine.getUnitsForCategory(UnitCategory.LENGTH)
        val ft = lengthUnits.first { it.id == "ft" }
        val yd = lengthUnits.first { it.id == "yd" }

        val terms = listOf(
            UnitMathTerm(sign = "+", value = 1.0, unit = ft, category = UnitCategory.LENGTH),
            UnitMathTerm(sign = "+", value = 2.0, unit = ft, category = UnitCategory.LENGTH)
        )

        val result = UnitMathEngine.calculate(terms, yd)
        assertTrue(result.isSuccess)
        assertEquals(1.0, result.outputValue, 1e-6)
        assertEquals("1", result.formattedResult)
    }

    @Test
    fun testProgrammerCalculatorHexAndBitwise() {
        val engine = ProgrammerCalculatorEngine()
        engine.wordSize = WordSize.BYTE
        engine.currentRadix = RadixMode.HEX

        // Input 0x0F
        engine.inputDigit('0')
        engine.inputDigit('F')
        assertEquals("0F", engine.getFormatted(RadixMode.HEX))
        assertEquals("15", engine.getFormatted(RadixMode.DEC))
        assertEquals("0000 1111", engine.getFormatted(RadixMode.BIN))

        // Bitwise AND with 0x03
        engine.setOp(BitwiseOp.AND)
        engine.inputDigit('3')
        engine.evaluate()

        assertEquals("03", engine.getFormatted(RadixMode.HEX))
        assertEquals("3", engine.getFormatted(RadixMode.DEC))
        assertEquals("0000 0011", engine.getFormatted(RadixMode.BIN))

        // Test Addition: 10 + 5 = 15
        engine.clear()
        engine.currentRadix = RadixMode.DEC
        engine.inputDigit('1')
        engine.inputDigit('0')
        assertEquals("10", engine.getFormatted(RadixMode.DEC))

        engine.setOp(BitwiseOp.ADD)
        engine.inputDigit('5')
        assertEquals("5", engine.getFormatted(RadixMode.DEC))

        engine.evaluate()
        assertEquals("15", engine.getFormatted(RadixMode.DEC))
        assertEquals("0F", engine.getFormatted(RadixMode.HEX))

        // Test Subtraction: 20 - 7 = 13
        engine.clear()
        engine.inputDigit('2')
        engine.inputDigit('0')
        engine.setOp(BitwiseOp.SUB)
        engine.inputDigit('7')
        engine.evaluate()
        assertEquals("13", engine.getFormatted(RadixMode.DEC))
    }
}
