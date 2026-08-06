package com.example.data.model

object DefaultData {
    val defaultDestinations = listOf(
        DestinationItem("Mecca", "مكة المكرمة", 85, 500.0),
        DestinationItem("Taif", "الطائف", 170, 700.0),
        DestinationItem("Yanbu", "ينبع", 330, 900.0),
        DestinationItem("Madinah", "المدينة المنورة", 340, 1100.0),
        DestinationItem("Yanbu Al-Sinaiyah", "ينبع الصناعية", 360, 900.0),
        DestinationItem("Abha", "أبها", 610, 1200.0),
        DestinationItem("Jizan", "جيزان", 710, 1200.0),
        DestinationItem("Buraydah (Qassim)", "بريدة (القصيم)", 900, 1200.0),
        DestinationItem("Riyadh", "الرياض", 950, 1200.0),
        DestinationItem("Ar-Rass", "الرس", 950, 1200.0),
        DestinationItem("Hail", "حائل", 1000, 1200.0),
        DestinationItem("Al-Kharj", "الخرج", 1000, 1200.0),
        DestinationItem("NEOM (future city)", "نيوم", 1100, 1700.0),
        DestinationItem("Tabuk", "تبوك", 1140, 1700.0),
        DestinationItem("Khobar", "الخبر", 1320, 1700.0),
        DestinationItem("Dhahran", "الظهران", 1330, 1700.0),
        DestinationItem("Dammam", "الدمام", 1350, 1700.0),
        DestinationItem("Ras Tanura", "راس تنورة", 1380, 1700.0),
        DestinationItem("Al-Hasa (Hofuf)", "الأحساء (الهفوف)", 1400, 1700.0),
        DestinationItem("Najran", "نجران", 1400, 1700.0),
        DestinationItem("Sakaka (Al-Jouf)", "سكاكا (الجوف)", 1400, 1700.0),
        DestinationItem("Khafji", "الخفجي", 1440, 1700.0),
        DestinationItem("Arar", "عرعر", 1600, 1700.0),
        DestinationItem("Jeddah", "جدة", 30, 400.0),
        DestinationItem("Riyadh City", "مدينة الرياض", 25, 400.0),
        DestinationItem("Dammam City", "مدينة الدمام", 20, 400.0),
        DestinationItem("Madinah City", "مدينة المدينة المنورة", 25, 400.0)
    )

    val truckTypes = listOf(
        "4-Ton Reefer Chilled",
        "4-Ton Reefer Freezer",
        "4-Ton Open Top",
        "4-Ton Closed Box",
        "40ft Flatbed",
        "40ft Curtain Side",
        "Lowbed"
    )

    val hubs = listOf("Jeddah", "Riyadh", "Dammam")

    val initialClients = listOf(
        ClientEntity(
            id = 1,
            name = "Al-Manar Fresh Foods LLC / شركة المنار للأغذية الطازجة",
            contact = "Mr. Tariq Al-Otaibi",
            phone = "+966 50 123 4567",
            email = "tariq@almanarfoods.sa",
            cr = "7054030577",
            vat = "314724412300003",
            building = "1234",
            street = "King Fahd Road",
            district = "Al-Rehab Dist.",
            city = "Jeddah",
            postal = "23345"
        ),
        ClientEntity(
            id = 2,
            name = "Al-Marai Logistics / شركة المراعي للخدمات اللوجستية",
            contact = "Mr. Ahmed Al-Harbi",
            phone = "+966 55 987 6543",
            email = "ahmed@almarai.sa",
            cr = "1010000154",
            vat = "300001234500003",
            building = "5678",
            street = "Olaya Street",
            district = "Al-Olaya Dist.",
            city = "Riyadh",
            postal = "12211"
        )
    )

    val initialVendors = listOf(
        VendorEntity(
            id = 1,
            name = "Al-Jazirah Transport Fleet",
            contact = "Mr. Salem",
            truck = "4-Ton Reefer Chilled",
            plate = "KSA 5678 LYZ",
            cost = 1200.0,
            trips = 3,
            istimaraExpiry = "2026-11-15",
            opCardExpiry = "2026-12-01",
            licenceExpiry = "2027-05-10",
            insuranceExpiry = "2026-10-20",
            phone = "+966 50 999 8877",
            email = "salem@aljazirahtransport.sa",
            crNumber = "4030112233",
            vatNumber = "310009988700003",
            bankName = "Al-Rajhi Bank",
            iban = "SA44 8000 0000 1234 5678 9001",
            city = "Jeddah",
            supportedTruckTypes = "4-Ton Reefer Chilled, 4-Ton Reefer Freezer"
        ),
        VendorEntity(
            id = 2,
            name = "Desert Line Logistics",
            contact = "Mr. Nasser",
            truck = "4-Ton Open Top",
            plate = "KSA 1234 BCD",
            cost = 900.0,
            trips = 2,
            istimaraExpiry = "2026-09-01",
            opCardExpiry = "2026-08-15",
            licenceExpiry = "2026-12-31",
            insuranceExpiry = "2026-11-10",
            phone = "+966 55 444 3322",
            email = "nasser@desertlinelogistics.sa",
            crNumber = "1010334455",
            vatNumber = "300005544300003",
            bankName = "Saudi National Bank (SNB)",
            iban = "SA88 1000 0000 9876 5432 1002",
            city = "Riyadh",
            supportedTruckTypes = "4-Ton Open Top, 4-Ton Closed Box, 40ft Flatbed"
        ),
        VendorEntity(
            id = 3,
            name = "Red Sea Haulage & 3PL",
            contact = "Mr. Faisal",
            truck = "40ft Flatbed",
            plate = "KSA 9988 XYZ",
            cost = 2500.0,
            trips = 1,
            istimaraExpiry = "2027-01-20",
            opCardExpiry = "2026-10-05",
            licenceExpiry = "2027-03-15",
            insuranceExpiry = "2026-09-30",
            phone = "+966 54 333 2211",
            email = "faisal@redseahaulage.sa",
            crNumber = "2050998877",
            vatNumber = "300008877600003",
            bankName = "Alinma Bank",
            iban = "SA55 0500 0000 1122 3344 5503",
            city = "Dammam",
            supportedTruckTypes = "40ft Flatbed, 40ft Curtain Side, Lowbed"
        )
    )

    fun getEstimatedTransitTime(originHub: String, destination: String, distance: Int): String {
        return when {
            originHub.equals(destination, ignoreCase = true) || distance <= 40 -> "2 - 4 Hours (Intracity)"
            distance <= 100 -> "2 - 3.5 Hours"
            distance <= 360 -> "4 - 6 Hours"
            distance <= 750 -> "7 - 10 Hours"
            distance <= 1100 -> "11 - 14 Hours"
            else -> "15 - 20 Hours"
        }
    }
}
