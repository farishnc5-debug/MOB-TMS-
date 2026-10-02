package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.ForecastRepository
import com.example.data.remote.GeminiRepository
import com.example.data.remote.ParsedShipmentAiResult
import com.example.util.ZatcaTlvEncoder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class DailyVolume(val isoDate: String, val count: Int)

data class ForecastDay(val isoDate: String, val point: Double, val low: Double, val high: Double)

sealed interface ForecastUiState {
    data object Idle : ForecastUiState
    data object Loading : ForecastUiState
    data class Success(val history: List<DailyVolume>, val forecast: List<ForecastDay>) : ForecastUiState
    data class Error(val message: String) : ForecastUiState
}

class TmsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).tmsDao()
    private val geminiRepo = GeminiRepository()
    private val forecastRepo = ForecastRepository()

    private val operationDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // StateFlows from DB
    val clients: StateFlow<List<ClientEntity>> = dao.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendors: StateFlow<List<VendorEntity>> = dao.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vendorRates: StateFlow<List<VendorRateEntity>> = dao.getAllVendorRates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tariffOverrides: StateFlow<List<TariffOverride>> = dao.getAllTariffOverrides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotations: StateFlow<List<QuotationEntity>> = dao.getAllQuotations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operations: StateFlow<List<OperationEntity>> = dao.getAllOperations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = dao.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _selectedHub = MutableStateFlow("Jeddah")
    val selectedHub: StateFlow<String> = _selectedHub.asStateFlow()

    private val _selectedTruckType = MutableStateFlow("4-Ton Reefer Chilled")
    val selectedTruckType: StateFlow<String> = _selectedTruckType.asStateFlow()

    private val _isRateEditMode = MutableStateFlow(false)
    val isRateEditMode: StateFlow<Boolean> = _isRateEditMode.asStateFlow()

    private val _aiParsingState = MutableStateFlow<String?>(null)
    val aiParsingState: StateFlow<String?> = _aiParsingState.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatHistory: StateFlow<List<Pair<String, String>>> = _aiChatHistory.asStateFlow()

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Idle)
    val forecastState: StateFlow<ForecastUiState> = _forecastState.asStateFlow()

    // Status names
    val statusStages = listOf(
        "Dispatched",
        "At Loading Location",
        "In Transit",
        "At Destination Waiting",
        "Offloaded & Left",
        "Finished"
    )

    fun setSelectedHub(hub: String) { _selectedHub.value = hub }
    fun setSelectedTruckType(truck: String) { _selectedTruckType.value = truck }
    fun toggleRateEditMode() { _isRateEditMode.value = !_isRateEditMode.value }
    fun setMarkup(value: Double) { _settings.value = _settings.value.copy(markup = value) }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
    }

    // Tariff Calculations
    fun getEffectiveCost(hub: String, truck: String, destName: String, defaultCost: Double, overrides: List<TariffOverride>): Double {
        val override = overrides.find { it.hub == hub && it.truckType == truck && it.destination == destName }
        return override?.cost ?: defaultCost
    }

    fun saveTariffOverride(hub: String, truck: String, destName: String, cost: Double) {
        viewModelScope.launch {
            val key = "${hub}_${truck}_$destName"
            dao.insertTariffOverride(TariffOverride(key, hub, truck, destName, cost))
        }
    }

    // Clients
    fun addClient(client: ClientEntity) {
        viewModelScope.launch { dao.insertClient(client) }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch { dao.deleteClient(client) }
    }

    // Vendors
    fun addVendor(vendor: VendorEntity) {
        viewModelScope.launch { dao.insertVendor(vendor) }
    }

    fun deleteVendor(vendor: VendorEntity) {
        viewModelScope.launch { dao.deleteVendor(vendor) }
    }

    fun saveVendorRate(vendorId: Int, hub: String, truckType: String, destination: String, cost: Double) {
        viewModelScope.launch {
            val key = "${vendorId}_${hub}_${truckType}_$destination"
            dao.insertVendorRate(VendorRateEntity(key, vendorId, hub, truckType, destination, cost))
        }
    }

    fun deleteVendorRate(rateId: String) {
        viewModelScope.launch { dao.deleteVendorRateById(rateId) }
    }

    fun getVendorCostForRoute(vendorId: Int, hub: String, truckType: String, destination: String, fallbackCost: Double): Double {
        val rate = vendorRates.value.find { it.vendorId == vendorId && it.hub == hub && it.truckType == truckType && it.destination == destination }
        return rate?.cost ?: fallbackCost
    }

    // Quotation Creation
    fun createQuotation(
        originHub: String,
        truckType: String,
        clientName: String,
        contactPerson: String,
        phone: String,
        email: String,
        validUntil: String,
        lineItems: List<LineItemData>,
        subtotal: Double,
        vat: Double,
        grandTotal: Double
    ) {
        viewModelScope.launch {
            val currSettings = _settings.value
            val refNo = "${currSettings.quotePrefix}${String.format(Locale.US, "%04d", currSettings.quoteCounter)}"
            _settings.value = currSettings.copy(quoteCounter = currSettings.quoteCounter + 1)

            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val dateStr = sdf.format(Date())

            val itemsArray = JSONArray()
            lineItems.forEach { item ->
                itemsArray.put(JSONObject().apply {
                    put("destination", item.destination)
                    put("distance", item.distance)
                    put("transitTime", item.transitTime)
                    put("extraDrops", item.extraDrops)
                    put("waitingHours", item.waitingHours)
                    put("lineTotal", item.lineTotal)
                })
            }

            val entity = QuotationEntity(
                refNo = refNo,
                date = dateStr,
                originHub = originHub,
                truckType = truckType,
                clientName = clientName,
                contactPerson = contactPerson,
                phone = phone,
                email = email,
                validUntil = validUntil,
                subtotal = subtotal,
                vat = vat,
                grandTotal = grandTotal,
                lineItemsJson = itemsArray.toString()
            )
            dao.insertQuotation(entity)
        }
    }

    fun clearQuotations() {
        viewModelScope.launch { dao.clearQuotations() }
    }

    // Dispatch Operation
    fun dispatchNewShipment(
        originHub: String,
        truckType: String,
        destination: String,
        distance: Int,
        clientName: String,
        contact: String,
        phone: String,
        extraDrops: Int,
        waitingHours: Int,
        vendor: VendorEntity,
        baseCost: Double,
        markupPct: Double
    ) {
        viewModelScope.launch {
            val currSettings = _settings.value
            val opId = "${currSettings.opPrefix}${String.format(Locale.US, "%04d", currSettings.opCounter)}"
            val refNo = "REF-${Calendar.getInstance().get(Calendar.YEAR)}-${String.format(Locale.US, "%04d", currSettings.opCounter)}"
            _settings.value = currSettings.copy(opCounter = currSettings.opCounter + 1)

            val transitTime = DefaultData.getEstimatedTransitTime(originHub, destination, distance)
            val sellingBase = Math.round(baseCost * (1 + markupPct / 100.0)).toDouble()
            val dropsCharge = extraDrops * currSettings.dropRate
            val billableWaiting = Math.max(0.0, waitingHours - currSettings.freeWaitingHours)
            val waitingCharge = if (billableWaiting > 0) {
                val raw = billableWaiting * currSettings.detentionRate
                if (currSettings.detentionCap > 0) Math.min(raw, currSettings.detentionCap) else raw
            } else 0.0

            val lineTotal = sellingBase + dropsCharge + waitingCharge
            val grandTotal = Math.round(lineTotal * (1 + currSettings.vatRate / 100.0)).toDouble()

            val lineItem = LineItemData(destination, distance, transitTime, extraDrops, waitingHours, null, lineTotal)
            val itemsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("destination", lineItem.destination)
                    put("distance", lineItem.distance)
                    put("transitTime", lineItem.transitTime)
                    put("extraDrops", lineItem.extraDrops)
                    put("waitingHours", lineItem.waitingHours)
                    put("lineTotal", lineItem.lineTotal)
                })
            }

            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val dateStr = sdf.format(Date())

            val op = OperationEntity(
                id = opId,
                refNo = refNo,
                date = dateStr,
                originHub = originHub,
                truckType = truckType,
                clientName = clientName,
                contact = contact,
                phone = phone,
                vendorName = vendor.name,
                vendorCost = vendor.cost,
                grandTotal = grandTotal,
                sellExVat = lineTotal,
                status = "Dispatched",
                statusIndex = 0,
                invoiced = false,
                lineItemsJson = itemsArray.toString()
            )
            dao.insertOperation(op)

            // Update vendor trips count
            dao.insertVendor(vendor.copy(trips = vendor.trips + 1))
        }
    }

    fun updateOperationStatus(op: OperationEntity, statusIdx: Int) {
        val newStatus = statusStages.getOrElse(statusIdx) { "Dispatched" }
        viewModelScope.launch {
            dao.insertOperation(op.copy(statusIndex = statusIdx, status = newStatus))
        }
    }

    fun deleteOperation(opId: String) {
        viewModelScope.launch { dao.deleteOperationById(opId) }
    }

    // Convert Operation to ZATCA Tax Invoice
    fun convertOperationToInvoice(op: OperationEntity) {
        viewModelScope.launch {
            val currSettings = _settings.value
            val invNo = "${currSettings.invoicePrefix}${String.format(Locale.US, "%04d", currSettings.invoiceCounter)}"
            _settings.value = currSettings.copy(invoiceCounter = currSettings.invoiceCounter + 1)

            val now = Date()
            val sdfIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            val sdfDate = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            val issuedAt = sdfIso.format(now)
            val dateStr = sdfDate.format(now)

            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, currSettings.paymentTermsDays) }
            val dueDateStr = sdfDate.format(cal.time)

            val subtotal = Math.round(op.grandTotal / (1 + currSettings.vatRate / 100.0)).toDouble()
            val vat = op.grandTotal - subtotal

            val invoice = InvoiceEntity(
                invNo = invNo,
                date = dateStr,
                issuedAt = issuedAt,
                dueDate = dueDateStr,
                opId = op.id,
                vendorCost = op.vendorCost,
                clientName = op.clientName,
                clientDetails = "Jeddah, Kingdom of Saudi Arabia",
                clientCR = currSettings.cr,
                clientVAT = currSettings.vat,
                subtotal = subtotal,
                vat = vat,
                grandTotal = op.grandTotal,
                status = "Unpaid",
                paidAmount = 0.0,
                lineItemsJson = op.lineItemsJson
            )

            dao.insertInvoice(invoice)
            dao.insertOperation(op.copy(invoiced = true, invoiceNo = invNo))
        }
    }

    // Payment Recording
    fun recordPayment(invoice: InvoiceEntity, paymentAmount: Double) {
        viewModelScope.launch {
            val newPaid = invoice.paidAmount + paymentAmount
            val newStatus = when {
                newPaid >= invoice.grandTotal - 0.5 -> "Paid"
                newPaid > 0 -> "Part Paid"
                else -> "Unpaid"
            }
            dao.insertInvoice(invoice.copy(paidAmount = newPaid, status = newStatus))
        }
    }

    // Gemini AI
    fun parsePromptWithGemini(promptText: String, onParsed: (ParsedShipmentAiResult) -> Unit) {
        viewModelScope.launch {
            _aiParsingState.value = "Parsing with Gemini AI..."
            val result = geminiRepo.parseShipmentPrompt(_settings.value.geminiKey, promptText)
            _aiParsingState.value = null
            onParsed(result)
        }
    }

    fun sendAiChatQuery(query: String) {
        val current = _aiChatHistory.value.toMutableList()
        current.add("You" to query)
        _aiChatHistory.value = current

        viewModelScope.launch {
            val answer = geminiRepo.generateContent(
                apiKeyOverride = _settings.value.geminiKey,
                systemInstruction = "You are an expert KSA freight and logistics consultant for Yalla Muv Company (CR: 7054030577, VAT: 314724412300003, Jeddah, KSA). Provide concise, professional advice on Saudi transport regulations (TGA, Wasl, Bayan, ZATCA e-invoicing).",
                prompt = query
            )
            val updated = _aiChatHistory.value.toMutableList()
            updated.add("Gemini Copilot" to answer)
            _aiChatHistory.value = updated
        }
    }

    // Demand Forecasting (TimesFM)
    private fun buildDailyVolumeSeries(hub: String?): List<DailyVolume> {
        val relevantOps = operations.value.filter { hub == null || it.originHub == hub }
        val parsedDates = relevantOps.mapNotNull { op -> runCatching { operationDateFormat.parse(op.date) }.getOrNull() }
        if (parsedDates.isEmpty()) return emptyList()

        val countsByIsoDate = parsedDates.groupingBy { isoDateFormat.format(it) }.eachCount()
        val sortedIsoDates = countsByIsoDate.keys.sorted()

        val calendar = Calendar.getInstance().apply { time = isoDateFormat.parse(sortedIsoDates.first())!! }
        val lastDate = isoDateFormat.parse(sortedIsoDates.last())!!

        val series = mutableListOf<DailyVolume>()
        while (!calendar.time.after(lastDate)) {
            val iso = isoDateFormat.format(calendar.time)
            series.add(DailyVolume(iso, countsByIsoDate[iso] ?: 0))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return series
    }

    fun runDemandForecast(hub: String?, horizonDays: Int) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading

            val history = buildDailyVolumeSeries(hub)
            if (history.size < 4) {
                _forecastState.value = ForecastUiState.Error(
                    "Need at least 4 days of shipment history to forecast (found ${history.size})."
                )
                return@launch
            }

            val series = history.map { it.count.toDouble() }
            forecastRepo.forecastDailyVolume(series, horizonDays).fold(
                onSuccess = { response ->
                    val calendar = Calendar.getInstance().apply { time = isoDateFormat.parse(history.last().isoDate)!! }
                    val forecastDays = response.pointForecast.mapIndexed { index, point ->
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        ForecastDay(
                            isoDate = isoDateFormat.format(calendar.time),
                            point = point,
                            low = response.quantiles["q10"]?.getOrNull(index) ?: point,
                            high = response.quantiles["q90"]?.getOrNull(index) ?: point
                        )
                    }
                    _forecastState.value = ForecastUiState.Success(history, forecastDays)
                },
                onFailure = { e ->
                    _forecastState.value = ForecastUiState.Error(
                        e.localizedMessage ?: "Forecast request failed. Is forecast-service running?"
                    )
                }
            )
        }
    }
}
