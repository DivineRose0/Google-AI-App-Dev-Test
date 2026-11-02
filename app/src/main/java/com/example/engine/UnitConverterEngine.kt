package com.example.engine

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

enum class UnitCategory(val displayName: String, val code: String) {
    LENGTH("Length", "LEN"),
    MASS("Mass & Weight", "MASS"),
    TEMPERATURE("Temperature", "TEMP"),
    AREA("Area", "AREA"),
    VOLUME("Volume & Capacity", "VOL"),
    COOKING("Cooking & Kitchen", "COOK"),
    TIME("Time Duration", "TIME"),
    SPEED("Speed & Velocity", "SPD"),
    DIGITAL("Digital Data", "DATA"),
    DATA_TRANSFER("Data Transfer Rate", "RATE"),
    PRESSURE("Pressure", "PRES"),
    ENERGY("Energy & Work", "ENGY"),
    POWER("Power", "POW"),
    FORCE("Force", "FRC"),
    TORQUE("Torque", "TRQ"),
    ANGLE("Angle & Geometry", "ANG"),
    FREQUENCY("Frequency", "FREQ"),
    ELECTRIC_CURRENT("Electric Current", "ELEC"),
    FUEL("Fuel Economy", "FUEL")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0,
    val isCustom: Boolean = false
)

object UnitConverterEngine {

    val categories: List<UnitCategory> = UnitCategory.values().toList()

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> = when (category) {
        UnitCategory.LENGTH -> listOf(
            ConversionUnit("m", "Meter", "m", 1.0),
            ConversionUnit("km", "Kilometer", "km", 1000.0),
            ConversionUnit("cm", "Centimeter", "cm", 0.01),
            ConversionUnit("mm", "Millimeter", "mm", 0.001),
            ConversionUnit("um", "Micrometer", "µm", 1e-6),
            ConversionUnit("nm", "Nanometer", "nm", 1e-9),
            ConversionUnit("pm", "Picometer", "pm", 1e-12),
            ConversionUnit("in", "Inch", "in", 0.0254),
            ConversionUnit("ft", "Foot", "ft", 0.3048),
            ConversionUnit("yd", "Yard", "yd", 0.9144),
            ConversionUnit("mi", "Mile", "mi", 1609.344),
            ConversionUnit("nmi", "Nautical Mile", "nmi", 1852.0),
            ConversionUnit("fathom", "Fathom", "ftm", 1.8288),
            ConversionUnit("rod", "Rod / Pole", "rd", 5.0292),
            ConversionUnit("chain", "Chain", "ch", 20.1168),
            ConversionUnit("furlong", "Furlong", "fur", 201.168),
            ConversionUnit("mil", "Mil (Thou)", "mil", 0.0000254),
            ConversionUnit("au", "Astronomical Unit", "AU", 1.495978707e11),
            ConversionUnit("ly", "Light Year", "ly", 9.4607304725808e15),
            ConversionUnit("pc", "Parsec", "pc", 3.085677581e16)
        )
        UnitCategory.MASS -> listOf(
            ConversionUnit("kg", "Kilogram", "kg", 1.0),
            ConversionUnit("g", "Gram", "g", 0.001),
            ConversionUnit("mg", "Milligram", "mg", 1e-6),
            ConversionUnit("ug", "Microgram", "µg", 1e-9),
            ConversionUnit("t", "Metric Ton", "t", 1000.0),
            ConversionUnit("lb", "Pound", "lb", 0.45359237),
            ConversionUnit("oz", "Ounce", "oz", 0.028349523125),
            ConversionUnit("ozt", "Troy Ounce", "oz t", 0.0311034768),
            ConversionUnit("st", "Stone", "st", 6.35029318),
            ConversionUnit("ct", "Carat", "ct", 0.0002),
            ConversionUnit("grain", "Grain", "gr", 0.00006479891),
            ConversionUnit("ton_us", "Short Ton (US)", "ton US", 907.18474),
            ConversionUnit("ton_uk", "Long Ton (UK)", "ton UK", 1016.0469088),
            ConversionUnit("slug", "Slug", "slug", 14.5939029),
            ConversionUnit("amu", "Atomic Mass Unit", "u", 1.66053906660e-27)
        )
        UnitCategory.TEMPERATURE -> listOf(
            ConversionUnit("c", "Celsius", "°C", isCustom = true),
            ConversionUnit("f", "Fahrenheit", "°F", isCustom = true),
            ConversionUnit("k", "Kelvin", "K", isCustom = true),
            ConversionUnit("r", "Rankine", "°R", isCustom = true)
        )
        UnitCategory.AREA -> listOf(
            ConversionUnit("sqm", "Square Meter", "m²", 1.0),
            ConversionUnit("sqkm", "Square Kilometer", "km²", 1e6),
            ConversionUnit("sqcm", "Square Centimeter", "cm²", 1e-4),
            ConversionUnit("sqmm", "Square Millimeter", "mm²", 1e-6),
            ConversionUnit("sqin", "Square Inch", "in²", 0.00064516),
            ConversionUnit("sqft", "Square Foot", "ft²", 0.09290304),
            ConversionUnit("sqyd", "Square Yard", "yd²", 0.83612736),
            ConversionUnit("sqmi", "Square Mile", "mi²", 2589988.110336),
            ConversionUnit("acre", "Acre", "ac", 4046.8564224),
            ConversionUnit("ha", "Hectare", "ha", 10000.0),
            ConversionUnit("barn", "Barn", "b", 1e-28)
        )
        UnitCategory.VOLUME -> listOf(
            ConversionUnit("l", "Liter", "L", 1.0),
            ConversionUnit("ml", "Milliliter", "mL", 0.001),
            ConversionUnit("m3", "Cubic Meter", "m³", 1000.0),
            ConversionUnit("cm3", "Cubic Centimeter", "cm³", 0.001),
            ConversionUnit("cuin", "Cubic Inch", "in³", 0.016387064),
            ConversionUnit("cuft", "Cubic Foot", "ft³", 28.316846592),
            ConversionUnit("cuyd", "Cubic Yard", "yd³", 764.554857984),
            ConversionUnit("gal_us", "Gallon (US)", "gal", 3.785411784),
            ConversionUnit("gal_uk", "Gallon (UK)", "gal UK", 4.54609),
            ConversionUnit("qt_us", "Quart (US)", "qt", 0.946352946),
            ConversionUnit("pt_us", "Pint (US)", "pt", 0.473176473),
            ConversionUnit("floz_us", "Fluid Ounce (US)", "fl oz", 0.0295735295625),
            ConversionUnit("cup_us", "Cup (US)", "cup", 0.2365882365),
            ConversionUnit("bbl_oil", "Oil Barrel", "bbl", 158.987294928)
        )
        UnitCategory.COOKING -> listOf(
            ConversionUnit("ml", "Milliliter", "mL", 1.0),
            ConversionUnit("l", "Liter", "L", 1000.0),
            ConversionUnit("tsp", "Teaspoon (US)", "tsp", 4.92892),
            ConversionUnit("tbsp", "Tablespoon (US)", "tbsp", 14.7868),
            ConversionUnit("dessertspoon", "Dessertspoon", "dstspn", 10.0),
            ConversionUnit("cup", "Cup (US)", "cup", 236.588),
            ConversionUnit("floz", "Fluid Ounce (US)", "fl oz", 29.5735),
            ConversionUnit("pt", "Pint (US)", "pt", 473.176),
            ConversionUnit("qt", "Quart (US)", "qt", 946.353),
            ConversionUnit("gal", "Gallon (US)", "gal", 3785.41),
            ConversionUnit("pinch", "Pinch", "pinch", 0.308),
            ConversionUnit("dash", "Dash", "dash", 0.616),
            ConversionUnit("drop", "Drop", "gtt", 0.05)
        )
        UnitCategory.TIME -> listOf(
            ConversionUnit("sec", "Second", "s", 1.0),
            ConversionUnit("ms", "Millisecond", "ms", 0.001),
            ConversionUnit("us", "Microsecond", "µs", 1e-6),
            ConversionUnit("ns", "Nanosecond", "ns", 1e-9),
            ConversionUnit("min", "Minute", "min", 60.0),
            ConversionUnit("hr", "Hour", "h", 3600.0),
            ConversionUnit("day", "Day", "d", 86400.0),
            ConversionUnit("wk", "Week", "wk", 604800.0),
            ConversionUnit("mo", "Month (30.4375 d)", "mo", 2629800.0),
            ConversionUnit("yr", "Year (365.25 d)", "yr", 31557600.0),
            ConversionUnit("decade", "Decade", "dec", 315576000.0),
            ConversionUnit("century", "Century", "cent", 3155760000.0)
        )
        UnitCategory.SPEED -> listOf(
            ConversionUnit("ms", "Meter / second", "m/s", 1.0),
            ConversionUnit("kmh", "Kilometer / hour", "km/h", 1.0 / 3.6),
            ConversionUnit("mph", "Mile / hour", "mph", 0.44704),
            ConversionUnit("knot", "Knot", "kn", 0.514444444),
            ConversionUnit("fts", "Foot / second", "ft/s", 0.3048),
            ConversionUnit("mach", "Mach (air STP)", "Ma", 340.29),
            ConversionUnit("c", "Speed of Light", "c", 299792458.0)
        )
        UnitCategory.DIGITAL -> listOf(
            ConversionUnit("bit", "Bit", "b", 0.125),
            ConversionUnit("nibble", "Nibble (4 bits)", "nibble", 0.5),
            ConversionUnit("byte", "Byte", "B", 1.0),
            ConversionUnit("kb", "Kilobyte (10³ B)", "KB", 1e3),
            ConversionUnit("kib", "Kibibyte (2¹⁰ B)", "KiB", 1024.0),
            ConversionUnit("mb", "Megabyte (10⁶ B)", "MB", 1e6),
            ConversionUnit("mib", "Mebibyte (2²⁰ B)", "MiB", 1048576.0),
            ConversionUnit("gb", "Gigabyte (10⁹ B)", "GB", 1e9),
            ConversionUnit("gib", "Gibibyte (2³⁰ B)", "GiB", 1073741824.0),
            ConversionUnit("tb", "Terabyte (10¹² B)", "TB", 1e12),
            ConversionUnit("tib", "Tebibyte (2⁴⁰ B)", "TiB", 1099511627776.0),
            ConversionUnit("pb", "Petabyte (10¹⁵ B)", "PB", 1e15),
            ConversionUnit("pib", "Pebibyte (2⁵⁰ B)", "PiB", 1125899906842624.0),
            ConversionUnit("eb", "Exabyte (10¹⁸ B)", "EB", 1e18)
        )
        UnitCategory.DATA_TRANSFER -> listOf(
            ConversionUnit("bps", "Bit / second", "bps", 1.0),
            ConversionUnit("kbps", "Kilobit / s", "Kbps", 1e3),
            ConversionUnit("mbps", "Megabit / s", "Mbps", 1e6),
            ConversionUnit("gbps", "Gigabit / s", "Gbps", 1e9),
            ConversionUnit("tbps", "Terabit / s", "Tbps", 1e12),
            ConversionUnit("bytesec", "Byte / second", "B/s", 8.0),
            ConversionUnit("kbytesec", "Kilobyte / s", "KB/s", 8e3),
            ConversionUnit("mbytesec", "Megabyte / s", "MB/s", 8e6),
            ConversionUnit("gbytesec", "Gigabyte / s", "GB/s", 8e9)
        )
        UnitCategory.PRESSURE -> listOf(
            ConversionUnit("pa", "Pascal", "Pa", 1.0),
            ConversionUnit("hpa", "Hectopascal", "hPa", 100.0),
            ConversionUnit("kpa", "Kilopascal", "kPa", 1000.0),
            ConversionUnit("mpa", "Megapascal", "MPa", 1e6),
            ConversionUnit("bar", "Bar", "bar", 100000.0),
            ConversionUnit("mbar", "Millibar", "mbar", 100.0),
            ConversionUnit("psi", "Pound/sq inch", "psi", 6894.757293168),
            ConversionUnit("ksi", "Kilopound/sq in", "ksi", 6894757.293168),
            ConversionUnit("atm", "Standard Atmosphere", "atm", 101325.0),
            ConversionUnit("torr", "Torr (mmHg)", "mmHg", 133.322387415),
            ConversionUnit("inhg", "Inch of Mercury", "inHg", 3386.388666667)
        )
        UnitCategory.ENERGY -> listOf(
            ConversionUnit("j", "Joule", "J", 1.0),
            ConversionUnit("kj", "Kilojoule", "kJ", 1000.0),
            ConversionUnit("mj", "Megajoule", "MJ", 1e6),
            ConversionUnit("cal", "Calorie", "cal", 4.184),
            ConversionUnit("kcal", "Kilocalorie", "kcal", 4184.0),
            ConversionUnit("wh", "Watt-hour", "Wh", 3600.0),
            ConversionUnit("kwh", "Kilowatt-hour", "kWh", 3.6e6),
            ConversionUnit("mwh", "Megawatt-hour", "MWh", 3.6e9),
            ConversionUnit("btu", "BTU", "BTU", 1055.05585262),
            ConversionUnit("ev", "Electron-volt", "eV", 1.602176634e-19),
            ConversionUnit("mev", "Megaelectron-volt", "MeV", 1.602176634e-13),
            ConversionUnit("erg", "Erg", "erg", 1e-7),
            ConversionUnit("ftlbf", "Foot-pound", "ft·lbf", 1.3558179483314)
        )
        UnitCategory.POWER -> listOf(
            ConversionUnit("w", "Watt", "W", 1.0),
            ConversionUnit("mw", "Milliwatt", "mW", 0.001),
            ConversionUnit("kw", "Kilowatt", "kW", 1000.0),
            ConversionUnit("megaw", "Megawatt", "MW", 1e6),
            ConversionUnit("gw", "Gigawatt", "GW", 1e9),
            ConversionUnit("hp", "Horsepower (Mechanical)", "hp", 745.69987158227),
            ConversionUnit("hp_m", "Horsepower (Metric)", "hp(M)", 735.49875),
            ConversionUnit("btuh", "BTU / hour", "BTU/h", 0.29307107)
        )
        UnitCategory.FORCE -> listOf(
            ConversionUnit("n", "Newton", "N", 1.0),
            ConversionUnit("kn", "Kilonewton", "kN", 1000.0),
            ConversionUnit("mn", "Meganewton", "MN", 1e6),
            ConversionUnit("dyn", "Dyne", "dyn", 1e-5),
            ConversionUnit("lbf", "Pound-force", "lbf", 4.4482216152605),
            ConversionUnit("kgf", "Kilogram-force", "kgf", 9.80665),
            ConversionUnit("ozf", "Ounce-force", "ozf", 0.27801385)
        )
        UnitCategory.TORQUE -> listOf(
            ConversionUnit("nm", "Newton-meter", "N·m", 1.0),
            ConversionUnit("knm", "Kilonewton-meter", "kN·m", 1000.0),
            ConversionUnit("lbfft", "Pound-foot", "lbf·ft", 1.3558179483314),
            ConversionUnit("lbfin", "Pound-inch", "lbf·in", 0.112984829),
            ConversionUnit("kgfm", "Kilogram-meter", "kgf·m", 9.80665)
        )
        UnitCategory.ANGLE -> listOf(
            ConversionUnit("deg", "Degree", "°", 1.0),
            ConversionUnit("rad", "Radian", "rad", 180.0 / Math.PI),
            ConversionUnit("grad", "Gradian", "grad", 0.9),
            ConversionUnit("arcmin", "Arcminute", "arcmin", 1.0 / 60.0),
            ConversionUnit("arcsec", "Arcsecond", "arcsec", 1.0 / 3600.0),
            ConversionUnit("rev", "Revolution / Turn", "rev", 360.0),
            ConversionUnit("mrad", "Milliradian", "mrad", (180.0 / Math.PI) * 0.001)
        )
        UnitCategory.FREQUENCY -> listOf(
            ConversionUnit("hz", "Hertz", "Hz", 1.0),
            ConversionUnit("khz", "Kilohertz", "kHz", 1e3),
            ConversionUnit("mhz", "Megahertz", "MHz", 1e6),
            ConversionUnit("ghz", "Gigahertz", "GHz", 1e9),
            ConversionUnit("thz", "Terahertz", "THz", 1e12),
            ConversionUnit("rpm", "Revolutions / minute", "RPM", 1.0 / 60.0),
            ConversionUnit("radsec", "Radian / second", "rad/s", 1.0 / (2.0 * Math.PI))
        )
        UnitCategory.ELECTRIC_CURRENT -> listOf(
            ConversionUnit("a", "Ampere", "A", 1.0),
            ConversionUnit("ma", "Milliampere", "mA", 0.001),
            ConversionUnit("ua", "Microampere", "µA", 1e-6),
            ConversionUnit("ka", "Kiloampere", "kA", 1000.0),
            ConversionUnit("abamp", "Abampere", "abA", 10.0)
        )
        UnitCategory.FUEL -> listOf(
            ConversionUnit("mpg_us", "Miles / Gallon (US)", "MPG (US)", isCustom = true),
            ConversionUnit("mpg_imp", "Miles / Gallon (UK)", "MPG (UK)", isCustom = true),
            ConversionUnit("l100km", "Liters / 100km", "L/100km", isCustom = true),
            ConversionUnit("kml", "Kilometers / Liter", "km/L", isCustom = true)
        )
    }

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit, category: UnitCategory): Double {
        if (from.id == to.id) return value

        if (category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.id, to.id)
        }

        if (category == UnitCategory.FUEL) {
            return convertFuel(value, from.id, to.id)
        }

        val baseValue = value * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        val kelvin = when (fromId) {
            "c" -> value + 273.15
            "f" -> (value - 32.0) * (5.0 / 9.0) + 273.15
            "k" -> value
            "r" -> value * (5.0 / 9.0)
            else -> value
        }
        return when (toId) {
            "c" -> kelvin - 273.15
            "f" -> (kelvin - 273.15) * (9.0 / 5.0) + 32.0
            "k" -> kelvin
            "r" -> kelvin * (9.0 / 5.0)
            else -> kelvin
        }
    }

    private fun convertFuel(value: Double, fromId: String, toId: String): Double {
        if (value <= 0) return 0.0
        val kml = when (fromId) {
            "kml" -> value
            "mpg_us" -> value * 0.425143707
            "mpg_imp" -> value * 0.35400619
            "l100km" -> 100.0 / value
            else -> value
        }
        return when (toId) {
            "kml" -> kml
            "mpg_us" -> kml / 0.425143707
            "mpg_imp" -> kml / 0.35400619
            "l100km" -> 100.0 / kml
            else -> kml
        }
    }

    fun formatConvertedValue(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0"
        val absVal = abs(value)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
            decimalSeparator = '.'
        }

        return if (absVal >= 1e9 || (absVal < 1e-4 && absVal > 0)) {
            DecimalFormat("0.######E0", symbols).format(value)
        } else {
            val df = DecimalFormat("#,##0.######", symbols)
            df.format(value)
        }
    }

    // Lookup unit by symbol or id
    fun findUnit(query: String): Pair<ConversionUnit, UnitCategory>? {
        val q = query.trim().lowercase()
        for (cat in categories) {
            val unitList = getUnitsForCategory(cat)
            for (u in unitList) {
                if (u.id.equals(q, true) || u.symbol.equals(q, true) || u.name.equals(q, true)) {
                    return Pair(u, cat)
                }
            }
        }
        return null
    }
}
