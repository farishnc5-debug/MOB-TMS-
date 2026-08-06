package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.Localization
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TmsViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TmsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var selectedLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }
            var selectedThemeMode by remember { mutableStateOf(com.example.ui.theme.ThemeMode.SYSTEM) }
            var selectedThemeStyle by remember { mutableStateOf(com.example.ui.theme.LayoutThemeStyle.NUDGE_NEO_BI) }
            var selectedNavMode by remember { mutableStateOf("BOTTOM_PILL") }

            val isAr = selectedLanguage == AppLanguage.ARABIC

            MyApplicationTheme(themeMode = selectedThemeMode, themeStyle = selectedThemeStyle) {
                CompositionLocalProvider(LocalLayoutDirection provides if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                val clients by viewModel.clients.collectAsStateWithLifecycle()
                val vendors by viewModel.vendors.collectAsStateWithLifecycle()
                val vendorRates by viewModel.vendorRates.collectAsStateWithLifecycle()
                val tariffOverrides by viewModel.tariffOverrides.collectAsStateWithLifecycle()
                val quotations by viewModel.quotations.collectAsStateWithLifecycle()
                val operations by viewModel.operations.collectAsStateWithLifecycle()
                val invoices by viewModel.invoices.collectAsStateWithLifecycle()
                val settings by viewModel.settings.collectAsStateWithLifecycle()

                val selectedHub by viewModel.selectedHub.collectAsStateWithLifecycle()
                val selectedTruckType by viewModel.selectedTruckType.collectAsStateWithLifecycle()
                val isRateEditMode by viewModel.isRateEditMode.collectAsStateWithLifecycle()

                val aiParsingState by viewModel.aiParsingState.collectAsStateWithLifecycle()
                val aiChatHistory by viewModel.aiChatHistory.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf("DASHBOARD") }

                // Dialog States
                var activeQuotePreview by remember { mutableStateOf<QuotationEntity?>(null) }
                var activeInvoiceView by remember { mutableStateOf<InvoiceEntity?>(null) }
                var activeWaybillView by remember { mutableStateOf<OperationEntity?>(null) }
                var activePaymentInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }
                var activeVendorAccount by remember { mutableStateOf<VendorEntity?>(null) }

                var showClientDirectory by remember { mutableStateOf(false) }
                var showRegisterVendor by remember { mutableStateOf(false) }
                var showSettings by remember { mutableStateOf(false) }
                var showLayoutOptions by remember { mutableStateOf(false) }

                val tabItems = listOf(
                    TabItem("DASHBOARD", Localization.getString("tab_dashboard", selectedLanguage), Icons.Default.Analytics),
                    TabItem("TARIFF", Localization.getString("tab_tariff", selectedLanguage), Icons.Default.TableChart),
                    TabItem("CRM", Localization.getString("tab_crm", selectedLanguage), Icons.Default.PointOfSale),
                    TabItem("OPS", "${Localization.getString("tab_ops", selectedLanguage)} (${operations.count { it.status != "Finished" }})", Icons.AutoMirrored.Filled.AltRoute),
                    TabItem("VENDORS", Localization.getString("tab_vendors", selectedLanguage), Icons.Default.Badge),
                    TabItem("INVOICES", Localization.getString("tab_invoices", selectedLanguage), Icons.AutoMirrored.Filled.ReceiptLong),
                    TabItem("HOW_TO_USE", Localization.getString("tab_how_to_use", selectedLanguage), Icons.AutoMirrored.Filled.Help)
                )

                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column {
                                            Text(
                                                Localization.getString("app_title", selectedLanguage),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                String.format(Localization.getString("app_subtitle", selectedLanguage), settings.cr),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Language Quick Switcher Toggle Button
                                    OutlinedButton(
                                        onClick = {
                                            selectedLanguage = if (selectedLanguage == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = if (selectedLanguage == AppLanguage.ENGLISH) "🇸🇦 عربي" else "🇬🇧 EN",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(onClick = {
                                        selectedThemeMode = when (selectedThemeMode) {
                                            com.example.ui.theme.ThemeMode.DARK -> com.example.ui.theme.ThemeMode.LIGHT
                                            com.example.ui.theme.ThemeMode.LIGHT -> com.example.ui.theme.ThemeMode.SYSTEM
                                            com.example.ui.theme.ThemeMode.SYSTEM -> com.example.ui.theme.ThemeMode.DARK
                                        }
                                    }) {
                                        Icon(
                                            when (selectedThemeMode) {
                                                com.example.ui.theme.ThemeMode.DARK -> Icons.Default.DarkMode
                                                com.example.ui.theme.ThemeMode.LIGHT -> Icons.Default.LightMode
                                                com.example.ui.theme.ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                            },
                                            contentDescription = "Toggle Light/Dark Theme Mode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { showLayoutOptions = true }) {
                                        Icon(Icons.Default.Palette, contentDescription = "Layout Options", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { showClientDirectory = true }) {
                                        Icon(Icons.Default.Contacts, contentDescription = "Clients")
                                    }
                                    IconButton(onClick = { showSettings = true }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            // Top Header Navigation Bar Option
                            if (selectedNavMode == "TOP_HEADER") {
                                ScrollableTabRow(
                                    selectedTabIndex = tabItems.indexOfFirst { it.id == currentTab }.coerceAtLeast(0),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    edgePadding = 8.dp
                                ) {
                                    tabItems.forEach { tab ->
                                        Tab(
                                            selected = currentTab == tab.id,
                                            onClick = { currentTab = tab.id },
                                            text = { Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (selectedNavMode == "BOTTOM_PILL") {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                tabItems.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab.id,
                                        onClick = { currentTab = tab.id },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, fontSize = 9.sp, maxLines = 1) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Side Rail Navigation Mode
                        if (selectedNavMode == "SIDE_RAIL") {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                tabItems.forEach { tab ->
                                    NavigationRailItem(
                                        selected = currentTab == tab.id,
                                        onClick = { currentTab = tab.id },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, fontSize = 9.sp, maxLines = 1) }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                            "DASHBOARD" -> DashboardScreen(
                                operations = operations,
                                invoices = invoices,
                                vendors = vendors,
                                settings = settings
                            )

                            "TARIFF" -> TariffScreen(
                                selectedHub = selectedHub,
                                selectedTruckType = selectedTruckType,
                                isRateEditMode = isRateEditMode,
                                tariffOverrides = tariffOverrides,
                                settings = settings,
                                onHubSelected = { viewModel.setSelectedHub(it) },
                                onTruckTypeSelected = { viewModel.setSelectedTruckType(it) },
                                onToggleEditMode = { viewModel.toggleRateEditMode() },
                                onSaveCostOverride = { hub, truck, dest, cost ->
                                    viewModel.saveTariffOverride(hub, truck, dest, cost)
                                }
                            )

                            "CRM" -> CrmScreen(
                                clients = clients,
                                selectedHub = selectedHub,
                                selectedTruckType = selectedTruckType,
                                tariffOverrides = tariffOverrides,
                                settings = settings,
                                onHubSelected = { viewModel.setSelectedHub(it) },
                                onTruckTypeSelected = { viewModel.setSelectedTruckType(it) },
                                onCreateQuotation = { quote ->
                                    viewModel.createQuotation(
                                        quote.originHub,
                                        quote.truckType,
                                        quote.clientName,
                                        quote.contactPerson,
                                        quote.phone,
                                        quote.email,
                                        quote.validUntil,
                                        emptyList(),
                                        quote.subtotal,
                                        quote.vat,
                                        quote.grandTotal
                                    )
                                },
                                onDispatchShipment = { hub, truck, dest, dist, cName, cContact, cPhone, drops, wait, cost, markup ->
                                    val vendor = vendors.firstOrNull() ?: DefaultData.initialVendors.first()
                                    viewModel.dispatchNewShipment(
                                        hub, truck, dest, dist, cName, cContact, cPhone, drops, wait, vendor, cost, markup
                                    )
                                    currentTab = "OPS"
                                },
                                onPreviewQuote = { quote ->
                                    activeQuotePreview = quote
                                }
                            )

                            "OPS" -> OperationsScreen(
                                operations = operations,
                                vendors = vendors,
                                settings = settings,
                                statusStages = viewModel.statusStages,
                                onUpdateStatus = { op, stageIdx ->
                                    viewModel.updateOperationStatus(op, stageIdx)
                                },
                                onConvertInvoice = { op ->
                                    viewModel.convertOperationToInvoice(op)
                                    currentTab = "INVOICES"
                                },
                                onViewWaybill = { op ->
                                    activeWaybillView = op
                                },
                                onDeleteOperation = { opId ->
                                    viewModel.deleteOperation(opId)
                                }
                            )

                            "VENDORS" -> VendorsScreen(
                                vendors = vendors,
                                vendorRates = vendorRates,
                                onRegisterVendor = { showRegisterVendor = true },
                                onSelectVendorAccount = { vendor -> activeVendorAccount = vendor },
                                onDeleteVendor = { vendor -> viewModel.deleteVendor(vendor) }
                            )

                            "INVOICES" -> InvoicesAndAiScreen(
                                invoices = invoices,
                                quotations = quotations,
                                aiParsingState = aiParsingState,
                                aiChatHistory = aiChatHistory,
                                onViewInvoice = { inv -> activeInvoiceView = inv },
                                onViewQuotation = { q -> activeQuotePreview = q },
                                onRecordPayment = { inv -> activePaymentInvoice = inv },
                                onParsePrompt = { prompt, onParsed ->
                                    viewModel.parsePromptWithGemini(prompt, onParsed)
                                },
                                onSendAiChat = { query ->
                                    viewModel.sendAiChatQuery(query)
                                }
                            )

                            "HOW_TO_USE" -> HowToUseScreen(
                                appLanguage = selectedLanguage,
                                onNavigateToTab = { target ->
                                    currentTab = when (target) {
                                        "DASHBOARD" -> "DASHBOARD"
                                        "TARIFF" -> "TARIFF"
                                        "CRM" -> "CRM"
                                        "OPS" -> "OPS"
                                        "VENDORS" -> "VENDORS"
                                        "INVOICES" -> "INVOICES"
                                        else -> "DASHBOARD"
                                    }
                                }
                            )
                        }
                    }

                    // Dialog Overlays
                    activeQuotePreview?.let { quote ->
                        BilingualQuotationDialog(
                            quote = quote,
                            settings = settings,
                            onDismiss = { activeQuotePreview = null }
                        )
                    }

                    activeInvoiceView?.let { invoice ->
                        TaxInvoiceDialog(
                            invoice = invoice,
                            settings = settings,
                            onDismiss = { activeInvoiceView = null }
                        )
                    }

                    activeWaybillView?.let { op ->
                        WaybillDialog(
                            operation = op,
                            settings = settings,
                            onDismiss = { activeWaybillView = null }
                        )
                    }

                    activePaymentInvoice?.let { inv ->
                        RecordPaymentDialog(
                            invoice = inv,
                            onSave = { amount ->
                                viewModel.recordPayment(inv, amount)
                            },
                            onDismiss = { activePaymentInvoice = null }
                        )
                    }

                    activeVendorAccount?.let { vendor ->
                        VendorAccountDialog(
                            vendor = vendor,
                            vendorRates = vendorRates,
                            onSaveRate = { hub, truckType, destination, cost ->
                                viewModel.saveVendorRate(vendor.id, hub, truckType, destination, cost)
                            },
                            onDeleteRate = { rateId ->
                                viewModel.deleteVendorRate(rateId)
                            },
                            onDismiss = { activeVendorAccount = null }
                        )
                    }

                    if (showClientDirectory) {
                        ClientDirectoryDialog(
                            clients = clients,
                            onAddClient = { c -> viewModel.addClient(c) },
                            onDeleteClient = { c -> viewModel.deleteClient(c) },
                            onDismiss = { showClientDirectory = false }
                        )
                    }

                    if (showRegisterVendor) {
                        RegisterVendorDialog(
                            onSave = { v -> viewModel.addVendor(v) },
                            onDismiss = { showRegisterVendor = false }
                        )
                    }

                    if (showSettings) {
                        SetupSettingsDialog(
                            settings = settings,
                            onSave = { newSettings -> viewModel.updateSettings(newSettings) },
                            onDismiss = { showSettings = false }
                        )
                    }

                    if (showLayoutOptions) {
                        LayoutOptionsDialog(
                            currentLanguage = selectedLanguage,
                            currentThemeMode = selectedThemeMode,
                            currentThemeStyle = selectedThemeStyle,
                            currentNavMode = selectedNavMode,
                            onSelectLanguage = { lang -> selectedLanguage = lang },
                            onSelectThemeMode = { mode -> selectedThemeMode = mode },
                            onSelectThemeStyle = { style -> selectedThemeStyle = style },
                            onSelectNavMode = { mode -> selectedNavMode = mode },
                            onDismiss = { showLayoutOptions = false }
                        )
                    }
                }
                }
            }
        }
    }
}
}

data class TabItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

