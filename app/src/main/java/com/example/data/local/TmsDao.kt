package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TmsDao {
    // Clients
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    // Vendors
    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR crNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchVendors(query: String): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE city = :city ORDER BY name ASC")
    fun getVendorsByCity(city: String): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :vendorId LIMIT 1")
    suspend fun getVendorById(vendorId: Int): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Delete
    suspend fun deleteVendor(vendor: VendorEntity)

    // Vendor Custom Rates
    @Query("SELECT * FROM vendor_rates")
    fun getAllVendorRates(): Flow<List<VendorRateEntity>>

    @Query("SELECT * FROM vendor_rates WHERE vendorId = :vendorId")
    fun getRatesForVendor(vendorId: Int): Flow<List<VendorRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendorRate(rate: VendorRateEntity)

    @Query("DELETE FROM vendor_rates WHERE id = :rateId")
    suspend fun deleteVendorRateById(rateId: String)

    // Tariff Overrides
    @Query("SELECT * FROM tariff_overrides")
    fun getAllTariffOverrides(): Flow<List<TariffOverride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTariffOverride(override: TariffOverride)

    // Quotations
    @Query("SELECT * FROM quotations ORDER BY refNo DESC")
    fun getAllQuotations(): Flow<List<QuotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity)

    @Query("DELETE FROM quotations")
    suspend fun clearQuotations()

    // Operations (Shipments)
    @Query("SELECT * FROM operations ORDER BY id DESC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE status = :status ORDER BY id DESC")
    fun getOperationsByStatus(status: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE vendorName = :vendorName ORDER BY id DESC")
    fun getOperationsByVendor(vendorName: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE clientName = :clientName ORDER BY id DESC")
    fun getOperationsByClient(clientName: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE invoiced = :invoiced ORDER BY id DESC")
    fun getOperationsByInvoiceState(invoiced: Boolean): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE id LIKE '%' || :query || '%' OR refNo LIKE '%' || :query || '%' OR clientName LIKE '%' || :query || '%' OR vendorName LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchOperations(query: String): Flow<List<OperationEntity>>

    @Query("SELECT COUNT(*) FROM operations WHERE status = :status")
    fun getOperationCountByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Query("DELETE FROM operations WHERE id = :opId")
    suspend fun deleteOperationById(opId: String)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY invNo DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY invNo DESC")
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientName = :clientName ORDER BY invNo DESC")
    fun getInvoicesByClient(clientName: String): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)
}
