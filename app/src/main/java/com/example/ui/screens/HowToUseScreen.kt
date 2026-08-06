package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.Localization

data class GuideStep(
    val id: Int,
    val category: String,
    val titleKey: String,
    val descKey: String,
    val icon: ImageVector,
    val imageResId: Int?,
    val keyFeaturesEn: List<String>,
    val keyFeaturesAr: List<String>,
    val proTipEn: String,
    val proTipAr: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseScreen(
    appLanguage: AppLanguage,
    onNavigateToTab: (String) -> Unit
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val guideSteps = remember {
        listOf(
            GuideStep(
                id = 1,
                category = "DASHBOARD",
                titleKey = "step_1_title",
                descKey = "step_1_desc",
                icon = Icons.Default.Analytics,
                imageResId = R.drawable.dashboard_guide_demo_1786028083095,
                keyFeaturesEn = listOf(
                    "Real-time revenue & operational stats cards",
                    "Active shipment status overview (Pending vs In-Transit vs Delivered)",
                    "Quick action buttons to add quotes and waybills immediately",
                    "Recent Activity Log for operational compliance"
                ),
                keyFeaturesAr = listOf(
                    "بطاقات ملخص الإيرادات والإحصائيات التشغيلية المباشرة",
                    "نظرة عامة على حالة الشحنات النشطة (معلقة، في الطريق، تم التسليم)",
                    "أزرار وصول سريع لإنشاء العروض وبوليسات الشحن مباشرة",
                    "سجل النشاطات الأخيرة للامتثال والمتابعة التشغيلية"
                ),
                proTipEn = "Pro Tip: Check the Dashboard header daily for pending active dispatch orders needing driver assignment.",
                proTipAr = "نصيحة ذهبية: افحص أعلى لوحة التحكم يومياً لمعرفة طلبات الشحن النشطة التي تحتاج تعيين سائق."
            ),
            GuideStep(
                id = 2,
                category = "TARIFF",
                titleKey = "step_2_title",
                descKey = "step_2_desc",
                icon = Icons.Default.TableChart,
                imageResId = R.drawable.tariff_quote_guide_1786028098407,
                keyFeaturesEn = listOf(
                    "Dynamic Hub selection (Jeddah, Riyadh, Dammam)",
                    "Truck Category filter (13.5m Flatbed, 6m Dyna, 12m Reefer, Curtain, Lowbed)",
                    "Interactive Cost Override editing mode",
                    "Instant route distance and standard lead-time calculation"
                ),
                keyFeaturesAr = listOf(
                    "تحديد مركز الانطلاق الديناميكي (جدة، الرياض، الدمام)",
                    "تصفية فئات الشاحنات (تريلة 13.5م، لوري 6م، دينا 4م، ستارة، لوستار)",
                    "نموذج تعديل وحفظ أسعار التكلفة المخصصة للمشاوير",
                    "حساب فوري لمسافة الطريق وزمن الوصول القياسي"
                ),
                proTipEn = "Pro Tip: Enable 'Edit Rates' mode on the Tariff screen to override cost baselines for custom corporate accounts.",
                proTipAr = "نصيحة ذهبية: قم بتفعيل وضع 'تعديل الأسعار' لتحديث تكلفة الشحن وحفظها للعملاء المميزين."
            ),
            GuideStep(
                id = 3,
                category = "CRM",
                titleKey = "step_3_title",
                descKey = "step_3_desc",
                icon = Icons.Default.PointOfSale,
                imageResId = R.drawable.tariff_quote_guide_1786028098407,
                keyFeaturesEn = listOf(
                    "B2B Client directory with Commercial Register (CR) numbers",
                    "Dynamic profit markup % adjuster slider",
                    "One-click official quotation document generation with serial numbers",
                    "Direct conversion from quotation into active operation waybill"
                ),
                keyFeaturesAr = listOf(
                    "دليل حسابات الشركات B2B مع السجل التجاري وأرقام التواصل",
                    "مؤشر سلس لتحديد نسبة هامش الربح المستهدفة (Profit Margin %)",
                    "توليد مستند عرض سعر رسمي برقم تسلسلي بضغطة زر",
                    "تحويل مباشر لعرض السعر إلى أمر شحن وبوليسة عملية نشطة"
                ),
                proTipEn = "Pro Tip: Quotes include automated 15% KSA VAT breakdowns ready for formal client approval.",
                proTipAr = "نصيحة ذهبية: تتضمن عروض الأسعار تفصيلاً تلقائياً لضريبة القيمة المضافة 15% جاهزة للاعتماد."
            ),
            GuideStep(
                id = 4,
                category = "OPS",
                titleKey = "step_4_title",
                descKey = "step_4_desc",
                icon = Icons.AutoMirrored.Filled.AltRoute,
                imageResId = R.drawable.waybill_ops_guide_1786028113045,
                keyFeaturesEn = listOf(
                    "Waybill document preview with origin/destination routes",
                    "Driver & Truck license plate assignment",
                    "Status lifecycle tracking (Planned -> Loading -> In-Transit -> Finished)",
                    "Digital printable Waybill receipt generation"
                ),
                keyFeaturesAr = listOf(
                    "معاينة مستند بوليسة الشحن مع تفاصيل مسار الانطلاق والوصول",
                    "تعيين السائق ورقم لوحة الشاحنة واسم الناقل المساند",
                    "متابعة دورة حياة الشحنة (مخطط -> جاري التحميل -> في الطريق -> تم التسليم)",
                    "طباعة وتنزيل إيصال بوليسة الشحن الرقمية"
                ),
                proTipEn = "Pro Tip: Tap 'Print Waybill' on any active shipment to open the formatted official transport dispatch sheet.",
                proTipAr = "نصيحة ذهبية: انقر على 'طباعة البوليسة' لأي شحنة نشطة لفتح نموذج إذن الدفع والتحميل الرسمي."
            ),
            GuideStep(
                id = 5,
                category = "VENDORS",
                titleKey = "step_5_title",
                descKey = "step_5_desc",
                icon = Icons.Default.Badge,
                imageResId = R.drawable.waybill_ops_guide_1786028113045,
                keyFeaturesEn = listOf(
                    "3PL Vendor sub-contractor registration portal",
                    "Vendor specific cost negotiation agreements",
                    "Fleet capacity tracking across Saudi logistics corridors",
                    "Vendor financial ledger & payment balances"
                ),
                keyFeaturesAr = listOf(
                    "بوابة تسجيل شركات النقليات والموردين المساندين 3PL",
                    "سجل أسعار التكلفة المتفق عليها لكل مورد بحسب المسار",
                    "متابعة توفر الشاحنات على محاور الطرق الرئيسية بالمملكة",
                    "كشف حساب مالي ومستحقات الموردين"
                ),
                proTipEn = "Pro Tip: Register top vendors first to unlock maximum profit margins during quotation build.",
                proTipAr = "نصيحة ذهبية: سجل الموردين المعتمدين أولاً للحصول على أفضل التكاليف عند إعداد عروض الأسعار."
            ),
            GuideStep(
                id = 6,
                category = "INVOICES",
                titleKey = "step_6_title",
                descKey = "step_6_desc",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                imageResId = R.drawable.ai_invoice_guide_1786028125776,
                keyFeaturesEn = listOf(
                    "Official Tax Invoice generation with 15% VAT",
                    "Gemini AI Document Scanner for automatic PDF quote/waybill parsing",
                    "AI Logistics Assistant chat window for smart queries",
                    "Payment tracking and balance status (Paid vs Pending)"
                ),
                keyFeaturesAr = listOf(
                    "إصدار الفواتير الضريبية الرسمية وفق متطلبات هيئة الزكاة والضريبة 15%",
                    "الماسح الضوئي بالذكاء الاصطناعي قوقل جيميني لقراءة طلبات الشحن تلقائياً",
                    "مساعد الذكاء الاصطناعي اللوجستي للإجابة والاستفسار الفوري",
                    "تسجيل المدفوعات وحساب المتبقي"
                ),
                proTipEn = "Pro Tip: Paste text or upload RFQ documents in the AI tab to auto-extract routes, cargo weight, and pricing!",
                proTipAr = "نصيحة ذهبية: انسخ نص الطلب أو ارفق صورة الطلب في تبويب الذكاء الاصطناعي لاستخراج البيانات فوراً!"
            )
        )
    }

    val categories = remember {
        listOf(
            "ALL" to if (isAr) "الكل" else "All Steps",
            "DASHBOARD" to if (isAr) "لوحة التحكم" else "Dashboard",
            "TARIFF" to if (isAr) "التعرفة" else "Tariff",
            "CRM" to if (isAr) "المبيعات" else "CRM & Quotes",
            "OPS" to if (isAr) "العمليات" else "Operations",
            "VENDORS" to if (isAr) "الموردين" else "Vendors",
            "INVOICES" to if (isAr) "الذكاء الاصطناعي" else "AI & Invoices"
        )
    }

    val filteredSteps = remember(searchQuery, selectedCategory) {
        guideSteps.filter { step ->
            val matchesCategory = selectedCategory == "ALL" || step.category == selectedCategory
            val titleText = Localization.getString(step.titleKey, appLanguage)
            val descText = Localization.getString(step.descKey, appLanguage)
            val matchesSearch = searchQuery.isBlank() ||
                    titleText.contains(searchQuery, ignoreCase = true) ||
                    descText.contains(searchQuery, ignoreCase = true) ||
                    step.keyFeaturesEn.any { it.contains(searchQuery, ignoreCase = true) } ||
                    step.keyFeaturesAr.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Help,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Localization.getString("guide_title", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = Localization.getString("guide_subtitle", appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isAr) "ابحث عن أي خطوة أو ميزة في التطبيق..." else "Search any step or feature...",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = catKey },
                            label = { Text(catLabel, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Steps List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredSteps, key = { it.id }) { step ->
                GuideStepCard(
                    step = step,
                    appLanguage = appLanguage,
                    onNavigateToTab = onNavigateToTab
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun GuideStepCard(
    step: GuideStep,
    appLanguage: AppLanguage,
    onNavigateToTab: (String) -> Unit
) {
    val isAr = appLanguage == AppLanguage.ARABIC
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = Localization.getString(step.titleKey, appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAr) "خطوة توضيحية #${step.id}" else "Step Demonstration #${step.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

                    // Photo Demonstration / Illustration Image
                    step.imageResId?.let { resId ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Step Demonstration Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay Badge
                            Surface(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopStart),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (isAr) "شاشة توضيحية" else "Visual Demo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Description
                    Text(
                        text = Localization.getString(step.descKey, appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    // Key Features Checklist
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isAr) "أبرز المزايا والخصائص:" else "Key Features & Functions:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val features = if (isAr) step.keyFeaturesAr else step.keyFeaturesEn
                        features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Pro Tip Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAr) step.proTipAr else step.proTipEn,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Navigation Action Button
                    Button(
                        onClick = { onNavigateToTab(step.category) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAr) "الانتقال إلى الشاشة الآن" else "Open Feature Screen Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
