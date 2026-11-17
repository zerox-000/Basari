package com.example.data.model

data class MebSubject(
    val id: String,
    val name: String,
    val shortName: String,
    val weeklyHours: Int,
    val topics: List<String>,
    val categoryColorIndex: Int
)

data class QuestionBankItem(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val topicName: String,
    val difficulty: String, // Kolay, Orta, Zor
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

object CurriculumCatalog {
    val gradeLevels = listOf(
        "5. Sınıf", "6. Sınıf", "7. Sınıf", "8. Sınıf",
        "9. Sınıf", "10. Sınıf", "11. Sınıf", "12. Sınıf", "Mezun (YKS)"
    )

    val learningStyles = listOf("Görsel", "İşitsel", "Okuma/Yazma", "Kinestetik")
    val studyHourWindows = listOf(
        "Sabah (06:00-12:00)",
        "Öğleden Sonra (12:00-18:00)",
        "Akşam (18:00-22:00)",
        "Gece (22:00-02:00)"
    )

    val difficulties = listOf("Kolay", "Orta", "Zor")

    fun branchesForGrade(gradeLevel: String): List<String> = when (gradeLevel) {
        "5. Sınıf", "6. Sınıf", "7. Sınıf", "8. Sınıf" -> listOf("Genel")
        "9. Sınıf", "10. Sınıf" -> listOf("Genel", "Sayısal", "Eşit Ağırlık", "Sözel")
        else -> listOf("Sayısal", "Eşit Ağırlık", "Sözel", "Yabancı Dil")
    }

    fun defaultTargetExamForGrade(gradeLevel: String): String = when (gradeLevel) {
        "5. Sınıf", "6. Sınıf", "7. Sınıf" -> "Okul Başarısı"
        "8. Sınıf" -> "LGS"
        else -> "YKS"
    }

    fun subjectsFor(gradeLevel: String, branch: String): List<MebSubject> {
        val isMiddleSchool = gradeLevel in listOf("5. Sınıf", "6. Sınıf", "7. Sınıf", "8. Sınıf")
        if (isMiddleSchool) {
            return listOf(
                MebSubject(
                    id = "turkce",
                    name = "Türkçe",
                    shortName = "TR",
                    weeklyHours = 5,
                    topics = listOf("Sözcükte Anlam", "Cümlede Anlam", "Paragrafta Anlam", "Fiilimsiler", "Cümlenin Ögeleri", "Yazım ve Noktalama"),
                    categoryColorIndex = 0
                ),
                MebSubject(
                    id = "matematik",
                    name = "Matematik",
                    shortName = "MAT",
                    weeklyHours = 5,
                    topics = listOf("Çarpanlar ve Katlar", "Üslü İfadeler", "Kareköklü İfadeler", "Veri Analizi", "Olasılık", "Cebirsel İfadeler", "Doğrusal Denklemler"),
                    categoryColorIndex = 1
                ),
                MebSubject(
                    id = "fen",
                    name = "Fen Bilimleri",
                    shortName = "FEN",
                    weeklyHours = 4,
                    topics = listOf("Mevsimler ve İklim", "DNA ve Genetik Kod", "Basınç", "Madde ve Endüstri", "Basit Makineler", "Elektrik Yükleri"),
                    categoryColorIndex = 2
                ),
                MebSubject(
                    id = "sosyal",
                    name = if (gradeLevel == "8. Sınıf") "T.C. İnkılap Tarihi" else "Sosyal Bilgiler",
                    shortName = "SOS",
                    weeklyHours = 3,
                    topics = listOf("Bir Kahraman Doğuyor", "Milli Uyanış", "Ya İstiklal Ya Ölüm", "Atatürkçülük", "Demokratikleşme Çabaları"),
                    categoryColorIndex = 3
                ),
                MebSubject(
                    id = "ingilizce",
                    name = "İngilizce",
                    shortName = "İNG",
                    weeklyHours = 4,
                    topics = listOf("Friendship", "Teen Life", "In The Kitchen", "On The Phone", "The Internet", "Adventures"),
                    categoryColorIndex = 4
                ),
                MebSubject(
                    id = "din",
                    name = "Din Kültürü ve Ahlak Bilgisi",
                    shortName = "DİN",
                    weeklyHours = 2,
                    topics = listOf("Kader İnancı", "Zekat ve Sadaka", "Din ve Hayat", "Hz. Muhammed'in Örnekliği"),
                    categoryColorIndex = 5
                )
            )
        }

        // Lise & YKS Ortak Dersler
        val commonHighSchool = mutableListOf(
            MebSubject(
                id = "edebiyat",
                name = "Türk Dili ve Edebiyatı",
                shortName = "EDB",
                weeklyHours = 5,
                topics = listOf("Paragraf ve Anlam", "Söz Sanatları", "Şiir Bilgisi", "Hikaye ve Roman", "Divan Edebiyatı", "Cumhuriyet Dönemi", "Dil Bilgisi"),
                categoryColorIndex = 0
            ),
            MebSubject(
                id = "matematik",
                name = "Matematik",
                shortName = "MAT",
                weeklyHours = 6,
                topics = listOf("Temel Kavramlar", "Problemler", "Fonksiyonlar", "Polinomlar", "İkinci Dereceden Denklemler", "Trigonometri", "Logaritma", "Limit ve Türev", "İntegral", "Geometri"),
                categoryColorIndex = 1
            ),
            MebSubject(
                id = "ingilizce",
                name = "Yabancı Dil (İngilizce)",
                shortName = "İNG",
                weeklyHours = 4,
                topics = listOf("Tenses", "Modals", "Passive Voice", "Conditionals", "Cloze Test", "Reading Comprehension"),
                categoryColorIndex = 6
            ),
            MebSubject(
                id = "din",
                name = "Din Kültürü ve Ahlak Bilgisi",
                shortName = "DİN",
                weeklyHours = 2,
                topics = listOf("Bilgi ve İnanç", "İslam ve İbadet", "Ahlak ve Değerler", "Kur'an'a Göre Hz. Muhammed"),
                categoryColorIndex = 7
            )
        )

        val scienceSubjects = listOf(
            MebSubject(
                id = "fizik",
                name = "Fizik",
                shortName = "FİZ",
                weeklyHours = 4,
                topics = listOf("Fizik Bilimine Giriş", "Kuvvet ve Hareket", "İş Güç Enerji", "Optik", "Dalgalar", "Elektrik ve Manyetizma", "Modern Fizik"),
                categoryColorIndex = 2
            ),
            MebSubject(
                id = "kimya",
                name = "Kimya",
                shortName = "KİM",
                weeklyHours = 4,
                topics = listOf("Atom ve Periyodik Sistem", "Kimyasal Türler", "Mol Kavramı", "Kimyasal Tepkimeler", "Asitler ve Bazlar", "Gazlar", "Organik Kimya"),
                categoryColorIndex = 3
            ),
            MebSubject(
                id = "biyoloji",
                name = "Biyoloji",
                shortName = "BİY",
                weeklyHours = 4,
                topics = listOf("Hücre ve Organeller", "Canlıların Sınıflandırılması", "Hücre Bölünmeleri", "Kalıtım", "Ekosistem Ekolojisi", "İnsan Fizyolojisi", "Genden Proteine"),
                categoryColorIndex = 4
            )
        )

        val socialSubjects = listOf(
            MebSubject(
                id = "tarih",
                name = "Tarih",
                shortName = "TAR",
                weeklyHours = 4,
                topics = listOf("Tarih ve Zaman", "İlk ve Orta Çağlarda Türk Dünyası", "İslam Medeniyeti", "Osmanlı Kuruluş ve Yükselme", "Kurtuluş Savaşı", "Atatürk İlke ve İnkılapları"),
                categoryColorIndex = 3
            ),
            MebSubject(
                id = "cografya",
                name = "Coğrafya",
                shortName = "COĞ",
                weeklyHours = 4,
                topics = listOf("Doğa ve İnsan", "Harita Bilgisi", "İklim Bilgisi", "İç ve Dış Kuvvetler", "Nüfus ve Yerleşme", "Türkiye Coğrafyası", "Küresel Ortam"),
                categoryColorIndex = 5
            ),
            MebSubject(
                id = "felsefe",
                name = "Felsefe",
                shortName = "FEL",
                weeklyHours = 2,
                topics = listOf("Felsefeyi Tanıma", "Bilgi Felsefesi", "Varlık Felsefesi", "Ahlak Felsefesi", "Siyaset ve Sanat Felsefesi"),
                categoryColorIndex = 4
            )
        )

        return when (branch) {
            "Sayısal" -> {
                commonHighSchool + scienceSubjects + listOf(
                    socialSubjects[0].copy(weeklyHours = 2),
                    socialSubjects[1].copy(weeklyHours = 2)
                )
            }
            "Eşit Ağırlık" -> {
                commonHighSchool + socialSubjects + listOf(
                    scienceSubjects[0].copy(weeklyHours = 2)
                )
            }
            "Sözel" -> {
                commonHighSchool + socialSubjects + listOf(
                    MebSubject(
                        id = "sosyoloji_psikoloji",
                        name = "Sosyoloji ve Psikoloji",
                        shortName = "SOS",
                        weeklyHours = 3,
                        topics = listOf("Psikoloji Bilimi", "Öğrenme ve Bellek", "Birey ve Toplum", "Toplumsal Yapı", "Mantık İlkeleri"),
                        categoryColorIndex = 2
                    )
                )
            }
            "Yabancı Dil" -> {
                listOf(
                    MebSubject(
                        id = "ydt_ingilizce",
                        name = "İleri İngilizce (YDT)",
                        shortName = "YDT",
                        weeklyHours = 10,
                        topics = listOf("Grammar & Tense", "Vocabulary", "Cloze Test", "Sentence Completion", "Translation", "Reading Paragraph", "Dialogue & Restatement"),
                        categoryColorIndex = 1
                    )
                ) + commonHighSchool.filter { it.id != "ingilizce" } + socialSubjects.take(2)
            }
            else -> {
                // 9-10 Genel Lise
                commonHighSchool + scienceSubjects + socialSubjects
            }
        }
    }

    val questionBank: List<QuestionBankItem> = listOf(
        // Matematik
        QuestionBankItem(
            id = "qb_mat_1",
            subjectId = "matematik",
            subjectName = "Matematik",
            topicName = "Temel Kavramlar",
            difficulty = "Kolay",
            questionText = "a ve b birer pozitif tam sayı olmak üzere, 3a + 2b = 24 eşitliğini sağlayan kaç farklı (a, b) ikilisi vardır?",
            options = listOf("2", "3", "4", "5", "6"),
            correctOptionIndex = 1,
            explanation = "2b çift ve 24 çift olduğu için 3a da çift olmalıdır. a = 2 -> b = 9, a = 4 -> b = 6, a = 6 -> b = 3. Toplam 3 farklı ikili vardır."
        ),
        QuestionBankItem(
            id = "qb_mat_2",
            subjectId = "matematik",
            subjectName = "Matematik",
            topicName = "Problemler",
            difficulty = "Orta",
            questionText = "Bir kırtasiyedeki kalemlerin %40'ı kırmızı, kalanların %25'i mavidir. Geriye 54 kalem kaldığına göre başlangıçta kaç kalem vardır?",
            options = listOf("100", "120", "135", "150", "180"),
            correctOptionIndex = 1,
            explanation = "100x kalemin 40x'i kırmızı, kalan 60x'in %25'i = 15x mavidir. Kalan 45x = 54 ise x = 1,2 olur. 100x = 120 kalem bulunur."
        ),
        QuestionBankItem(
            id = "qb_mat_3",
            subjectId = "matematik",
            subjectName = "Matematik",
            topicName = "Fonksiyonlar",
            difficulty = "Zor",
            questionText = "Gerçel sayılar kümesinde tanımlı f fonksiyonu her x, y için f(x + y) = f(x) + f(y) + 2xy eşitliğini sağlıyor. f(1) = 4 olduğuna göre f(5) kaçtır?",
            options = listOf("36", "40", "44", "48", "52"),
            correctOptionIndex = 1,
            explanation = "f(2) = 2f(1)+2 = 10, f(3) = f(2)+f(1)+4 = 18, f(4) = f(3)+f(1)+6 = 28, f(5) = f(4)+f(1)+8 = 40. Genel kural f(n) = n² + 3n."
        ),
        QuestionBankItem(
            id = "qb_mat_4",
            subjectId = "matematik",
            subjectName = "Matematik",
            topicName = "Üslü İfadeler",
            difficulty = "Kolay",
            questionText = "2^5 · 4^3 / 8^2 işleminin sonucu kaçtır?",
            options = listOf("8", "16", "32", "64", "128"),
            correctOptionIndex = 2,
            explanation = "4^3 = 2^6 ve 8^2 = 2^6 olduğundan (2^5 · 2^6) / 2^6 = 2^5 = 32 olur."
        ),
        // Fizik
        QuestionBankItem(
            id = "qb_fiz_1",
            subjectId = "fizik",
            subjectName = "Fizik",
            topicName = "Kuvvet ve Hareket",
            difficulty = "Kolay",
            questionText = "Sürtünmesiz yatay düzlemde durmakta olan 4 kg kütleli cisme 20 N büyüklüğünde yatay kuvvet uygulanıyor. Cismin ivmesi kaç m/s² olur?",
            options = listOf("2", "4", "5", "8", "10"),
            correctOptionIndex = 2,
            explanation = "F = m · a bağıntısından 20 = 4 · a => a = 5 m/s² bulunur."
        ),
        QuestionBankItem(
            id = "qb_fiz_2",
            subjectId = "fizik",
            subjectName = "Fizik",
            topicName = "İş Güç Enerji",
            difficulty = "Orta",
            questionText = "Yerden 20 m yükseklikten serbest bırakılan 2 kg kütleli cismin yere çarpma kinetik enerjisi kaç Joule'dür? (g = 10 m/s², hava direnci önemsiz)",
            options = listOf("200", "300", "400", "500", "600"),
            correctOptionIndex = 2,
            explanation = "Mekanik enerji korunur: Ep = m·g·h = 2 · 10 · 20 = 400 J tamamen kinetik enerjiye dönüşür."
        ),
        QuestionBankItem(
            id = "qb_fiz_3",
            subjectId = "fizik",
            subjectName = "Fizik",
            topicName = "Optik",
            difficulty = "Zor",
            questionText = "Odak uzaklığı f olan çukur aynanın asal ekseni üzerine yerleştirilen bir cismin görüntüsü kendisinin 2 katı boyda ve gerçektir. Cisim aynadan kaç f uzaktadır?",
            options = listOf("f/2", "3f/2", "2f", "5f/2", "3f"),
            correctOptionIndex = 1,
            explanation = "Gerçek ve büyük görüntü cisim F ile M (f ile 2f) arasındayken oluşur. Boy oranı 2 olduğu için dc = 1,5f = 3f/2, dg = 3f olur."
        ),
        // Kimya
        QuestionBankItem(
            id = "qb_kim_1",
            subjectId = "kimya",
            subjectName = "Kimya",
            topicName = "Atom ve Periyodik Sistem",
            difficulty = "Kolay",
            questionText = "Atom numarası 17 olan X elementinin periyodik sistemdeki yeri aşağıdakilerden hangisidir?",
            options = listOf("2. Periyot 7A", "3. Periyot 5A", "3. Periyot 7A", "3. Periyot 8A", "4. Periyot 1A"),
            correctOptionIndex = 2,
            explanation = "17X elektron dizilimi 2) 8) 7) şeklindedir. 3 katmanı olduğu için 3. periyot, son katmanında 7 elektron olduğu için 7A grubundadır."
        ),
        QuestionBankItem(
            id = "qb_kim_2",
            subjectId = "kimya",
            subjectName = "Kimya",
            topicName = "Mol Kavramı",
            difficulty = "Orta",
            questionText = "Normal koşullarda 11,2 litre hacim kaplayan CH4 gazı kaç gramdır? (C: 12, H: 1)",
            options = listOf("4", "8", "12", "16", "24"),
            correctOptionIndex = 1,
            explanation = "11,2 L / 22,4 L = 0,5 mol CH4 eder. MA(CH4) = 16 g/mol olduğuna göre 0,5 · 16 = 8 gramdır."
        ),
        // Biyoloji
        QuestionBankItem(
            id = "qb_biy_1",
            subjectId = "biyoloji",
            subjectName = "Biyoloji",
            topicName = "Hücre ve Organeller",
            difficulty = "Kolay",
            questionText = "Ökaryot hücrelerde oksijenli solunum ile ATP sentezinin yoğun olarak gerçekleştiği çift zarlı organel hangisidir?",
            options = listOf("Ribozom", "Lizozom", "Golgi Aygıtı", "Mitokondri", "Endoplazmik Retikulum"),
            correctOptionIndex = 3,
            explanation = "Mitokondri çift zarlıdır, kendi DNA/RNA'sına sahiptir ve oksijenli solunumla ATP üretir."
        ),
        QuestionBankItem(
            id = "qb_biy_2",
            subjectId = "biyoloji",
            subjectName = "Biyoloji",
            topicName = "Kalıtım",
            difficulty = "Orta",
            questionText = "AaBb genotipli iki bireyin çaprazlanması sonucu A_bb fenotipli bireylerin oluşma olasılığı kaçtır? (Genler bağımsızdır)",
            options = listOf("1/16", "3/16", "9/16", "1/4", "3/8"),
            correctOptionIndex = 1,
            explanation = "Aa x Aa -> A_ olasılığı 3/4; Bb x Bb -> bb olasılığı 1/4. Çarpımları (3/4)·(1/4) = 3/16 olur."
        ),
        // Edebiyat / Türkçe
        QuestionBankItem(
            id = "qb_edb_1",
            subjectId = "edebiyat",
            subjectName = "Türk Dili ve Edebiyatı",
            topicName = "Söz Sanatları",
            difficulty = "Kolay",
            questionText = "'Güneş gülümseyerek uyandırdı şehri bu sabah.' cümlesinde hangi söz sanatı ağır basmaktadır?",
            options = listOf("Teşhis (Kişileştirme)", "Mübalağa", "Tevriye", "Kinaye", "İstifham"),
            correctOptionIndex = 0,
            explanation = "İnsana özgü 'gülümseme' özelliği güneşe verildiği için kişileştirme (teşhis) sanatı vardır."
        ),
        QuestionBankItem(
            id = "qb_tur_1",
            subjectId = "turkce",
            subjectName = "Türkçe",
            topicName = "Paragrafta Anlam",
            difficulty = "Orta",
            questionText = "Bir yazarın okura sadece bilgi vermeyi değil, onda estetik bir heyecan uyandırmayı amaçlaması eserini kalıcı kılar. Bu cümlede kalıcılığın temel koşulu aşağıdakilerden hangisidir?",
            options = listOf("Yalın dil kullanımı", "Güncel konuları işlemek", "Bilgiyi sanatla harmanlamak", "Uzun cümleler kurmak", "Öğreticilikten kaçınmak"),
            correctOptionIndex = 2,
            explanation = "Hem bilgi verme hem de estetik heyecan uyandırma, bilgiyi sanat/estetik ile birleştirmeyi ifade eder."
        ),
        QuestionBankItem(
            id = "qb_tar_1",
            subjectId = "tarih",
            subjectName = "Tarih",
            topicName = "Kurtuluş Savaşı",
            difficulty = "Orta",
            questionText = "Milli Mücadele'de 'Vatan bir bütündür, bölünemez.' kararı ilk kez nerede alınmıştır?",
            options = listOf("Amasya Genelgesi", "Erzurum Kongresi", "Sivas Kongresi", "Amasya Görüşmeleri", "Misak-ı Milli"),
            correctOptionIndex = 1,
            explanation = "İlk kez Erzurum Kongresi'nde 'Milli sınırlar içinde vatan bir bütündür, parçalanamaz' ilkesi kabul edilmiştir."
        ),
        QuestionBankItem(
            id = "qb_cog_1",
            subjectId = "cografya",
            subjectName = "Coğrafya",
            topicName = "İklim Bilgisi",
            difficulty = "Kolay",
            questionText = "Türkiye'de en fazla yağışı sonbahar mevsiminde alan iklim tipi hangisidir?",
            options = listOf("Akdeniz İklimi", "Karasal İklim", "Karadeniz İklimi", "Sert Karasal İklim", "Marmara Geçiş İklimi"),
            correctOptionIndex = 2,
            explanation = "Karadeniz iklimi her mevsim yağışlıdır ancak maksimum yağışı sonbahar aylarında alır."
        ),
        QuestionBankItem(
            id = "qb_fen_1",
            subjectId = "fen",
            subjectName = "Fen Bilimleri",
            topicName = "DNA ve Genetik Kod",
            difficulty = "Kolay",
            questionText = "Bir DNA molekülünde Adenin nükleotidinin karşısına daima hangi organik baz gelir?",
            options = listOf("Guanin", "Sitozin", "Timin", "Urasil", "Fosfat"),
            correctOptionIndex = 2,
            explanation = "DNA'da Adenin (A) ile Timin (T), Guanin (G) ile Sitozin (S) eşleşir."
        ),
        QuestionBankItem(
            id = "qb_ing_1",
            subjectId = "ingilizce",
            subjectName = "İngilizce",
            topicName = "Conditionals",
            difficulty = "Orta",
            questionText = "If she ____ earlier, she wouldn't have missed the morning train.",
            options = listOf("wakes up", "woke up", "had woken up", "would wake up", "has woken up"),
            correctOptionIndex = 2,
            explanation = "Past unreal condition (Type 3): If + Past Perfect (had woken up), would have + V3."
        )
    )
}
