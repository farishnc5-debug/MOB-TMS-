package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tariff_overrides")
data class TariffOverride(
    @PrimaryKey val id: String, // "hub_truck_destination"
    val hub: String,
    val truckType: String,
    val destination: String,
    val cost: Double
)

data class DestinationItem(
    val destination: String,
    val destinationAr: String,
    val distance: Int,
    val defaultCost: Double = 0.0
)

@Entity(
    tableName = "clients",
    indices = [
        Index(value = ["name"]),
        Index(value = ["cr"]),
        Index(value = ["vat"])
    ]
)
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contact: String,
    val phone: String,
    val email: String,
    val cr: String,
    val vat: String,
    val building: String,
    val street: String,
    val district: String,
    val city: String,
    val postal: String
)

@Entity(
    tableName = "vendors",
    indices = [
        Index(value = ["name"]),
        Index(value = ["city"]),
        Index(value = ["truck"]),
        Index(value = ["crNumber"])
    ]
)
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contact: String,
    val truck: String,
    val plate: String,
    val cost: Double,
    val trips: Int = 0,
    val istimaraExpiry: String = "",
    val opCardExpiry: String = "",
    val licenceExpiry: String = "",
    val insuranceExpiry: String = "",
    val phone: String = "",
    val email: String = "",
    val crNumber: String = "",
    val vatNumber: String = "",
    val bankName: String = "",
    val iban: String = "",
    val city: String = "Jeddah",
    val supportedTruckTypes: String = ""
)

@Entity(
    tableName = "vendor_rates",
    indices = [
        Index(value = ["vendorId"]),
        Index(value = ["hub", "truckType", "destination"])
    ]
)
data class VendorRateEntity(
    @PrimaryKey val id: String, // "${vendorId}_${hub}_${truckType}_${destination}"
    val vendorId: Int,
    val hub: String,
    val truckType: String,
    val destination: String,
    val cost: Double
)

data class LineItemData(
    val destination: String,
    val distance: Int,
    val transitTime: String,
    val extraDrops: Int = 0,
    val waitingHours: Int = 2,
    val manualRate: Double? = null,
    val lineTotal: Double = 0.0
)

@Entity(
    tableName = "quotations",
    indices = [
        Index(value = ["clientName"]),
        Index(value = ["date"])
    ]
)
data class QuotationEntity(
    @PrimaryKey val refNo: String,
    val date: String,
    val originHub: String,
    val truckType: String,
    val clientName: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val validUntil: String,
    val subtotal: Double,
    val vat: Double,
    val grandTotal: Double,
    val lineItemsJson: String // serialized JSON of List<LineItemData>
)

@Entity(
    tableName = "operations",
    indices = [
        Index(value = ["refNo"]),
        Index(value = ["status"]),
        Index(value = ["clientName"]),
        Index(value = ["vendorName"]),
        Index(value = ["date"]),
        Index(value = ["invoiced"])
    ]
)
data class OperationEntity(
    @PrimaryKey val id: String,
    val refNo: String,
    val date: String,
    val originHub: String,
    val truckType: String,
    val clientName: String,
    val contact: String,
    val phone: String,
    val vendorName: String,
    val vendorCost: Double,
    val grandTotal: Double,
    val sellExVat: Double,
    val status: String = "Dispatched",
    val statusIndex: Int = 0,
    val invoiced: Boolean = false,
    val invoiceNo: String = "",
    val originalDocsStatus: String = "Pending Collection from Vendor",
    val podFileName: String = "",
    val lineItemsJson: String // serialized JSON of List<LineItemData>
)

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["opId"]),
        Index(value = ["clientName"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey val invNo: String,
    val date: String,
    val issuedAt: String,
    val dueDate: String,
    val opId: String,
    val vendorCost: Double,
    val clientName: String,
    val clientDetails: String,
    val clientCR: String,
    val clientVAT: String,
    val subtotal: Double,
    val vat: Double,
    val grandTotal: Double,
    val status: String = "Unpaid", // Unpaid, Part Paid, Paid
    val paidAmount: Double = 0.0,
    val lineItemsJson: String
)

data class PaymentRecord(
    val amount: Double,
    val date: String,
    val ref: String
)

data class AppSettings(
    val companyName: String = "Yalla Muv Company",
    val cr: String = "7054030577",
    val vat: String = "314724412300003",
    val vatRate: Double = 15.0,
    val freeWaitingHours: Double = 2.0,
    val detentionRate: Double = 100.0,
    val detentionCap: Double = 500.0,
    val dropRate: Double = 100.0,
    val paymentTermsDays: Int = 30,
    val minMarginPct: Double = 15.0,
    val expiryWindowDays: Int = 30,
    val invoicePrefix: String = "YM-INV-2026-",
    val invoiceCounter: Int = 105,
    val quotePrefix: String = "YM-QT-2026-",
    val quoteCounter: Int = 105,
    val opPrefix: String = "YM-SHP-",
    val opCounter: Int = 105,
    val geminiKey: String = "",
    val markup: Double = 30.0
)
