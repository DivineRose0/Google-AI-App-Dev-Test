package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ExpressionEvaluator
import com.example.ui.theme.Vt323Family
import java.text.DecimalFormat
import kotlin.math.*

@Composable
fun FunctionGraphView(
    modifier: Modifier = Modifier
) {
    var formula by remember { mutableStateOf("sin(x)") }
    var xMin by remember { mutableDoubleStateOf(-10.0) }
    var xMax by remember { mutableDoubleStateOf(10.0) }
    var yMin by remember { mutableDoubleStateOf(-5.0) }
    var yMax by remember { mutableDoubleStateOf(5.0) }

    // Touch probe position (in world coordinates)
    var probeX by remember { mutableStateOf<Double?>(null) }
    var probeY by remember { mutableStateOf<Double?>(null) }

    val evaluator = remember { ExpressionEvaluator(isDegreeMode = false) }

    fun evalFunc(xVal: Double): Double {
        return try {
            val expr = formula.replace("x", "($xVal)").replace("X", "($xVal)")
            val res = evaluator.evaluate(expr)
            if (res.isError || res.value.isNaN() || res.value.isInfinite()) Double.NaN else res.value
        } catch (_: Exception) {
            Double.NaN
        }
    }

    // Mathematical Telemetry
    val yIntercept = remember(formula) { evalFunc(0.0) }
    val detectedRoots = remember(formula, xMin, xMax) {
        val roots = mutableListOf<Double>()
        val step = (xMax - xMin) / 100.0
        var prevX = xMin
        var prevY = evalFunc(prevX)
        for (i in 1..100) {
            val currX = xMin + i * step
            val currY = evalFunc(currX)
            if (!prevY.isNaN() && !currY.isNaN()) {
                if (prevY * currY <= 0) {
                    val root = currX - currY * (currX - prevX) / (currY - prevY)
                    roots.add(root)
                    if (roots.size >= 4) break
                }
            }
            prevX = currX
            prevY = currY
        }
        roots
    }

    val fmt = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // --- Oscilloscope Display Bezel ---
        RetroTerminalBox(
            title = "OSCILLOSCOPE // y = f(x)",
            padding = 6.dp
        ) {
            // Formula and Range Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "f(x) = $formula",
                    fontFamily = Vt323Family,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Zoom controls
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = {
                            val xRange = (xMax - xMin) * 0.35
                            val yRange = (yMax - yMin) * 0.35
                            xMin += xRange
                            xMax -= xRange
                            yMin += yRange
                            yMax -= yRange
                        },
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("[+ ZOOM]", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val xRange = (xMax - xMin) * 0.5
                            val yRange = (yMax - yMin) * 0.5
                            xMin -= xRange
                            xMax += xRange
                            yMin -= yRange
                            yMax += yRange
                        },
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("[- ZOOM]", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            xMin = -10.0
                            xMax = 10.0
                            yMin = -5.0
                            yMax = 5.0
                            probeX = null
                            probeY = null
                        },
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("[1:1]", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- CRT Canvas Graph Plotter ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF030804))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .pointerInput(formula, xMin, xMax, yMin, yMax) {
                        detectTapGestures { offset ->
                            val worldX = xMin + (offset.x / size.width.toDouble()) * (xMax - xMin)
                            val worldY = evalFunc(worldX)
                            probeX = worldX
                            probeY = worldY
                        }
                    }
                    .pointerInput(formula, xMin, xMax, yMin, yMax) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x
                            val dy = dragAmount.y
                            val xSpan = xMax - xMin
                            val ySpan = yMax - yMin
                            val deltaWorldX = (dx / size.width.toDouble()) * xSpan
                            val deltaWorldY = (dy / size.height.toDouble()) * ySpan
                            xMin -= deltaWorldX
                            xMax -= deltaWorldX
                            yMin += deltaWorldY
                            yMax += deltaWorldY

                            val probeLocalX = change.position.x
                            val worldX = xMin + (probeLocalX / size.width.toDouble()) * (xMax - xMin)
                            probeX = worldX
                            probeY = evalFunc(worldX)
                        }
                    }
            ) {
                val phosphorGreen = MaterialTheme.colorScheme.primary
                val gridColor = Color(0xFF0F2613)
                val axisColor = Color(0xFF1E4D27)
                val probeColor = MaterialTheme.colorScheme.secondary

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    if (w <= 0 || h <= 0) return@Canvas

                    // Draw grid lines
                    val xSpan = xMax - xMin
                    val ySpan = yMax - yMin

                    // Vertical grid lines
                    val gridXCount = 8
                    for (i in 0..gridXCount) {
                        val gx = (w / gridXCount) * i
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    }

                    // Horizontal grid lines
                    val gridYCount = 6
                    for (i in 0..gridYCount) {
                        val gy = (h / gridYCount) * i
                        drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    }

                    // Axes (x = 0 and y = 0)
                    if (0.0 in xMin..xMax) {
                        val axisX = (((0.0 - xMin) / xSpan) * w).toFloat()
                        drawLine(axisColor, Offset(axisX, 0f), Offset(axisX, h), strokeWidth = 2f)
                    }
                    if (0.0 in yMin..yMax) {
                        val axisY = ((1.0 - (0.0 - yMin) / ySpan) * h).toFloat()
                        drawLine(axisColor, Offset(0f, axisY), Offset(w, axisY), strokeWidth = 2f)
                    }

                    // Plot the curve y = f(x)
                    val curvePath = Path()
                    var firstPoint = true
                    val samplePoints = 200

                    for (i in 0..samplePoints) {
                        val screenX = (w / samplePoints.toFloat()) * i
                        val worldX = xMin + (screenX / w.toDouble()) * xSpan
                        val worldY = evalFunc(worldX)

                        if (!worldY.isNaN() && worldY in (yMin - ySpan)..(yMax + ySpan)) {
                            val screenY = ((1.0 - (worldY - yMin) / ySpan) * h).toFloat()
                            if (firstPoint) {
                                curvePath.moveTo(screenX, screenY)
                                firstPoint = false
                            } else {
                                curvePath.lineTo(screenX, screenY)
                            }
                        } else {
                            firstPoint = true
                        }
                    }

                    drawPath(
                        path = curvePath,
                        color = phosphorGreen,
                        style = Stroke(width = 3f)
                    )

                    // Draw Touch Probe Marker
                    if (probeX != null && probeY != null && !probeY!!.isNaN()) {
                        val pScreenX = (((probeX!! - xMin) / xSpan) * w).toFloat()
                        val pScreenY = ((1.0 - (probeY!! - yMin) / ySpan) * h).toFloat()

                        // Crosshair lines
                        drawLine(probeColor.copy(alpha = 0.5f), Offset(pScreenX, 0f), Offset(pScreenX, h), strokeWidth = 1f)
                        drawLine(probeColor.copy(alpha = 0.5f), Offset(0f, pScreenY), Offset(w, pScreenY), strokeWidth = 1f)

                        // Dot
                        drawCircle(probeColor, radius = 6f, center = Offset(pScreenX, pScreenY))
                        drawCircle(Color.Black, radius = 3f, center = Offset(pScreenX, pScreenY))
                    }
                }

                // CRT Scanlines & Telemetry Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Y_MAX: ${fmt.format(yMax)}",
                            fontFamily = Vt323Family,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (probeX != null && probeY != null && !probeY!!.isNaN()) {
                                "PROBE > (x: ${fmt.format(probeX)}, y: ${fmt.format(probeY)})"
                            } else {
                                "[DRAG/TOUCH TO INSPECT]"
                            },
                            fontFamily = Vt323Family,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "X_MIN: ${fmt.format(xMin)}",
                            fontFamily = Vt323Family,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "X_MAX: ${fmt.format(xMax)}",
                            fontFamily = Vt323Family,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mathematical Analysis Telemetry Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "f(0) = ${if (yIntercept.isNaN()) "UNDEF" else fmt.format(yIntercept)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "ROOTS: ${if (detectedRoots.isEmpty()) "NONE" else detectedRoots.joinToString(", ") { fmt.format(it) }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- Function Presets Bar ---
        Text(
            text = "SELECT FUNCTION PRESET:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val presets = listOf(
            "sin(x)",
            "cos(x)",
            "x^2 - 4",
            "x^3 - 3*x",
            "1/x",
            "sqrt(abs(x))",
            "sin(x)/x",
            "abs(x)",
            "2^x",
            "tan(x)"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(presets) { p ->
                val isSel = formula == p
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable {
                            formula = p
                            probeX = null
                            probeY = null
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "[$p]",
                        fontFamily = Vt323Family,
                        fontSize = 16.sp,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- In-App Mathematical Formula Keypad ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton("x", { formula += "x" }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 36.dp)
                CalcButton("^", { formula += "^" }, Modifier.weight(1f), ButtonType.FUNCTION, 18.sp, 36.dp)
                CalcButton("sin", { formula += "sin(" }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                CalcButton("cos", { formula += "cos(" }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                CalcButton("tan", { formula += "tan(" }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalcButton("(", { formula += "(" }, Modifier.weight(1f), ButtonType.OPERATOR, 16.sp, 36.dp)
                CalcButton(")", { formula += ")" }, Modifier.weight(1f), ButtonType.OPERATOR, 16.sp, 36.dp)
                CalcButton("sqrt", { formula += "sqrt(" }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                CalcButton("abs", { formula += "abs(" }, Modifier.weight(1f), ButtonType.FUNCTION, 14.sp, 36.dp)
                CalcButton("DEL", { if (formula.isNotEmpty()) formula = formula.dropLast(1) }, Modifier.weight(1f), ButtonType.ACTION, 14.sp, 36.dp)
                CalcButton("CLR", { formula = "" }, Modifier.weight(1f), ButtonType.ACTION, 14.sp, 36.dp)
            }
        }
    }
}
