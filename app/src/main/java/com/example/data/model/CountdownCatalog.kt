package com.example.data.model

import java.util.Calendar

data class OfficialMilestone(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // "YKS", "LGS", "Yazılı", "Tatil"
    val month: Int, // 1..12
    val dayOfMonth: Int,
    val year: Int
)

object CountdownCatalog {
    // 2026-2027 MEB & ÖSYM Academic Calendar Milestones (starting Nov 2026 -> June 2027)
    private val baseMilestones = listOf(
        OfficialMilestone(
            id = "yazili_1_2",
            title = "1. Dönem 2. Yazılı Haftası",
            subtitle = "Aralık sonu / Ocak başı MEB ortak yazılı dönemi",
            category = "Yazılı",
            year = 2026, month = 12, dayOfMonth = 28
        ),
        OfficialMilestone(
            id = "yariyil_tatili",
            title = "Yarıyıl (Sömestr) Tatili",
            subtitle = "1. Dönem karne günü & 15 tatil başlangıcı",
            category = "Tatil",
            year = 2027, month = 1, dayOfMonth = 18
        ),
        OfficialMilestone(
            id = "yazili_2_1",
            title = "2. Dönem 1. Yazılı Haftası",
            subtitle = "2. dönem ilk sınav dönemi",
            category = "Yazılı",
            year = 2027, month = 3, dayOfMonth = 22
        ),
        OfficialMilestone(
            id = "ara_tatil_2",
            title = "2. Dönem Ara Tatili",
            subtitle = "Bahar ara tatil haftası",
            category = "Tatil",
            year = 2027, month = 4, dayOfMonth = 5
        ),
        OfficialMilestone(
            id = "yazili_2_2",
            title = "2. Dönem 2. Yazılı Haftası",
            subtitle = "Yıl sonu final yazılı dönemi",
            category = "Yazılı",
            year = 2027, month = 5, dayOfMonth = 24
        ),
        OfficialMilestone(
            id = "lgs_sinavi",
            title = "LGS Sınav Günü",
            subtitle = "Liselere Geçiş Sistemi Merkezi Sınavı",
            category = "LGS",
            year = 2027, month = 6, dayOfMonth = 6
        ),
        OfficialMilestone(
            id = "yaz_tatili",
            title = "Yaz Tatili (Okulların Kapanışı)",
            subtitle = "2026-2027 Eğitim Öğretim Yılı sonu",
            category = "Tatil",
            year = 2027, month = 6, dayOfMonth = 18
        ),
        OfficialMilestone(
            id = "yks_tyt",
            title = "YKS 1. Oturum (TYT)",
            subtitle = "Temel Yeterlilik Testi",
            category = "YKS",
            year = 2027, month = 6, dayOfMonth = 19
        ),
        OfficialMilestone(
            id = "yks_ayt",
            title = "YKS 2. Oturum (AYT & YDT)",
            subtitle = "Alan Yeterlilik ve Yabancı Dil Testi",
            category = "YKS",
            year = 2027, month = 6, dayOfMonth = 20
        )
    )

    fun upcomingOfficialMilestones(nowMillis: Long = System.currentTimeMillis()): List<Pair<OfficialMilestone, Long>> {
        return baseMilestones.map { milestone ->
            var targetYear = milestone.year
            var millis = toEpochMillis(targetYear, milestone.month, milestone.dayOfMonth)
            // Roll forward 1 year if already passed
            while (millis < nowMillis) {
                targetYear += 1
                millis = toEpochMillis(targetYear, milestone.month, milestone.dayOfMonth)
            }
            milestone.copy(year = targetYear) to millis
        }.sortedBy { it.second }
    }

    fun toEpochMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
