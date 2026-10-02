package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DefaultData
import com.example.ui.viewmodel.DailyVolume
import com.example.ui.viewmodel.ForecastDay
import com.example.ui.viewmodel.ForecastUiState
import java.util.Locale

@Composable
fun ForecastScreen(
    forecastState: ForecastUiState,
    onRunForecast: (hub: String?, horizonDays: Int) -> Unit
) {
    var selectedHub by remember { mutableStateOf<String?>(null) }
    var horizonDays by remember { mutableIntStateOf(14) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Demand Forecast", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                "Predicts future daily shipment volume from your dispatch history, powered by Google's TimesFM forecasting model.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hub", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedHub == null,
                            onClick = { selectedHub = null },
                            label = { Text("All Hubs", fontSize = 11.sp) }
                        )
                        DefaultData.hubs.forEach { hub ->
                            FilterChip(
                                selected = selectedHub == hub,
                                onClick = { selectedHub = hub },
                                label = { Text(hub, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Forecast Horizon", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 14, 30).forEach { days ->
                            FilterChip(
                                selected = horizonDays == days,
                                onClick = { horizonDays = days },
                                label = { Text("$days days", fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = { onRunForecast(selectedHub, horizonDays) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = forecastState !is ForecastUiState.Loading
                    ) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (forecastState is ForecastUiState.Loading) "Forecasting..." else "Run Forecast")
                    }
                }
            }
        }

        when (forecastState) {
            is ForecastUiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text("Calling forecast-service (first run may need to download the model)...", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            is ForecastUiState.Error -> item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Forecast Failed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Text(forecastState.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            is ForecastUiState.Success -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Historical vs. Forecast Volume", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            ForecastChart(
                                history = forecastState.history,
                                forecast = forecastState.forecast,
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                LegendDot(color = MaterialTheme.colorScheme.outline, label = "History")
                                LegendDot(color = MaterialTheme.colorScheme.primary, label = "Forecast (10-90% range)")
                            }
                        }
                    }
                }

                item {
                    Text("Forecast Detail", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                items(forecastState.forecast) { day ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(day.isoDate, fontSize = 12.sp)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "~${String.format(Locale.US, "%.1f", day.point)} shipments",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Range: ${String.format(Locale.US, "%.1f", day.low)} - ${String.format(Locale.US, "%.1f", day.high)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            ForecastUiState.Idle -> item {
                Text(
                    "Choose a hub and horizon, then run a forecast to see predicted shipment volume.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, shape = androidx.compose.foundation.shape.CircleShape))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ForecastChart(
    history: List<DailyVolume>,
    forecast: List<ForecastDay>,
    modifier: Modifier = Modifier
) {
    val historyColor = MaterialTheme.colorScheme.outline
    val forecastColor = MaterialTheme.colorScheme.primary
    val rangeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)

    // Keep the chart readable by only plotting the most recent history points.
    val recentHistory = history.takeLast(30)
    val maxValue = (recentHistory.maxOfOrNull { it.count } ?: 0)
        .coerceAtLeast(forecast.maxOfOrNull { it.high }?.toInt() ?: 0)
        .coerceAtLeast(1)
        .toFloat()

    Canvas(modifier = modifier) {
        val totalBars = recentHistory.size + forecast.size
        if (totalBars == 0) return@Canvas

        val barSlot = size.width / totalBars
        val barWidth = barSlot * 0.6f

        recentHistory.forEachIndexed { index, day ->
            val barHeight = (day.count / maxValue) * size.height
            val x = index * barSlot + (barSlot - barWidth) / 2
            drawRect(
                color = historyColor,
                topLeft = Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }

        forecast.forEachIndexed { index, day ->
            val slotIndex = recentHistory.size + index
            val x = slotIndex * barSlot + barSlot / 2

            val lowY = size.height - (day.low.toFloat() / maxValue) * size.height
            val highY = size.height - (day.high.toFloat() / maxValue) * size.height
            drawLine(
                color = rangeColor,
                start = Offset(x, lowY.coerceIn(0f, size.height)),
                end = Offset(x, highY.coerceIn(0f, size.height)),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )

            val pointY = size.height - (day.point.toFloat() / maxValue) * size.height
            drawCircle(color = forecastColor, radius = barWidth / 3.5f, center = Offset(x, pointY.coerceIn(0f, size.height)))
        }
    }
}
