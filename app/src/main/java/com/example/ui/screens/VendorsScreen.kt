package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun VendorsScreen(
    vendors: List<VendorEntity>,
    vendorRates: List<VendorRateEntity>,
    onRegisterVendor: () -> Unit,
    onSelectVendorAccount: (VendorEntity) -> Unit,
    onDeleteVendor: (VendorEntity) -> Unit
) {
    val totalCostSum = vendors.sumOf { it.cost * it.trips.coerceAtLeast(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Summary
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ACTIVE VENDORS", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("${vendors.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL SUBCONTRACT COST", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    Text("SAR ${String.format(Locale.US, "%.0f", totalCostSum)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registered Vendors & Accounts (${vendors.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Button(onClick = onRegisterVendor) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Register Vendor Account")
            }
        }

        // Vendor Cards List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vendors) { vendor ->
                val myRatesCount = vendorRates.count { it.vendorId == vendor.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(vendor.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("CR: ${vendor.crNumber} | VAT: ${vendor.vatNumber}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                Text("Contact: ${vendor.contact} (${vendor.phone})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteVendor(vendor) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Primary Truck: ${vendor.truck} (${vendor.plate})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            Text("Base Rate: SAR ${vendor.cost}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Configured Route Rates: $myRatesCount destinations", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            
                            OutlinedButton(
                                onClick = { onSelectVendorAccount(vendor) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage Rates & Account", fontSize = 11.sp)
                            }
                        }

                        // Expiry Status Warnings
                        val alerts = mutableListOf<String>()
                        if (vendor.istimaraExpiry.isNotBlank() && vendor.istimaraExpiry < "2026-10-01") alerts.add("Istimara: ${vendor.istimaraExpiry}")
                        if (vendor.opCardExpiry.isNotBlank() && vendor.opCardExpiry < "2026-10-01") alerts.add("WASL TGA: ${vendor.opCardExpiry}")
                        if (vendor.licenceExpiry.isNotBlank() && vendor.licenceExpiry < "2026-10-01") alerts.add("Licence: ${vendor.licenceExpiry}")
                        if (vendor.insuranceExpiry.isNotBlank() && vendor.insuranceExpiry < "2026-10-01") alerts.add("Insurance: ${vendor.insuranceExpiry}")

                        if (alerts.isNotEmpty()) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Compliance Expiries Warning:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    alerts.forEach { a -> Text(a, fontSize = 10.sp, color = MaterialTheme.colorScheme.onErrorContainer) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
