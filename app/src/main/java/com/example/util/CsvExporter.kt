package com.example.util

import com.example.data.model.InvoiceEntity
import com.example.data.model.OperationEntity
import com.example.data.model.QuotationEntity
import java.util.Locale

object CsvExporter {

    /**
     * Generates a structured CSV for Shipment Operations records with UTF-8 BOM for Excel support.
     */
    fun exportShipmentsCsv(operations: List<OperationEntity>): String {
        val sb = StringBuilder()
        // UTF-8 BOM for proper Arabic/English rendering in Excel
        sb.append("\uFEFF")

        // CSV Header
        sb.append("Shipment ID,Ref Quote No,Date,Origin Hub,Truck Type,Client Name,Contact Person,Phone,Vendor Name,Vendor Cost (SAR),Sell Ex-VAT (SAR),Grand Total (SAR),Status,Invoice No,Document Status\n")

        for (op in operations) {
            val line = listOf(
                escapeCsv(op.id),
                escapeCsv(op.refNo),
                escapeCsv(op.date),
                escapeCsv(op.originHub),
                escapeCsv(op.truckType),
                escapeCsv(op.clientName),
                escapeCsv(op.contact),
                escapeCsv(op.phone),
                escapeCsv(op.vendorName),
                String.format(Locale.US, "%.2f", op.vendorCost),
                String.format(Locale.US, "%.2f", op.sellExVat),
                String.format(Locale.US, "%.2f", op.grandTotal),
                escapeCsv(op.status),
                escapeCsv(op.invoiceNo.ifBlank { "N/A" }),
                escapeCsv(op.originalDocsStatus)
            ).joinToString(",")
            sb.append(line).append("\n")
        }

        return sb.toString()
    }

    /**
     * Generates a structured ZATCA-compliant Tax Invoice audit CSV for tax auditing and offline accounting.
     */
    fun exportInvoicesCsv(invoices: List<InvoiceEntity>): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")

        // CSV Header
        sb.append("Invoice No,Issue Date,Due Date,Operation ID,Client Name,Client CR,Client VAT,Subtotal Ex-VAT (SAR),VAT 15% (SAR),Grand Total (SAR),Payment Status,Paid Amount (SAR),Balance Due (SAR),Vendor Cost (SAR)\n")

        for (inv in invoices) {
            val balanceDue = (inv.grandTotal - inv.paidAmount).coerceAtLeast(0.0)
            val line = listOf(
                escapeCsv(inv.invNo),
                escapeCsv(inv.date),
                escapeCsv(inv.dueDate),
                escapeCsv(inv.opId),
                escapeCsv(inv.clientName),
                escapeCsv(inv.clientCR),
                escapeCsv(inv.clientVAT),
                String.format(Locale.US, "%.2f", inv.subtotal),
                String.format(Locale.US, "%.2f", inv.vat),
                String.format(Locale.US, "%.2f", inv.grandTotal),
                escapeCsv(inv.status),
                String.format(Locale.US, "%.2f", inv.paidAmount),
                String.format(Locale.US, "%.2f", balanceDue),
                String.format(Locale.US, "%.2f", inv.vendorCost)
            ).joinToString(",")
            sb.append(line).append("\n")
        }

        return sb.toString()
    }

    /**
     * Generates a Quotations summary CSV.
     */
    fun exportQuotationsCsv(quotations: List<QuotationEntity>): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")

        sb.append("Quotation Ref,Date,Valid Until,Origin Hub,Truck Type,Client Name,Contact Person,Phone,Email,Subtotal Ex-VAT (SAR),VAT 15% (SAR),Grand Total (SAR)\n")

        for (qt in quotations) {
            val line = listOf(
                escapeCsv(qt.refNo),
                escapeCsv(qt.date),
                escapeCsv(qt.validUntil),
                escapeCsv(qt.originHub),
                escapeCsv(qt.truckType),
                escapeCsv(qt.clientName),
                escapeCsv(qt.contactPerson),
                escapeCsv(qt.phone),
                escapeCsv(qt.email),
                String.format(Locale.US, "%.2f", qt.subtotal),
                String.format(Locale.US, "%.2f", qt.vat),
                String.format(Locale.US, "%.2f", qt.grandTotal)
            ).joinToString(",")
            sb.append(line).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(text: String): String {
        val escaped = text.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
