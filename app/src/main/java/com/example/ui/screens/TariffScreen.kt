package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TariffScreen(
    selectedHub: String,
    selectedTruckType: String,
    isRateEditMode: Boolean,
    tariffOverrides: List<TariffOverride>,
    settings: AppSettings,
    onHubSelected: (String) -> Unit,
    onTruckTypeSelected: (String) -> Unit,
    onToggleEditMode: () -> Unit,
    onSaveCostOverride: (String, String, String, Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var hubExpanded by remember { mutableStateOf(false) }
    var truckExpanded by remember { mutableStateOf(false) }

    val destinations = DefaultData.defaultDestinations.filter {
        searchQuery.isBlank() || it.destination.contains(searchQuery, ignoreCase = true) || it.destinationAr.contains(searchQuery)
    }

    val markupMult = 1.0 + (settings.markup / 100.0)

    // Calculate averages
    var totalDist = 0
    var totalCost = 0.0
    var totalSelling = 0.0

    DefaultData.defaultDestinations.forEach { d ->
        val key = "${selectedHub}_${selectedTruckType}_${d.destination}"
        val override = tariffOverrides.find { it.id == key }
        val cost = override?.cost ?: d.defaultCost
        val selling = Math.round(cost * markupMult).toDouble()

        totalDist += d.distance
        totalCost += cost
        totalSelling += selling
    }

    val count = DefaultData.defaultDestinations.size.coerceAtLeast(1)
    val avgDist = totalDist / count
    val avgCost = totalCost / count
    val avgSelling = totalSelling / count
    val avgProfit = avgSelling - avgCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selectors Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Hub Selector
                    ExposedDropdownMenuBox(
                        expanded = hubExpanded,
                        onExpandedChange = { hubExpanded = !hubExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "$selectedHub Hub",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Origin Hub") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hubExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = hubExpanded, onDismissRequest = { hubExpanded = false }) {
                            DefaultData.hubs.forEach { hub ->
                                DropdownMenuItem(
                                    text = { Text("$hub Hub") },
                                    onClick = {
                                        onHubSelected(hub)
                                        hubExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Truck Selector
                    ExposedDropdownMenuBox(
                        expanded = truckExpanded,
                        onExpandedChange = { truckExpanded = !truckExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedTruckType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Truck Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = truckExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = truckExpanded, onDismissRequest = { truckExpanded = false }) {
                            DefaultData.truckTypes.forEach { truck ->
                                DropdownMenuItem(
                                    text = { Text(truck) },
                                    onClick = {
                                        onTruckTypeSelected(truck)
                                        truckExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Markup: +${String.format(Locale.US, "%.0f", settings.markup)}%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(onClick = onToggleEditMode, colors = ButtonDefaults.buttonColors(containerColor = if (isRateEditMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)) {
                        Icon(if (isRateEditMode) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRateEditMode) "Done Editing" else "Edit Hub Rates")
                    }
                }
            }
        }

        // Summary Bar
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG DIST", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("$avgDist km", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG COST", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("SAR ${String.format(Locale.US, "%.0f", avgCost)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG SELLING", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("SAR ${String.format(Locale.US, "%.0f", avgSelling)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AVG PROFIT", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("SAR ${String.format(Locale.US, "%.0f", avgProfit)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Search Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search destination city...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        // Route Matrix Items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(destinations) { dest ->
                val key = "${selectedHub}_${selectedTruckType}_${dest.destination}"
                val override = tariffOverrides.find { it.id == key }
                val currentCost = override?.cost ?: dest.defaultCost
                val sellingRate = Math.round(currentCost * markupMult).toDouble()
                val profit = sellingRate - currentCost
                val transitTime = DefaultData.getEstimatedTransitTime(selectedHub, dest.destination, dest.distance)

                var editCostText by remember(currentCost) { mutableStateOf(currentCost.toString()) }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dest.destination, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(dest.destinationAr, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("${dest.distance} km • $transitTime", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }

                        if (isRateEditMode) {
                            OutlinedTextField(
                                value = editCostText,
                                onValueChange = {
                                    editCostText = it
                                    val newCost = it.toDoubleOrNull() ?: 0.0
                                    onSaveCostOverride(selectedHub, selectedTruckType, dest.destination, newCost)
                                },
                                label = { Text("Cost SAR") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(100.dp)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("SAR ${String.format(Locale.US, "%.0f", sellingRate)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                                Text("Cost SAR ${String.format(Locale.US, "%.0f", currentCost)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text("+Profit SAR ${String.format(Locale.US, "%.0f", profit)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}
