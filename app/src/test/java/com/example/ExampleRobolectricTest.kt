package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.InvoiceEntity
import com.example.data.model.OperationEntity
import com.example.data.model.QuotationEntity
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.Localization
import com.example.util.CsvExporter
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yalla Muv", appName)
  }

  @Test
  fun `test localization helper for English and Arabic`() {
    val dashboardEn = Localization.getString("tab_dashboard", AppLanguage.ENGLISH)
    val dashboardAr = Localization.getString("tab_dashboard", AppLanguage.ARABIC)

    assertEquals("Dashboard", dashboardEn)
    assertEquals("لوحة التحكم", dashboardAr)

    val appTitleEn = Localization.getString("app_title", AppLanguage.ENGLISH)
    val appTitleAr = Localization.getString("app_title", AppLanguage.ARABIC)

    assertEquals("Yalla Muv Company", appTitleEn)
    assertEquals("شركة يلا موڤ للنقل", appTitleAr)
  }

  @Test
  fun `test csv exporter for shipments operations`() {
    val sampleOps = listOf(
      OperationEntity(
        id = "OP-2026-101",
        refNo = "QT-2026-001",
        date = "2026-08-01",
        originHub = "Jeddah Islamic Port",
        truckType = "3T Dyno Box Truck",
        clientName = "Saudi Aramco Logistics",
        contact = "Ahmed Al-Ghamdi",
        phone = "+966 50 123 4567",
        vendorName = "Almajdouie Logistics",
        vendorCost = 1200.0,
        sellExVat = 1500.0,
        grandTotal = 1725.0,
        status = "In-Transit",
        invoiceNo = "INV-2026-001",
        lineItemsJson = "[]"
      )
    )

    val csv = CsvExporter.exportShipmentsCsv(sampleOps)

    assertTrue(csv.contains("OP-2026-101"))
    assertTrue(csv.contains("Saudi Aramco Logistics"))
    assertTrue(csv.contains("1725.00"))
  }

  @Test
  fun `test csv exporter for tax invoices`() {
    val sampleInvoices = listOf(
      InvoiceEntity(
        invNo = "INV-2026-001",
        date = "2026-08-01",
        issuedAt = "2026-08-01 10:00:00",
        dueDate = "2026-08-31",
        opId = "OP-2026-101",
        vendorCost = 1200.0,
        clientName = "Saudi Aramco Logistics",
        clientDetails = "Jeddah, Saudi Arabia",
        clientCR = "4030123456",
        clientVAT = "300123456700003",
        subtotal = 1500.0,
        vat = 225.0,
        grandTotal = 1725.0,
        paidAmount = 1725.0,
        status = "PAID",
        lineItemsJson = "[]"
      )
    )

    val csv = CsvExporter.exportInvoicesCsv(sampleInvoices)

    assertTrue(csv.contains("INV-2026-001"))
    assertTrue(csv.contains("300123456700003"))
    assertTrue(csv.contains("PAID"))
  }

  @Test
  fun `test csv exporter for quotations`() {
    val sampleQuotes = listOf(
      QuotationEntity(
        refNo = "QT-2026-001",
        date = "2026-08-01",
        validUntil = "2026-08-15",
        originHub = "Jeddah Port",
        truckType = "Flatbed 12m",
        clientName = "SABIC Petrochemicals",
        contactPerson = "Fahad Saleh",
        phone = "+966 55 987 6543",
        email = "fahad@sabic.com",
        subtotal = 2000.0,
        vat = 300.0,
        grandTotal = 2300.0,
        lineItemsJson = "[]"
      )
    )

    val csv = CsvExporter.exportQuotationsCsv(sampleQuotes)

    assertTrue(csv.contains("QT-2026-001"))
    assertTrue(csv.contains("SABIC Petrochemicals"))
    assertTrue(csv.contains("2300.00"))
  }

  @Test
  fun `test database dao indexing and search queries`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.data.local.AppDatabase::class.java).build()
    val dao = db.tmsDao()

    val vendor1 = com.example.data.model.VendorEntity(
      name = "Almajdouie Logistics",
      contact = "Sami",
      truck = "4-Ton Reefer",
      plate = "LMN-1234",
      cost = 1500.0,
      city = "Dammam",
      crNumber = "2050123456"
    )
    dao.insertVendor(vendor1)

    val op1 = OperationEntity(
      id = "OP-2026-999",
      refNo = "REF-2026-999",
      date = "2026-08-01",
      originHub = "Jeddah",
      truckType = "4-Ton Reefer",
      clientName = "P&G Saudi",
      contact = "Omar",
      phone = "0500000000",
      vendorName = "Almajdouie Logistics",
      vendorCost = 1500.0,
      grandTotal = 2100.0,
      sellExVat = 1826.0,
      status = "Dispatched",
      lineItemsJson = "[]"
    )
    dao.insertOperation(op1)

    val foundVendor = dao.getVendorById(1)
    assertEquals("Almajdouie Logistics", foundVendor?.name)

    val searchResult = dao.searchVendors("Almajdouie").first()
    assertEquals(1, searchResult.size)

    val opsResult = dao.getOperationsByStatus("Dispatched").first()
    assertEquals(1, opsResult.size)
    assertEquals("OP-2026-999", opsResult[0].id)

    db.close()
  }

  @Test
  fun `test 15 percent VAT and margin calculation math`() {
    val sellExVat = 2000.0
    val vatRate = 0.15
    val vatAmount = sellExVat * vatRate
    val grandTotal = sellExVat + vatAmount

    assertEquals(300.0, vatAmount, 0.001)
    assertEquals(2300.0, grandTotal, 0.001)

    val vendorCost = 1500.0
    val margin = sellExVat - vendorCost
    val marginPercentage = (margin / sellExVat) * 100.0

    assertEquals(500.0, margin, 0.001)
    assertEquals(25.0, marginPercentage, 0.001)
  }
}

