package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*

@Composable
fun WaybillDialog(
    operation: OperationEntity,
    settings: AppSettings,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar with Print/Save PDF/Share
                DocumentActionBar(
                    documentTitle = "Waybill Bill of Lading",
                    docNumber = "YM-WB-${operation.id}",
                    onClose = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                    Text(" WASL BAYAN WAYBILL - بوليصة شحن معتمدة ", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(settings.companyName.uppercase(), fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Carrier TGA License: 010239123 | Bayan Approved", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("WASL Permit Tracking: WASL-2026-KSA-99812", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("WAYBILL NO:", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text("YM-WB-${operation.id}", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                                Text("Date: ${operation.date}", fontSize = 11.sp)
                                Text("Status: ${operation.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }

                    // Shipper & Consignee Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("SHIPPER (ORIGIN) / المرسل:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text("${operation.originHub} Logistics Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("King Fahd Branch Rd, Logistics Dist.", fontSize = 11.sp)
                                Text("Dispatch Contact: Dispatch Hub (+966 12 654 3210)", fontSize = 11.sp)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("CONSIGNEE (DESTINATION) / المستلم:", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                Text(operation.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Destination: ${operation.originHub} -> Destination City", fontSize = 11.sp)
                                Text("Attn: ${operation.contact} (${operation.phone})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Transport Fleet & Vehicle Details
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("VEHICLE & SUBCONTRACTOR ASSIGNMENT / بيانات المركبة والسائق", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Truck Spec: ${operation.truckType}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Subcontractor: ${operation.vendorName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Plate No: KSA 5678 LYZ", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    Text("Driver Iqama: 2489012390", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Cargo Specification Table
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("CARGO SPECIFICATION & MANIFEST / تفاصيل الحمولة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Package Count: 18 Euro Pallets", fontSize = 11.sp)
                                Text("Temp Setting: +2°C to +4°C (Chilled)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Weight: 12,400 Kgs", fontSize = 11.sp)
                                Text("Security Seal No: SEAL-KSA-99812", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Proof of Delivery (POD) Signatures
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PROOF OF DELIVERY (POD) ACKNOWLEDGMENT / إقرار الاستلام", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("I hereby confirm receipt of goods listed above in good order and correct condition.", fontSize = 10.sp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Driver Signature", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("_________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Shipper Dispatch Stamp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("_________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Consignee Seal & Recv Sign", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("_________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientDirectoryDialog(
    clients: List<ClientEntity>,
    onAddClient: (ClientEntity) -> Unit,
    onDeleteClient: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var cr by remember { mutableStateOf("") }
    var vat by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Jeddah") }
    var postal by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ZATCA Client Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add New ZATCA Compliant Client", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name (EN / AR) *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cr, onValueChange = { cr = it }, label = { Text("CR (10 Digits) *") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = vat, onValueChange = { vat = it }, label = { Text("VAT (15 Digits) *") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = building, onValueChange = { building = it }, label = { Text("Bldg No *") }, modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District *") }, modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = postal, onValueChange = { postal = it }, label = { Text("Postal Code *") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && cr.isNotBlank() && vat.isNotBlank()) {
                                onAddClient(
                                    ClientEntity(
                                        name = name,
                                        contact = contact,
                                        phone = phone.ifBlank { "+966 50 000 0000" },
                                        email = "client@domain.sa",
                                        cr = cr,
                                        vat = vat,
                                        building = building.ifBlank { "1234" },
                                        street = street.ifBlank { "King Fahd Rd" },
                                        district = district.ifBlank { "Al-Rehab" },
                                        city = city.ifBlank { "Jeddah" },
                                        postal = postal.ifBlank { "23345" }
                                    )
                                )
                                name = ""; contact = ""; cr = ""; vat = ""; phone = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Client")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Existing Clients (${clients.size})", fontWeight = FontWeight.Bold)
                    clients.forEach { c ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("CR: ${c.cr} | VAT: ${c.vat}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("${c.building}, ${c.street}, ${c.district}, ${c.city}", fontSize = 10.sp)
                                }
                                IconButton(onClick = { onDeleteClient(c) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterVendorDialog(
    onSave: (VendorEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var crNumber by remember { mutableStateOf("") }
    var vatNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("Al Rajhi Bank") }
    var iban by remember { mutableStateOf("") }
    var truck by remember { mutableStateOf("Reefer 13.6m Chilled") }
    var plate by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("1200") }
    var istimara by remember { mutableStateOf("2026-11-15") }
    var opCard by remember { mutableStateOf("2026-12-01") }
    var licence by remember { mutableStateOf("2027-05-10") }
    var insurance by remember { mutableStateOf("2026-10-20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Subcontractor Vendor") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Account Identity & Legal Registration", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor Legal / Fleet Name *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Manager") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone (+966)") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = crNumber, onValueChange = { crNumber = it }, label = { Text("CR Number (سجل تجاري)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = vatNumber, onValueChange = { vatNumber = it }, label = { Text("VAT Reg (رقم ضريبي)") }, modifier = Modifier.weight(1f))
                }

                Text("Bank & Settlement Details", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = iban, onValueChange = { iban = it }, label = { Text("IBAN Number") }, modifier = Modifier.weight(1f))
                }

                Text("Primary Fleet Specs & Base Rate", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = truck, onValueChange = { truck = it }, label = { Text("Primary Truck Type") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Plate No") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Base Cost Rate (SAR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }

                Text("TGA Regulatory Compliance Expiries (YYYY-MM-DD)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = istimara, onValueChange = { istimara = it }, label = { Text("Istimara Expiry") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = opCard, onValueChange = { opCard = it }, label = { Text("WASL / TGA Card") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = licence, onValueChange = { licence = it }, label = { Text("Driver Licence") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = insurance, onValueChange = { insurance = it }, label = { Text("Insurance Expiry") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            VendorEntity(
                                name = name,
                                contact = contact.ifBlank { "Fleet Manager" },
                                phone = phone.ifBlank { "+966 50 000 0000" },
                                email = email.ifBlank { "info@vendor.sa" },
                                crNumber = crNumber.ifBlank { "1010889900" },
                                vatNumber = vatNumber.ifBlank { "310099887700003" },
                                bankName = bankName,
                                iban = iban.ifBlank { "SA44 8000 0000 1234 5678 9000" },
                                truck = truck,
                                plate = plate.ifBlank { "KSA 9988 LYZ" },
                                cost = costText.toDoubleOrNull() ?: 1200.0,
                                istimaraExpiry = istimara,
                                opCardExpiry = opCard,
                                licenceExpiry = licence,
                                insuranceExpiry = insurance
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Create Vendor Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun VendorAccountDialog(
    vendor: VendorEntity,
    vendorRates: List<VendorRateEntity>,
    onSaveRate: (hub: String, truckType: String, destination: String, cost: Double) -> Unit,
    onDeleteRate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHub by remember { mutableStateOf("Jeddah") }
    var selectedTruck by remember { mutableStateOf(vendor.truck) }
    var selectedDestination by remember { mutableStateOf("Riyadh") }
    var customRateText by remember { mutableStateOf(vendor.cost.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${vendor.name} — Vendor Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("CR: ${vendor.crNumber} | VAT: ${vendor.vatNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Card
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("VENDOR PROFILE & BANK ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Contact: ${vendor.contact} (${vendor.phone})", fontSize = 11.sp)
                            Text("Email: ${vendor.email}", fontSize = 11.sp)
                            Text("Bank: ${vendor.bankName} | IBAN: ${vendor.iban}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                            Text("Default Fleet Spec: ${vendor.truck} | Plate: ${vendor.plate}", fontSize = 11.sp)
                        }
                    }

                    // Configure Route Rates
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("SET DESTINATION COST RATE / إعداد أسعار التكلفة للوجهات", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = selectedHub, onValueChange = { selectedHub = it }, label = { Text("Origin Hub") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = selectedTruck, onValueChange = { selectedTruck = it }, label = { Text("Vehicle Type") }, modifier = Modifier.weight(1f))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = selectedDestination, onValueChange = { selectedDestination = it }, label = { Text("Destination City") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = customRateText, onValueChange = { customRateText = it }, label = { Text("Cost Rate (SAR)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            }

                            Button(
                                onClick = {
                                    val cost = customRateText.toDoubleOrNull() ?: vendor.cost
                                    onSaveRate(selectedHub, selectedTruck, selectedDestination, cost)
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Rate for Destination")
                            }
                        }
                    }

                    // Existing Saved Rates List
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("SAVED COST RATES FOR THIS VENDOR (${vendorRates.filter { it.vendorId == vendor.id }.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                            val myRates = vendorRates.filter { it.vendorId == vendor.id }
                            if (myRates.isEmpty()) {
                                Text("No custom destination rates configured yet. Default base rate SAR ${vendor.cost} applies.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            } else {
                                myRates.forEach { rate ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${rate.hub} ➔ ${rate.destination}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Truck: ${rate.truckType}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("SAR ${rate.cost}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                            IconButton(onClick = { onDeleteRate(rate.id) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetupSettingsDialog(
    settings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var companyName by remember { mutableStateOf(settings.companyName) }
    var cr by remember { mutableStateOf(settings.cr) }
    var vat by remember { mutableStateOf(settings.vat) }
    var vatRateText by remember { mutableStateOf(settings.vatRate.toString()) }
    var freeWaitingText by remember { mutableStateOf(settings.freeWaitingHours.toString()) }
    var detentionRateText by remember { mutableStateOf(settings.detentionRate.toString()) }
    var dropRateText by remember { mutableStateOf(settings.dropRate.toString()) }
    var paymentTermsText by remember { mutableStateOf(settings.paymentTermsDays.toString()) }
    var geminiKey by remember { mutableStateOf(settings.geminiKey) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Setup & Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Company & ZATCA Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Legal Company Name") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cr, onValueChange = { cr = it }, label = { Text("CR Number") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = vat, onValueChange = { vat = it }, label = { Text("VAT Reg Number") }, modifier = Modifier.weight(1f))
                    }

                    Text("Commercial & Operational Rules", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = vatRateText, onValueChange = { vatRateText = it }, label = { Text("VAT Rate %") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = freeWaitingText, onValueChange = { freeWaitingText = it }, label = { Text("Free Waiting Hrs") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = detentionRateText, onValueChange = { detentionRateText = it }, label = { Text("Detention SAR/hr") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = dropRateText, onValueChange = { dropRateText = it }, label = { Text("Extra Drop SAR") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = paymentTermsText, onValueChange = { paymentTermsText = it }, label = { Text("Payment Terms (Days)") }, modifier = Modifier.fillMaxWidth())

                    Text("Gemini AI Copilot Key", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
                        label = { Text("Google AI Studio Key") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSave(
                                settings.copy(
                                    companyName = companyName,
                                    cr = cr,
                                    vat = vat,
                                    vatRate = vatRateText.toDoubleOrNull() ?: 15.0,
                                    freeWaitingHours = freeWaitingText.toDoubleOrNull() ?: 2.0,
                                    detentionRate = detentionRateText.toDoubleOrNull() ?: 100.0,
                                    dropRate = dropRateText.toDoubleOrNull() ?: 100.0,
                                    paymentTermsDays = paymentTermsText.toIntOrNull() ?: 30,
                                    geminiKey = geminiKey
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutOptionsDialog(
    currentLanguage: com.example.ui.theme.AppLanguage,
    currentThemeMode: com.example.ui.theme.ThemeMode,
    currentThemeStyle: com.example.ui.theme.LayoutThemeStyle,
    currentNavMode: String,
    onSelectLanguage: (com.example.ui.theme.AppLanguage) -> Unit,
    onSelectThemeMode: (com.example.ui.theme.ThemeMode) -> Unit,
    onSelectThemeStyle: (com.example.ui.theme.LayoutThemeStyle) -> Unit,
    onSelectNavMode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isAr = currentLanguage == com.example.ui.theme.AppLanguage.ARABIC
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                if (isAr) "خيارات المظهر واللغة والواجهة" else "Professional Layout & Language Options",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (isAr) "تخصيص لغة التطبيق (العربية / English)، المظهر، ونمط التنقل" else "Customize application language, theme mode, and navigation layout",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                HorizontalDivider()

                // Section 0: Application Language Toggle (English / Arabic KSA)
                Text(
                    if (isAr) "١. لغة التطبيق (العربية / English)" else "1. APPLICATION LANGUAGE (ENGLISH / ARABIC)",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.example.ui.theme.AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Card(
                            onClick = { onSelectLanguage(lang) },
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(lang.flag, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(lang.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(lang.code.uppercase(), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                RadioButton(selected = isSelected, onClick = { onSelectLanguage(lang) })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 1: Appearance Light/Dark Mode Toggle
                Text(
                    if (isAr) "٢. نمط المظهر (فاتح / داكن / حسب النظام)" else "2. APPEARANCE MODE (LIGHT / DARK / SYSTEM)",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                com.example.ui.theme.ThemeMode.values().forEach { mode ->
                    val isSelected = currentThemeMode == mode
                    Card(
                        onClick = { onSelectThemeMode(mode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(mode.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(selected = isSelected, onClick = { onSelectThemeMode(mode) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 2: Preset Theme Style
                Text("2. BI DASHBOARD THEME PRESET", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                com.example.ui.theme.LayoutThemeStyle.values().forEach { style ->
                    val isSelected = currentThemeStyle == style
                    Card(
                        onClick = { onSelectThemeStyle(style) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(style.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(style.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(selected = isSelected, onClick = { onSelectThemeStyle(style) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section 3: Navigation Layout Mode
                Text("3. NAVIGATION LAYOUT STYLE", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)

                val navOptions = listOf(
                    Triple("BOTTOM_PILL", "Modern Floating Dock", "Sleek elevated bottom pill bar with active indicators"),
                    Triple("TOP_HEADER", "Executive Header Tabs", "Quick top header navigation chips for immediate view access"),
                    Triple("SIDE_RAIL", "Clean Side Rail Layout", "Professional side navigation bar optimized for wide displays")
                )

                navOptions.forEach { (modeKey, modeTitle, modeDesc) ->
                    val isSelected = currentNavMode == modeKey
                    Card(
                        onClick = { onSelectNavMode(modeKey) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(modeTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(modeDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(selected = isSelected, onClick = { onSelectNavMode(modeKey) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Apply & Close")
                }
            }
        }
    }
}

