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
import com.example.data.remote.ParsedShipmentAiResult
import java.util.Locale

import com.example.ui.components.ExportDataDialog
import com.example.util.CsvExporter

@Composable
fun InvoicesAndAiScreen(
    invoices: List<InvoiceEntity>,
    quotations: List<QuotationEntity>,
    aiParsingState: String?,
    aiChatHistory: List<Pair<String, String>>,
    onViewInvoice: (InvoiceEntity) -> Unit,
    onViewQuotation: (QuotationEntity) -> Unit,
    onRecordPayment: (InvoiceEntity) -> Unit,
    onParsePrompt: (String, (ParsedShipmentAiResult) -> Unit) -> Unit,
    onSendAiChat: (String) -> Unit
) {
    var subTab by remember { mutableStateOf("INVOICES") } // INVOICES, QUOTES, GEMINI_AI
    var promptInput by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportTitle by remember { mutableStateOf("") }
    var exportSubtitle by remember { mutableStateOf("") }
    var exportCsvData by remember { mutableStateOf("") }
    var exportFileName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SubTab Bar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = subTab == "INVOICES",
                onClick = { subTab = "INVOICES" },
                label = { Text("Tax Invoices (${invoices.size})") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = subTab == "QUOTES",
                onClick = { subTab = "QUOTES" },
                label = { Text("Quotes (${quotations.size})") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = subTab == "GEMINI_AI",
                onClick = { subTab = "GEMINI_AI" },
                label = { Text("Gemini AI") },
                modifier = Modifier.weight(1f)
            )
        }

        when (subTab) {
            "INVOICES" -> {
                if (invoices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No ZATCA Tax Invoices generated yet.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tax Invoice Audit Ledger",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        FilledTonalButton(
                            onClick = {
                                exportTitle = "ZATCA Tax Invoices Audit Export"
                                exportSubtitle = "Offline Excel/CSV Audit Record for KSA Tax Authorities"
                                exportCsvData = CsvExporter.exportInvoicesCsv(invoices)
                                exportFileName = "ZATCA_Tax_Invoices_2026.csv"
                                showExportDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel/CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(invoices) { inv ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(inv.invNo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                                            Text(inv.clientName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        val statusColor = when (inv.status) {
                                            "Paid" -> MaterialTheme.colorScheme.tertiary
                                            "Part Paid" -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.error
                                        }

                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(inv.status, color = statusColor, fontWeight = FontWeight.Bold) }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Total Due: SAR ${String.format(Locale.US, "%.2f", inv.grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Paid: SAR ${String.format(Locale.US, "%.2f", inv.paidAmount)} | Due: ${inv.dueDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (inv.status != "Paid") {
                                                OutlinedButton(onClick = { onRecordPayment(inv) }) {
                                                    Text("Payment")
                                                }
                                            }
                                            Button(onClick = { onViewInvoice(inv) }) {
                                                Text("View")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "QUOTES" -> {
                if (quotations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No saved quotations.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Generated Quotes Ledger",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            FilledTonalButton(
                                onClick = {
                                    exportTitle = "Quotations Records Export"
                                    exportSubtitle = "Offline Excel/CSV Summary of B2B Logistics Quotes"
                                    exportCsvData = CsvExporter.exportQuotationsCsv(quotations)
                                    exportFileName = "YallaMuv_Quotations_2026.csv"
                                    showExportDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Excel/CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(quotations) { q ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(q.refNo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(q.clientName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Total: SAR ${String.format(Locale.US, "%.2f", q.grandTotal)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Button(onClick = { onViewQuotation(q) }) {
                                            Text("View")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "GEMINI_AI" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Natural Language Quote Parser
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Gemini AI Natural Language Quote Parser", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Type shipment request in English or Arabic (e.g. '4-ton reefer chilled from Jeddah to Riyadh for Al-Marai Foods with 2 extra drops').", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                                OutlinedTextField(
                                    value = promptInput,
                                    onValueChange = { promptInput = it },
                                    placeholder = { Text("Enter prompt...") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (promptInput.isNotBlank()) {
                                            onParsePrompt(promptInput) { result ->
                                                promptInput = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Parse & Fill Quote with AI")
                                }

                                aiParsingState?.let {
                                    Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Regulatory Advisor Chat
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Text("KSA Logistics Regulatory Copilot", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (aiChatHistory.isEmpty()) {
                                        Text("Ask about TGA operating cards, Wasl platform, Bayan waybills, ZATCA e-invoicing...", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    } else {
                                        aiChatHistory.forEach { (sender, text) ->
                                            Text("$sender: $text", fontSize = 11.sp, fontWeight = if (sender == "You") FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = chatInput,
                                        onValueChange = { chatInput = it },
                                        placeholder = { Text("Ask regulatory question...") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            if (chatInput.isNotBlank()) {
                                                onSendAiChat(chatInput)
                                                chatInput = ""
                                            }
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
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
            title = exportTitle,
            subtitle = exportSubtitle,
            csvContent = exportCsvData,
            fileName = exportFileName,
            onDismiss = { showExportDialog = false }
        )
    }
}
