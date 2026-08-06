package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
fun CrmScreen(
    clients: List<ClientEntity>,
    selectedHub: String,
    selectedTruckType: String,
    tariffOverrides: List<TariffOverride>,
    settings: AppSettings,
    onHubSelected: (String) -> Unit,
    onTruckTypeSelected: (String) -> Unit,
    onCreateQuotation: (QuotationEntity) -> Unit,
    onDispatchShipment: (String, String, String, Int, String, String, String, Int, Int, Double, Double) -> Unit,
    onPreviewQuote: (QuotationEntity) -> Unit
) {
    var selectedClient by remember { mutableStateOf<ClientEntity?>(clients.firstOrNull()) }
    var clientName by remember(selectedClient) { mutableStateOf(selectedClient?.name ?: "Al-Manar Fresh Foods LLC") }
    var contactPerson by remember(selectedClient) { mutableStateOf(selectedClient?.contact ?: "Mr. Tariq Al-Otaibi") }
    var phone by remember(selectedClient) { mutableStateOf(selectedClient?.phone ?: "+966 50 123 4567") }
    var email by remember(selectedClient) { mutableStateOf(selectedClient?.email ?: "tariq@almanarfoods.sa") }
    var validUntil by remember { mutableStateOf("30 Days (30 يوماً)") }

    var clientExpanded by remember { mutableStateOf(false) }
    var hubExpanded by remember { mutableStateOf(false) }
    var truckExpanded by remember { mutableStateOf(false) }

    // Line items state
    var lineItems by remember {
        mutableStateOf(
            listOf(
                LineItemData(destination = "Riyadh", distance = 950, transitTime = "11 - 14 Hours", extraDrops = 0, waitingHours = 2)
            )
        )
    }

    val markupMult = 1.0 + (settings.markup / 100.0)

    // Calculate live totals
    var baseTotal = 0.0
    var extraDropsTotal = 0.0
    var waitingTotal = 0.0

    val processedLineItems = lineItems.map { item ->
        val defaultRoute = DefaultData.defaultDestinations.find { it.destination == item.destination }
        val defaultCost = defaultRoute?.defaultCost ?: 1200.0
        val key = "${selectedHub}_${selectedTruckType}_${item.destination}"
        val overrideCost = tariffOverrides.find { it.id == key }?.cost ?: defaultCost
        val autoBase = Math.round(overrideCost * markupMult).toDouble()

        val effectiveBase = item.manualRate ?: autoBase
        baseTotal += effectiveBase

        val dropsCharge = item.extraDrops * settings.dropRate
        extraDropsTotal += dropsCharge

        val billableWaiting = Math.max(0.0, item.waitingHours - settings.freeWaitingHours)
        val waitCharge = if (billableWaiting > 0) {
            val raw = billableWaiting * settings.detentionRate
            if (settings.detentionCap > 0) Math.min(raw, settings.detentionCap) else raw
        } else 0.0
        waitingTotal += waitCharge

        val lineTot = effectiveBase + dropsCharge + waitCharge
        item.copy(lineTotal = lineTot)
    }

    val subtotal = baseTotal + extraDropsTotal + waitingTotal
    val vat = subtotal * (settings.vatRate / 100.0)
    val grandTotal = subtotal + vat

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Client & Fleet Header Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Client & Fleet Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    DropdownMenuItem(text = { Text("$hub Hub") }, onClick = { onHubSelected(hub); hubExpanded = false })
                                }
                            }
                        }

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
                                    DropdownMenuItem(text = { Text(truck) }, onClick = { onTruckTypeSelected(truck); truckExpanded = false })
                                }
                            }
                        }
                    }

                    // Client Selector
                    if (clients.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = clientExpanded,
                            onExpandedChange = { clientExpanded = !clientExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClient?.name ?: "Select Client",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Registered Client") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(expanded = clientExpanded, onDismissRequest = { clientExpanded = false }) {
                                clients.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedClient = c; clientExpanded = false })
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client Company Name") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("Contact Person") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Line Items Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("2. Shipment Line Items (${lineItems.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Button(
                    onClick = {
                        lineItems = lineItems + LineItemData(destination = "Riyadh", distance = 950, transitTime = "11 - 14 Hours")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Line Item")
                }
            }
        }

        // Line Items List
        itemsIndexed(lineItems) { index, item ->
            var destExpanded by remember { mutableStateOf(false) }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Line Item #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        if (lineItems.size > 1) {
                            IconButton(onClick = { lineItems = lineItems.filterIndexed { i, _ -> i != index } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = destExpanded,
                        onExpandedChange = { destExpanded = !destExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "${item.destination} (${item.distance} km)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Destination") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = destExpanded, onDismissRequest = { destExpanded = false }) {
                            DefaultData.defaultDestinations.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text("${d.destination} (${d.destinationAr} - ${d.distance} km)") },
                                    onClick = {
                                        val updated = lineItems.toMutableList()
                                        updated[index] = item.copy(
                                            destination = d.destination,
                                            distance = d.distance,
                                            transitTime = DefaultData.getEstimatedTransitTime(selectedHub, d.destination, d.distance)
                                        )
                                        lineItems = updated
                                        destExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = item.extraDrops.toString(),
                            onValueChange = {
                                val drops = it.toIntOrNull() ?: 0
                                val updated = lineItems.toMutableList()
                                updated[index] = item.copy(extraDrops = drops)
                                lineItems = updated
                            },
                            label = { Text("Extra Drops") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = item.waitingHours.toString(),
                            onValueChange = {
                                val hrs = it.toIntOrNull() ?: 2
                                val updated = lineItems.toMutableList()
                                updated[index] = item.copy(waitingHours = hrs)
                                lineItems = updated
                            },
                            label = { Text("Waiting Hrs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Totals Card
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Tariff Total:")
                        Text("SAR ${String.format(Locale.US, "%.0f", baseTotal)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Extra Drop Charges:")
                        Text("SAR ${String.format(Locale.US, "%.0f", extraDropsTotal)}", fontFamily = FontFamily.Monospace)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Waiting Charges:")
                        Text("SAR ${String.format(Locale.US, "%.0f", waitingTotal)}", fontFamily = FontFamily.Monospace)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal (ex-VAT):", fontWeight = FontWeight.Bold)
                        Text("SAR ${String.format(Locale.US, "%.2f", subtotal)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("15% ZATCA VAT:")
                        Text("SAR ${String.format(Locale.US, "%.2f", vat)}", fontFamily = FontFamily.Monospace)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total (Incl. VAT):", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("SAR ${String.format(Locale.US, "%.2f", grandTotal)}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val dummyQuote = QuotationEntity(
                            refNo = "PREVIEW",
                            date = "Today",
                            originHub = selectedHub,
                            truckType = selectedTruckType,
                            clientName = clientName,
                            contactPerson = contactPerson,
                            phone = phone,
                            email = email,
                            validUntil = validUntil,
                            subtotal = subtotal,
                            vat = vat,
                            grandTotal = grandTotal,
                            lineItemsJson = "[]"
                        )
                        onPreviewQuote(dummyQuote)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview")
                }

                Button(
                    onClick = {
                        val firstItem = lineItems.firstOrNull() ?: LineItemData("Riyadh", 950, "11 - 14 Hours")
                        val defaultCost = DefaultData.defaultDestinations.find { it.destination == firstItem.destination }?.defaultCost ?: 1200.0
                        val vendor = DefaultData.initialVendors.first()

                        onDispatchShipment(
                            selectedHub,
                            selectedTruckType,
                            firstItem.destination,
                            firstItem.distance,
                            clientName,
                            contactPerson,
                            phone,
                            firstItem.extraDrops,
                            firstItem.waitingHours,
                            defaultCost,
                            settings.markup
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Dispatch")
                }
            }
        }
    }
}
