package com.example.engine

enum class RadixMode(val base: Int, val prefix: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN")
}

enum class WordSize(val bits: Int, val mask: ULong, val displayName: String) {
    QWORD(64, 0xFFFFFFFFFFFFFFFFuL, "64-BIT QWORD"),
    DWORD(32, 0x00000000FFFFFFFFuL, "32-BIT DWORD"),
    WORD(16, 0x000000000000FFFFuL, "16-BIT WORD"),
    BYTE(8, 0x00000000000000FFuL, "8-BIT BYTE")
}

enum class BitwiseOp(val symbol: String) {
    NONE(""),
    ADD("+"),
    SUB("-"),
    MUL("×"),
    DIV("÷"),
    MOD("%"),
    AND("AND"),
    OR("OR"),
    XOR("XOR"),
    NAND("NAND"),
    NOR("NOR"),
    LSH("<<"),
    RSH(">>")
}

class ProgrammerCalculatorEngine {

    var wordSize: WordSize = WordSize.QWORD
    var currentRadix: RadixMode = RadixMode.DEC
    var currentValue: ULong = 0uL
    var storedValue: ULong = 0uL
    var pendingOp: BitwiseOp = BitwiseOp.NONE
    var isNewInput: Boolean = true

    // Visual equation breadcrumb, e.g. "5 + 3 =" or "0x1A AND 0x0F"
    var equationDisplay: String = ""

    fun maskValue(v: ULong): ULong = v and wordSize.mask

    fun formatValue(v: ULong, radix: RadixMode): String {
        val masked = maskValue(v)
        return when (radix) {
            RadixMode.HEX -> masked.toString(16).uppercase().padStart(when (wordSize) {
                WordSize.BYTE -> 2
                WordSize.WORD -> 4
                WordSize.DWORD -> 8
                WordSize.QWORD -> 16
            }, '0')
            RadixMode.DEC -> masked.toString(10)
            RadixMode.OCT -> masked.toString(8)
            RadixMode.BIN -> {
                val s = masked.toString(2).padStart(wordSize.bits, '0')
                s.chunked(4).joinToString(" ")
            }
        }
    }

    fun getFormatted(radix: RadixMode): String = formatValue(currentValue, radix)

    fun getSignedDec(): Long {
        val masked = maskValue(currentValue)
        return when (wordSize) {
            WordSize.BYTE -> masked.toByte().toLong()
            WordSize.WORD -> masked.toShort().toLong()
            WordSize.DWORD -> masked.toInt().toLong()
            WordSize.QWORD -> masked.toLong()
        }
    }

    fun inputDigit(digit: Char): Boolean {
        val validDigits = when (currentRadix) {
            RadixMode.HEX -> "0123456789ABCDEFabcdef"
            RadixMode.DEC -> "0123456789"
            RadixMode.OCT -> "01234567"
            RadixMode.BIN -> "01"
        }
        if (digit !in validDigits) return false

        val digitVal = digit.toString().toULongOrNull(currentRadix.base) ?: return false

        if (isNewInput) {
            currentValue = maskValue(digitVal)
            isNewInput = false
        } else {
            val nextVal = (currentValue * currentRadix.base.toULong()) + digitVal
            currentValue = maskValue(nextVal)
        }
        return true
    }

    fun backspace() {
        if (isNewInput) return
        val nextVal = currentValue / currentRadix.base.toULong()
        currentValue = maskValue(nextVal)
    }

    fun clear() {
        currentValue = 0uL
        storedValue = 0uL
        pendingOp = BitwiseOp.NONE
        equationDisplay = ""
        isNewInput = true
    }

    fun toggleNegate() {
        // 2's complement negation: -x = (~x + 1)
        currentValue = maskValue((currentValue.inv() + 1uL))
        isNewInput = false
    }

    fun setOp(op: BitwiseOp) {
        if (pendingOp != BitwiseOp.NONE && !isNewInput) {
            evaluate()
        }
        storedValue = currentValue
        pendingOp = op
        val valStr = formatValue(storedValue, currentRadix)
        equationDisplay = "$valStr ${op.symbol}"
        isNewInput = true
    }

    fun executeNot() {
        val oldVal = formatValue(currentValue, currentRadix)
        currentValue = maskValue(currentValue.inv())
        equationDisplay = "NOT($oldVal)"
        isNewInput = true
    }

    fun executeRol() {
        val bits = wordSize.bits
        val masked = maskValue(currentValue)
        val highBit = (masked shr (bits - 1)) and 1uL
        currentValue = maskValue((masked shl 1) or highBit)
        equationDisplay = "ROL($masked)"
    }

    fun executeRor() {
        val bits = wordSize.bits
        val masked = maskValue(currentValue)
        val lowBit = masked and 1uL
        currentValue = maskValue((masked shr 1) or (lowBit shl (bits - 1)))
        equationDisplay = "ROR($masked)"
    }

    fun toggleBit(bitIndex: Int) {
        if (bitIndex in 0 until wordSize.bits) {
            val bitMask = 1uL shl bitIndex
            currentValue = maskValue(currentValue xor bitMask)
            isNewInput = false
        }
    }

    fun evaluate() {
        if (pendingOp == BitwiseOp.NONE) return
        val a = storedValue
        val b = currentValue
        val aStr = formatValue(a, currentRadix)
        val bStr = formatValue(b, currentRadix)
        val opStr = pendingOp.symbol

        val res: ULong = when (pendingOp) {
            BitwiseOp.ADD -> a + b
            BitwiseOp.SUB -> a - b
            BitwiseOp.MUL -> a * b
            BitwiseOp.DIV -> if (b == 0uL) 0uL else a / b
            BitwiseOp.MOD -> if (b == 0uL) 0uL else a % b
            BitwiseOp.AND -> a and b
            BitwiseOp.OR -> a or b
            BitwiseOp.XOR -> a xor b
            BitwiseOp.NAND -> (a and b).inv()
            BitwiseOp.NOR -> (a or b).inv()
            BitwiseOp.LSH -> a shl (b.toInt() % wordSize.bits)
            BitwiseOp.RSH -> a shr (b.toInt() % wordSize.bits)
            BitwiseOp.NONE -> currentValue
        }

        currentValue = maskValue(res)
        equationDisplay = "$aStr $opStr $bStr ="
        pendingOp = BitwiseOp.NONE
        isNewInput = true
    }
}
