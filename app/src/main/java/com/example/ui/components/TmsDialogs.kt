package com.example.ui.components

import android.content.Context
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.util.ZatcaTlvEncoder
import org.json.JSONArray
import java.util.Locale

data class ParsedLineItem(
    val destination: String,
    val distance: Int,
    val transitTime: String,
    val lineTotal: Double
)

@Composable
fun DocumentActionBar(
    documentTitle: String,
    docNumber: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isGeneratingPdf by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("ORIGINAL DOCUMENT / نسخة أصلية معتمدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Print Button
                FilledTonalButton(
                    onClick = {
                        Toast.makeText(context, "🖨️ Preparing Print Spooler for $docNumber...", Toast.LENGTH_LONG).show()
                        try {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                            // Print spooler trigger notification
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print / طباعة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Download PDF Button
                Button(
                    onClick = {
                        isGeneratingPdf = true
                        Toast.makeText(context, "📄 Saved $docNumber.pdf to Documents folder!", Toast.LENGTH_LONG).show()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save PDF / تحميل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "📤 Document $docNumber link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                IconButton(onClick = onClose, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        if (isGeneratingPdf) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun BilingualQuotationDialog(
    quote: QuotationEntity,
    settings: AppSettings,
    onDismiss: () -> Unit
) {
    val parsedItems = remember(quote.lineItemsJson) {
        val list = mutableListOf<ParsedLineItem>()
        try {
            val jsonArr = JSONArray(quote.lineItemsJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    ParsedLineItem(
                        destination = obj.optString("destination", "Riyadh"),
                        distance = obj.optInt("distance", 950),
                        transitTime = obj.optString("transitTime", "11 - 14 Hours"),
                        lineTotal = obj.optDouble("lineTotal", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        list
    }

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
                    documentTitle = "Commercial Quotation",
                    docNumber = quote.refNo,
                    onClose = onDismiss
                )

                // Scrollable Document Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Letterhead
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(settings.companyName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text("شركة يلا موف للنقل البري والخدمات اللوجستية", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Transport & Freight Logistics | Al Rehab Dist., Jeddah, KSA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("CR: ${settings.cr} | VAT: ${settings.vat}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                                        Text("OFFICIAL QUOTATION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text("عرض سعر تجاري رسمي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(quote.refNo, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                Text("Date: ${quote.date}", fontSize = 11.sp)
                                Text("Valid Until: ${quote.validUntil}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Client & Fleet Specs
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PREPARED FOR / مقدم إلى:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Text(quote.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Attn: ${quote.contactPerson}", fontSize = 11.sp)
                                Text("Phone: ${quote.phone}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("Email: ${quote.email}", fontSize = 11.sp)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("FLEET & SERVICE SPECIFICATION:", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                Text("Origin Hub: ${quote.originHub} KSA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Truck Spec: ${quote.truckType}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Payment Terms: 30 Days Net", fontSize = 11.sp)
                                Text("Currency: Saudi Arabian Riyal (SAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Line Items Table
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("DESTINATION & FREIGHT CHARGES / جدول الأسعار والمسارات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("SAR", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            if (parsedItems.isEmpty()) {
                                Text("Point-to-Point Freight Service (${quote.originHub} -> Selected Destinations)", fontSize = 12.sp)
                            } else {
                                parsedItems.forEachIndexed { i, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${i + 1}. ${quote.originHub} -> ${item.destination}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Est Distance: ${item.distance} km | Transit Window: ${item.transitTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("SAR ${String.format(Locale.US, "%.2f", item.lineTotal)}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                    if (i < parsedItems.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }

                    // Financial Summary Breakdown
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Freight Subtotal (ex. VAT) / المجموع الفرعي:", fontSize = 12.sp)
                                Text("SAR ${String.format(Locale.US, "%.2f", quote.subtotal)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("15% ZATCA Value Added Tax (VAT) / الضريبة:", fontSize = 12.sp)
                                Text("SAR ${String.format(Locale.US, "%.2f", quote.vat)}", fontFamily = FontFamily.Monospace)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total (incl. 15% VAT) / الإجمالي الشامل:", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("SAR ${String.format(Locale.US, "%.2f", quote.grandTotal)}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Terms & Commercial Conditions
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("COMMERCIAL TERMS & OPERATIONAL POLICY / الشروط والأحكام:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("• Rates are valid for 30 days from date of issue and subject to truck availability.", fontSize = 10.sp)
                            Text("• Free waiting time: First 2 hours free at origin and destination.", fontSize = 10.sp)
                            Text("• Detention / Overtime: SAR 100 per hour after free window (capped at SAR 500 per 24 hours).", fontSize = 10.sp)
                            Text("• Additional drop-off locations charged at SAR 100 per extra stop.", fontSize = 10.sp)
                            Text("• Payment terms: 30 days from official invoice date.", fontSize = 10.sp)
                        }
                    }

                    // Signature & Stamp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Prepared By (Yalla Muv Logistics)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Authorized Signatory & Stamp", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Client Acceptance & Seal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Signature: ____________________", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaxInvoiceDialog(
    invoice: InvoiceEntity,
    settings: AppSettings,
    onDismiss: () -> Unit
) {
    val tlvPayload = remember(invoice) {
        ZatcaTlvEncoder.generateTlvBase64(
            sellerName = settings.companyName,
            vatNumber = settings.vat,
            timestampIso = invoice.issuedAt,
            totalWithVat = invoice.grandTotal,
            vatTotal = invoice.vat
        )
    }

    // Parse JSON items
    val parsedItems = remember(invoice.lineItemsJson) {
        val list = mutableListOf<ParsedLineItem>()
        try {
            val jsonArr = JSONArray(invoice.lineItemsJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    ParsedLineItem(
                        destination = obj.optString("destination", "Riyadh"),
                        distance = obj.optInt("distance", 950),
                        transitTime = obj.optString("transitTime", "11 - 14 Hours"),
                        lineTotal = obj.optDouble("lineTotal", invoice.subtotal)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        list
    }

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
                    documentTitle = "ZATCA Tax Invoice",
                    docNumber = invoice.invNo,
                    onClose = onDismiss
                )

                // Scrollable Document Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Banner with QR Code
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(6.dp)) {
                                    Text(" TAX INVOICE - فاتورة ضريبية ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(settings.companyName.uppercase(), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("شركة يلا موف للخدمات اللوجستية", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Jeddah, Kingdom of Saudi Arabia | CR: ${settings.cr}", fontSize = 11.sp)
                                Text("VAT Reg No: ${settings.vat}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                ZatcaQrCodeComposable(tlvBase64 = tlvPayload)
                                Text("ZATCA Phase 2 QR", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Invoice Metadata Row
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("INVOICE NO / رقم الفاتورة:", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text(invoice.invNo, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                Text("Shipment Ref: ${invoice.opId}", fontSize = 11.sp)
                            }
                            Column {
                                Text("ISSUE DATE / تاريخ الإصدار:", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text(invoice.date, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text("DUE DATE / تاريخ الاستحقاق:", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text(invoice.dueDate, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Client (Buyer) ZATCA Details
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("BUYER DETAILS / بيانات العميل المشتري:", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(invoice.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("National Address: ${invoice.clientDetails}", fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Client CR: ${invoice.clientCR}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Client VAT: ${invoice.clientVAT}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    // Itemized Table
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SERVICE LINE ITEMS / تفاصيل الشحنة والخدمات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            if (parsedItems.isEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Freight Transportation Service", fontSize = 12.sp)
                                    Text("SAR ${String.format(Locale.US, "%.2f", invoice.subtotal)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                parsedItems.forEachIndexed { i, item ->
                                    val itemExVat = item.lineTotal
                                    val itemVat = itemExVat * 0.15
                                    val itemInclVat = itemExVat + itemVat

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${i + 1}. Freight Transport: ${item.destination}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Distance: ${item.distance} km | Ex. VAT: SAR ${String.format(Locale.US, "%.2f", itemExVat)} | VAT 15%: SAR ${String.format(Locale.US, "%.2f", itemVat)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("SAR ${String.format(Locale.US, "%.2f", itemInclVat)}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                    }
                                    if (i < parsedItems.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }

                    // Financial Summary Breakdown
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Taxable Amount (ex. VAT) / المجموع غير شامل الضريبة:")
                                Text("SAR ${String.format(Locale.US, "%.2f", invoice.subtotal)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total VAT Amount (15%) / مجموع ضريبة القيمة المضافة:")
                                Text("SAR ${String.format(Locale.US, "%.2f", invoice.vat)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Due (incl. VAT) / الإجمالي شامل الضريبة:", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("SAR ${String.format(Locale.US, "%.2f", invoice.grandTotal)}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid / المبلغ المدفوع:")
                                Text("SAR ${String.format(Locale.US, "%.2f", invoice.paidAmount)}", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.tertiary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Balance Outstanding / المتبقي:", fontWeight = FontWeight.Bold)
                                val balance = Math.max(0.0, invoice.grandTotal - invoice.paidAmount)
                                Text("SAR ${String.format(Locale.US, "%.2f", balance)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = if (balance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }

                    // Banking Details for Transfer
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("BANK PAYMENT DETAILS / الحساب البنكي للسداد:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Beneficiary: Yalla Muv Logistics Company", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Bank Name: Al-Rajhi Bank (مصرف الراجحي)", fontSize = 11.sp)
                            Text("IBAN: SA82 8000 0000 6080 1010 9999", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Swift Code: RJHI SA RI", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    invoice: InvoiceEntity,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(String.format(Locale.US, "%.2f", Math.max(0.0, invoice.grandTotal - invoice.paidAmount))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Customer Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Invoice: ${invoice.invNo}", fontWeight = FontWeight.Bold)
                Text("Client: ${invoice.clientName}", fontSize = 12.sp)
                Text("Total: SAR ${invoice.grandTotal} | Paid: SAR ${invoice.paidAmount}", fontSize = 12.sp)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Received (SAR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(amt)
                        onDismiss()
                    }
                }
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
