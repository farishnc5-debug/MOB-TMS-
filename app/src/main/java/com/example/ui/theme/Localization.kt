package com.example.ui.theme

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇬🇧"),
    ARABIC("ar", "العربية", "🇸🇦")
}

object Localization {
    fun getString(key: String, language: AppLanguage): String {
        val isAr = language == AppLanguage.ARABIC
        return when (key) {
            "app_title" -> if (isAr) "شركة يلا موڤ للنقل" else "Yalla Muv Company"
            "app_subtitle" -> if (isAr) "سجل تجاري: %s | جدة، المملكة العربية السعودية" else "CR: %s | Jeddah, KSA"
            
            // Tabs
            "tab_dashboard" -> if (isAr) "لوحة التحكم" else "Dashboard"
            "tab_tariff" -> if (isAr) "جدول التعرفة" else "Tariff"
            "tab_crm" -> if (isAr) "المبيعات والعروض" else "CRM & Quotes"
            "tab_ops" -> if (isAr) "العمليات الشحنات" else "Operations"
            "tab_vendors" -> if (isAr) "الموردين والأسطول" else "Vendors"
            "tab_invoices" -> if (isAr) "الفواتير والذكاء الاصطناعي" else "Invoices & AI"
            "tab_how_to_use" -> if (isAr) "كيفية الاستخدام" else "How to Use"

            // Buttons & Actions
            "toggle_lang" -> if (isAr) "ENGLISH" else "عربي"
            "layout_options" -> if (isAr) "خيارات المظهر واللغة" else "Layout & Language Options"
            "settings" -> if (isAr) "الإعدادات" else "Settings"
            "clients" -> if (isAr) "العملاء" else "Clients"
            "apply" -> if (isAr) "تطبيق وإغلاق" else "Apply & Close"
            
            // How to Use Guide
            "guide_title" -> if (isAr) "دليل استخدام تطبيق يلا موڤ اللوجستي" else "Yalla Muv Logistics Platform - User Guide"
            "guide_subtitle" -> if (isAr) "خطوات توضيحية مفصلة مع الصور للتحكم بالتعرفة، الشحنات، العروض والذكاء الاصطناعي" else "Comprehensive step-by-step instructions and photo demonstrations for logistics management"
            
            // Steps
            "step_1_title" -> if (isAr) "١. لوحة التحكم والتحليلات اللوجستية" else "1. Logistics Dashboard & Analytics"
            "step_1_desc" -> if (isAr) "مراقبة الأداء المالي، إجمالي الشحنات النشطة، الفواتير المستحقة، وتوزيع الأسطول في جدة والرياض والدمام." else "Monitor financial KPIs, active shipments count, pending invoices, and fleet distribution across KSA hubs."
            
            "step_2_title" -> if (isAr) "٢. إدارة جدول التعرفة وأسعار الشاحنات" else "2. Tariff Matrix & Truck Costing"
            "step_2_desc" -> if (isAr) "اختر مركز الانطلاق (جدة، الرياض، الدمام) ونوع الشاحنة (تريلة 13.5m، لوري 6m، دينا 4m). يمكنك تعديل الأسعار وحفظها مباشرة." else "Select origin hub and vehicle type (13.5m Flatbed, 6m Dyna, 12m Reefer). Dynamic price editing with real-time override storage."
            
            "step_3_title" -> if (isAr) "٣. إدارة العملاء وإنشاء عروض الأسعار (CRM)" else "3. CRM & B2B Quote Generation"
            "step_3_desc" -> if (isAr) "إضافة بيانات الشركات والعملاء، تحديد نسبة الربح (Profit Margin %)، وتوليد عرض سعر رسمي برقم تسلسلي وطباعته أو مشاركته." else "Register B2B client profiles, set target markup margins %, and generate official quotations with automated serial numbers."
            
            "step_4_title" -> if (isAr) "٤. إدارة العمليات وبوليسات الشحن (Waybills)" else "4. Operations & Waybill Tracking"
            "step_4_desc" -> if (isAr) "تحويل العروض المقبولة إلى أمر شحن، تعيين المورد والسائق ورقم لوحة الشاحنة، ومتابعة حالة الطريق (قيد التحميل، في الطريق، تم التسليم)." else "Convert quotes to operational orders, assign vendor drivers & truck license plates, and update live dispatch statuses."
            
            "step_5_title" -> if (isAr) "٥. بوابه الموردين والأسطول المساند" else "5. Vendor Portal & Fleet Network"
            "step_5_desc" -> if (isAr) "تسجيل نقليات الموردين، توثيق أسعار التكلفة لكل خط سير، وإدارة الحسابات المالية للموردين." else "Register 3PL vendor transport companies, track negotiated cost baselines per route, and view balance summaries."
            
            "step_6_title" -> if (isAr) "٦. الفواتير والماسح الضوئي بالذكاء الاصطناعي" else "6. Smart Invoices & AI Document Scanner"
            "step_6_desc" -> if (isAr) "إصدار الفواتير الضريبية مع حساب ضريبة القيمة المضافة 15%. يمكنك استخدام الذكاء الاصطناعي لاستخراج بيانات طلبات الشحن من ملفات PDF أو الصور تلقائياً." else "Issue tax invoices with 15% VAT calculation. Use Gemini AI OCR to parse shipment request PDFs/images automatically into structured data."

            else -> key
        }
    }
}
