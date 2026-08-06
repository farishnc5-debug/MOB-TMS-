package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.charset.StandardCharsets

@Composable
fun ZatcaQrCodeComposable(
    tlvBase64: String,
    modifier: Modifier = Modifier
) {
    // Generate a deterministic 21x21 QR-like matrix from the base64 string
    val matrixSize = 21
    val grid = remember(tlvBase64) {
        val bytes = tlvBase64.toByteArray(StandardCharsets.UTF_8)
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }

        // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left 7x7 squares)
        fun drawFinder(startX: Int, startY: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInnerCenter = r in 2..4 && c in 2..4
                    matrix[startX + r][startY + c] = isBorder || isInnerCenter
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, matrixSize - 7)
        drawFinder(matrixSize - 7, 0)

        // Fill data cells based on bytes
        var byteIdx = 0
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Skip finder pattern areas
                val inTopLeft = r < 7 && c < 7
                val inTopRight = r < 7 && c >= matrixSize - 7
                val inBottomLeft = r >= matrixSize - 7 && c < 7

                if (!inTopLeft && !inTopRight && !inBottomLeft) {
                    val b = if (bytes.isNotEmpty()) bytes[byteIdx % bytes.size].toInt() else 0
                    matrix[r][c] = (b + r * 3 + c * 7) % 2 == 0
                    byteIdx++
                }
            }
        }
        matrix
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp)
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val cellSize = size.width / matrixSize
                for (r in 0 until matrixSize) {
                    for (c in 0 until matrixSize) {
                        if (grid[r][c]) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(c * cellSize, r * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "ZATCA TLV QR",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
