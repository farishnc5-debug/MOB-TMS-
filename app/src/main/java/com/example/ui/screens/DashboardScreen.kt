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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.util.Locale

@Composable
fun DashboardScreen(
    operations: List<OperationEntity>,
    invoices: List<InvoiceEntity>,
    vendors: List<VendorEntity>,
    settings: AppSettings
) {
    val activeOps = operations.filter { it.status != "Finished" }
    val finishedOps = operations.filter { it.status == "Finished" }

    var revenue = 0.0
    var vendorCost = 0.0
    invoices.forEach { inv ->
        revenue += inv.subtotal
        vendorCost += inv.vendorCost
    }
    val margin = revenue - vendorCost
    val marginPct = if (revenue > 0) (margin / revenue) * 100 else 0.0

    var receivables = 0.0
    var overdue = 0.0
    invoices.forEach { inv ->
        val outstanding = Math.max(0.0, inv.grandTotal - inv.paidAmount)
        receivables += outstanding
        if (outstanding > 0 && inv.status != "Paid") {
            overdue += outstanding
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Regulatory Expiry Alerts
        val expiries = mutableListOf<String>()
        vendors.forEach { v ->
            if (v.istimaraExpiry.isNotBlank() && v.istimaraExpiry < "2026-10-01") expiries.add("${v.name}: Istimara expiring soon (${v.istimaraExpiry})")
            if (v.opCardExpiry.isNotBlank() && v.opCardExpiry < "2026-10-01") expiries.add("${v.name}: TGA Operating Card expiring soon (${v.opCardExpiry})")
        }

        if (expiries.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Regulatory Compliance Alerts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        expiries.forEach { alert ->
                            Text("• $alert", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Top 4 KPI Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "ACTIVE OPERATIONS",
                        value = "${activeOps.size}",
                        subtitle = "${finishedOps.size} finished all-time",
                        icon = Icons.Default.Route,
                        color = MaterialTheme.colorScheme.primary
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "REVENUE (EX-VAT)",
                        value = "SAR ${String.format(Locale.US, "%.0f", revenue)}",
                        subtitle = "Across ${invoices.size} invoices",
                        icon = Icons.Default.AttachMoney,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "GROSS MARGIN",
                        value = "SAR ${String.format(Locale.US, "%.0f", margin)}",
                        subtitle = "${String.format(Locale.US, "%.1f", marginPct)}% net margin",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = if (marginPct < settings.minMarginPct) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "RECEIVABLES",
                        value = "SAR ${String.format(Locale.US, "%.0f", receivables)}",
                        subtitle = "SAR ${String.format(Locale.US, "%.0f", overdue)} past due",
                        icon = Icons.Default.Schedule,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Fleet Status Distribution
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Fleet Status Pipeline Distribution", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    val stages = listOf("Dispatched", "At Loading Location", "In Transit", "At Destination Waiting", "Offloaded & Left", "Finished")
                    val total = operations.size.coerceAtLeast(1)

                    stages.forEach { stage ->
                        val count = operations.count { it.status == stage }
                        val progress = count.toFloat() / total
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stage, fontSize = 11.sp)
                                Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                            )
                        }
                    }
                }
            }
        }

        // Margin Watchlist
        val lowMarginOps = operations.filter {
            val exVat = it.sellExVat
            val m = exVat - it.vendorCost
            val pct = if (exVat > 0) (m / exVat) * 100 else 0.0
            pct < settings.minMarginPct
        }

        if (lowMarginOps.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Margin Watchlist (Below ${settings.minMarginPct}% Target)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                        }
                        lowMarginOps.forEach { op ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${op.id} - ${op.clientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Vendor: ${op.vendorName} (Cost SAR ${op.vendorCost})", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                val exVat = op.sellExVat
                                val m = exVat - op.vendorCost
                                val pct = if (exVat > 0) (m / exVat) * 100 else 0.0
                                Text("${String.format(Locale.US, "%.1f", pct)}%", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
