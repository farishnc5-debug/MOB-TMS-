package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.util.Locale

import com.example.ui.components.ExportDataDialog
import com.example.util.CsvExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationsScreen(
    operations: List<OperationEntity>,
    vendors: List<VendorEntity>,
    settings: AppSettings,
    statusStages: List<String>,
    onUpdateStatus: (OperationEntity, Int) -> Unit,
    onConvertInvoice: (OperationEntity) -> Unit,
    onViewWaybill: (OperationEntity) -> Unit,
    onDeleteOperation: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var filterExpanded by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }

    val filteredOps = operations.filter { op ->
        val matchesQuery = searchQuery.isBlank() ||
                op.id.contains(searchQuery, ignoreCase = true) ||
                op.clientName.contains(searchQuery, ignoreCase = true) ||
                op.vendorName.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (statusFilter) {
            "ALL" -> true
            "OPEN" -> op.status != "Finished"
            else -> op.status == statusFilter
        }
        matchesQuery && matchesStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active Operations & Waybills (${filteredOps.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FilledTonalButton(
                onClick = { showExportDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Excel/CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Search & Filter Header
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ID, client or vendor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f)
            )

            ExposedDropdownMenuBox(
                expanded = filterExpanded,
                onExpandedChange = { filterExpanded = !filterExpanded },
                modifier = Modifier.width(140.dp)
            ) {
                OutlinedTextField(
                    value = statusFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    DropdownMenuItem(text = { Text("ALL") }, onClick = { statusFilter = "ALL"; filterExpanded = false })
                    DropdownMenuItem(text = { Text("OPEN") }, onClick = { statusFilter = "OPEN"; filterExpanded = false })
                    statusStages.forEach { st ->
                        DropdownMenuItem(text = { Text(st) }, onClick = { statusFilter = st; filterExpanded = false })
                    }
                }
            }
        }

        if (filteredOps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active operations match current filters.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredOps) { op ->
                    val exVat = op.sellExVat
                    val margin = exVat - op.vendorCost
                    val marginPct = if (exVat > 0) (margin / exVat) * 100 else 0.0

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Title row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(op.id, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        Text(op.refNo, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(op.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    }
                                    Text("Client: ${op.clientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Vendor: ${op.vendorName} (Cost: SAR ${op.vendorCost})", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }

                                IconButton(onClick = { onDeleteOperation(op.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Margin badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Sell: SAR ${String.format(Locale.US, "%.0f", exVat)} | Margin: SAR ${String.format(Locale.US, "%.0f", margin)} (${String.format(Locale.US, "%.1f", marginPct)}%)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (marginPct < settings.minMarginPct) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )

                                if (op.invoiced) {
                                    SuggestionChip(onClick = {}, label = { Text("Invoiced", fontSize = 10.sp) })
                                }
                            }

                            // Advance Stage Tracking Pipeline Buttons
                            Text("ADVANCE TRACKING STAGE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    statusStages.take(3).forEachIndexed { index, st ->
                                        val isCurrent = index == op.statusIndex
                                        Button(
                                            onClick = { onUpdateStatus(op, index) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("${index + 1}. $st", fontSize = 9.sp, maxLines = 1)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    statusStages.drop(3).forEachIndexed { indexOffset, st ->
                                        val realIndex = indexOffset + 3
                                        val isCurrent = realIndex == op.statusIndex
                                        Button(
                                            onClick = { onUpdateStatus(op, realIndex) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("${realIndex + 1}. $st", fontSize = 9.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            // Document actions
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { onViewWaybill(op) }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Waybill")
                                }

                                if (op.statusIndex == 5 && !op.invoiced) {
                                    Button(onClick = { onConvertInvoice(op) }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Receipt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tax Invoice")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDataDialog(
            title = "Shipments Operations Audit Export",
            subtitle = "Structured Excel/CSV export of all active and finished waybills",
            csvContent = CsvExporter.exportShipmentsCsv(operations),
            fileName = "YallaMuv_Shipments_Operations_2026.csv",
            onDismiss = { showExportDialog = false }
        )
    }
}
