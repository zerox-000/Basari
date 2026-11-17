package com.example.data.firebase

import android.util.Log
import com.example.data.model.AiPriorityTask
import com.example.data.model.AiWeakArea
import com.example.data.model.MockExam
import com.example.data.model.QuestionLog
import com.example.data.model.UserProfile
import com.example.data.model.WrittenGrade
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

import com.example.data.model.MebSubject
import com.example.data.model.StudySessionItem

data class GeneratedAiFeedback(
    val summary: String,
    val weakAreas: List<AiWeakArea>,
    val priorityTasks: List<AiPriorityTask>,
    val studyTips: List<String>,
    val resources: List<String>
)

data class GeneratedStudyPlan(
    val planTitle: String,
    val coachNote: String,
    val sessions: List<StudySessionItem>
)

class AiFeedbackRepository {
    private val feedbackSchema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.string(
                description = "Öğrencinin genel durumunu, yazılı notlarını, soru çözüm performansını ve deneme netlerini değerlendiren motive edici ve net Türkçe özet."
            ),
            "weakAreas" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "subject" to Schema.string(description = "Ders adı"),
                        "topic" to Schema.string(description = "Eksik konu veya ünite adı"),
                        "severity" to Schema.enumeration(listOf("Yüksek", "Orta", "Düşük")),
                        "reason" to Schema.string(description = "Tespit edilen eksiklik nedeni (hata oranı, düşük not veya az soru)")
                    )
                ),
                description = "Tespit edilen eksik ders ve konular listesi"
            ),
            "priorityTasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Yapılması gereken somut çalışma görevi"),
                        "priority" to Schema.enumeration(listOf("Yüksek", "Orta", "Düşük")),
                        "durationMinutes" to Schema.integer(description = "Önerilen çalışma süresi (dakika)")
                    )
                ),
                description = "Öncelik sırasına göre çalışma listesi"
            ),
            "studyTips" to Schema.array(
                items = Schema.string(),
                description = "Öğrencinin öğrenme stili ve tercih ettiği çalışma saatlerine uygun kişiselleştirilmiş taktikler"
            ),
            "resources" to Schema.array(
                items = Schema.string(),
                description = "MEB OGM Materyal, EBA, Kazanım Testleri ve konu/soru bankası seviyesi önerileri"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = feedbackSchema
            }
        )
    }

    private val studyPlanSchema = Schema.obj(
        properties = mapOf(
            "planTitle" to Schema.string(description = "Haftalık çalışma programı başlığı"),
            "coachNote" to Schema.string(description = "Programın neden bu şekilde oluşturulduğunu anlatan koç notu"),
            "sessions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "dayOfWeek" to Schema.enumeration(
                            listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")
                        ),
                        "timeSlot" to Schema.string(description = "Saat aralığı, örn: 18:30 - 19:30"),
                        "subjectName" to Schema.string(description = "MEB ders adı"),
                        "topicName" to Schema.string(description = "Çalışılacak spesifik konu veya ünite"),
                        "activityType" to Schema.enumeration(
                            listOf("Konu Tekrarı", "Soru Çözümü", "Deneme Analizi", "Yazılı Hazırlık")
                        ),
                        "targetQuestions" to Schema.integer(description = "Hedef soru sayısı (konu tekrarında 0-15, soru çözümünde 20-80)"),
                        "durationMinutes" to Schema.integer(description = "Oturum süresi dakika olarak (30-120)")
                    )
                ),
                description = "Pazartesi'den Pazar'a toplam 14-21 arası haftalık çalışma oturumu"
            )
        )
    )

    private val studyPlanModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = studyPlanSchema
            }
        )
    }

    suspend fun analyzeStudentPerformance(
        profile: UserProfile,
        writtenGrades: List<WrittenGrade>,
        questionLogs: List<QuestionLog>,
        mockExams: List<MockExam>
    ): Result<GeneratedAiFeedback> {
        return try {
            val prompt = buildAnalysisPrompt(profile, writtenGrades, questionLogs, mockExams)
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Yapay zeka boş yanıt döndürdü."))
            }

            val json = JSONObject(rawJson)
            val summary = json.optString("summary", "Performans analizi oluşturuldu.")

            val weakArr = json.optJSONArray("weakAreas")
            val weakAreas = buildList {
                if (weakArr != null) {
                    for (i in 0 until weakArr.length()) {
                        val obj = weakArr.optJSONObject(i) ?: continue
                        add(
                            AiWeakArea(
                                subject = obj.optString("subject", "Genel"),
                                topic = obj.optString("topic", "Tekrar"),
                                severity = obj.optString("severity", "Orta"),
                                reason = obj.optString("reason", "")
                            )
                        )
                    }
                }
            }

            val tasksArr = json.optJSONArray("priorityTasks")
            val priorityTasks = buildList {
                if (tasksArr != null) {
                    for (i in 0 until tasksArr.length()) {
                        val obj = tasksArr.optJSONObject(i) ?: continue
                        add(
                            AiPriorityTask(
                                title = obj.optString("title", "Konu tekrarı"),
                                priority = obj.optString("priority", "Orta"),
                                durationMinutes = obj.optInt("durationMinutes", 45)
                            )
                        )
                    }
                }
            }

            val tipsArr = json.optJSONArray("studyTips")
            val studyTips = buildList {
                if (tipsArr != null) {
                    for (i in 0 until tipsArr.length()) {
                        val s = tipsArr.optString(i)
                        if (s.isNotBlank()) add(s)
                    }
                }
            }

            val resArr = json.optJSONArray("resources")
            val resources = buildList {
                if (resArr != null) {
                    for (i in 0 until resArr.length()) {
                        val s = resArr.optString(i)
                        if (s.isNotBlank()) add(s)
                    }
                }
            }

            Result.success(
                GeneratedAiFeedback(
                    summary = summary,
                    weakAreas = weakAreas,
                    priorityTasks = priorityTasks,
                    studyTips = studyTips,
                    resources = resources
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI analysis timed out", e)
            Result.failure(Exception("Yapay zeka analizi zaman aşımına uğradı. Lütfen tekrar deneyin."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI analysis failed", e)
            Result.failure(e)
        }
    }

    private fun buildAnalysisPrompt(
        profile: UserProfile,
        writtenGrades: List<WrittenGrade>,
        questionLogs: List<QuestionLog>,
        mockExams: List<MockExam>
    ): String {
        val gradesText = if (writtenGrades.isEmpty()) {
            "Henüz yazılı notu girilmemiş."
        } else {
            writtenGrades.take(30).joinToString("\n") {
                "- ${it.subjectName} (${it.term} ${it.examType}): ${it.score}/100"
            }
        }

        val logsByTopic = questionLogs
            .groupBy { "${it.subjectName} | ${it.topicName} | ${it.difficulty}" }
            .entries
            .take(35)
            .joinToString("\n") { (key, list) ->
                val tot = list.sumOf { it.totalCount }
                val cor = list.sumOf { it.correctCount }
                val wrg = list.sumOf { it.wrongCount }
                val acc = if (tot > 0) ((cor * 100.0) / tot).toInt() else 0
                "- $key -> Toplam: $tot, Doğru: $cor, Yanlış: $wrg (Başarı: %$acc)"
            }
            .ifEmpty { "Henüz soru çözümü kaydedilmemiş." }

        val mocksText = if (mockExams.isEmpty()) {
            "Henüz deneme sınavı girilmemiş."
        } else {
            mockExams.take(10).joinToString("\n") { exam ->
                val subs = exam.parsedSubjects().joinToString(", ") {
                    val weak = if (it.weakTopics.isNotEmpty()) " [Eksik: ${it.weakTopics.joinToString("/")}]" else ""
                    "${it.subjectName}: ${"%.2f".format(it.net)} net$weak"
                }
                "- ${exam.examTitle} (${exam.examType}) Toplam Net: ${"%.2f".format(exam.totalNet)}, Tahmini Puan: ${"%.1f".format(exam.estimatedScore)} | $subs"
            }
        }

        return """
            Sen Türkiye MEB müfredatına, LGS ve YKS (TYT/AYT/YDT) sınav sistemine hakim uzman bir eğitim koçusun.
            Aşağıdaki öğrenci profilini ve tüm akademik verilerini analiz ederek Türkçe, somut ve uygulanabilir geri bildirim üret.

            ÖĞRENCİ PROFİLİ:
            - Ad Soyad: ${profile.firstName} ${profile.lastName}
            - Sınıf Seviyesi: ${profile.gradeLevel}
            - Bölüm/Alan: ${profile.branch}
            - Hedef Sınav: ${profile.targetExam} (Hedef Puan: ${profile.targetScore}, Günlük Soru Hedefi: ${profile.dailyQuestionGoal})
            - Öğrenme Stili: ${profile.learningStyle}
            - Tercih Edilen Çalışma Saatleri: ${profile.preferredStudyHours} (Günlük ${profile.dailyStudyMinutes} dk)

            YAZILI NOTLARI:
            $gradesText

            KONU VE ZORLUK BAZLI ÇÖZÜLEN SORULAR:
            $logsByTopic

            DENEME SINAVLARI:
            $mocksText

            GÖREVİN:
            1. Öğrencinin en zayıf olduğu dersleri ve konuları net gerekçelerle tespit et (veri azsa sınıf ve bölümüne göre temel kritik konuları da belirt).
            2. Önceliklendirme listesi oluştur (Yüksek, Orta, Düşük öncelik ve dakika önerisi ile).
            3. Öğrencinin öğrenme stiline (${profile.learningStyle}) ve çalışma saatine (${profile.preferredStudyHours}) özel çalışma önerileri ver.
            4. MEB OGM Materyal, EBA, Kazanım Kavrama Testleri ve zorluk seviyesine uygun ek kaynak tavsiyeleri sun.
        """.trimIndent()
    }

    suspend fun generateWeeklyStudyPlan(
        profile: UserProfile,
        subjects: List<MebSubject>,
        writtenGrades: List<WrittenGrade>,
        questionLogs: List<QuestionLog>,
        mockExams: List<MockExam>,
        focusNote: String
    ): Result<GeneratedStudyPlan> {
        return try {
            val subjectText = subjects.joinToString("\n") { s ->
                "- ${s.name} (${s.weeklyHours} saat/hafta): ${s.topics.take(5).joinToString(", ")}"
            }
            val weakSignals = buildAnalysisPrompt(profile, writtenGrades, questionLogs, mockExams)
            val extraFocus = if (focusNote.isNotBlank()) "\nÖĞRENCİNİN ÖZEL İSTEĞİ:\n$focusNote" else ""

            val prompt = """
                Aşağıdaki öğrenci profili, MEB müfredat dersleri ve performans verilerine göre Pazartesi'den Pazar'a dengeli ve uygulanabilir bir HAFTALIK DERS ÇALIŞMA PROGRAMI oluştur.

                KURALLAR:
                - Her gün için öğrencinin tercih ettiği çalışma saatine (${profile.preferredStudyHours}) uygun saat aralıkları (timeSlot) belirle.
                - Günlük toplam çalışma süresi ortalama ${profile.dailyStudyMinutes} dakika ve günlük hedef ${profile.dailyQuestionGoal} soru civarında olsun.
                - Hafta sonuna (Cumartesi veya Pazar) en az 1 Deneme Analizi / Genel Tekrar oturumu yerleştir.
                - Öğrenme stili (${profile.learningStyle}) ve zayıf ders/konuları gözeterek 14 ile 21 arasında oturum planla (her gün 2 veya 3 oturum).
                - Sadece listedeki MEB derslerini ve gerçek ünitelerini kullan.

                MEB DERS LİSTESİ:
                $subjectText
                $extraFocus

                $weakSignals
            """.trimIndent()

            val response = withTimeout(60_000L) {
                studyPlanModel.generateContent(prompt)
            }
            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Yapay zeka boş çalışma programı döndürdü."))
            }

            val json = JSONObject(rawJson)
            val title = json.optString("planTitle", "${profile.gradeLevel} ${profile.branch} Programı").take(120)
            val coachNote = json.optString("coachNote", "Profiline ve hedeflerine göre dengeli haftalık plan hazırlandı.").take(1200)
            val arr = json.optJSONArray("sessions")
            val sessions = buildList {
                if (arr != null) {
                    for (i in 0 until arr.length().coerceAtMost(35)) {
                        val obj = arr.optJSONObject(i) ?: continue
                        add(
                            StudySessionItem(
                                sessionId = "s_${i + 1}",
                                dayOfWeek = obj.optString("dayOfWeek", "Pazartesi"),
                                timeSlot = obj.optString("timeSlot", "18:30 - 19:30"),
                                subjectName = obj.optString("subjectName", subjects.firstOrNull()?.name ?: "Matematik"),
                                topicName = obj.optString("topicName", "Konu Tekrarı"),
                                activityType = obj.optString("activityType", "Soru Çözümü"),
                                targetQuestions = obj.optInt("targetQuestions", 30).coerceIn(0, 300),
                                durationMinutes = obj.optInt("durationMinutes", 50).coerceIn(15, 240),
                                completed = false
                            )
                        )
                    }
                }
            }
            if (sessions.isEmpty()) {
                return Result.failure(IllegalStateException("Program oturumları oluşturulamadı."))
            }
            Result.success(GeneratedStudyPlan(planTitle = title, coachNote = coachNote, sessions = sessions))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Study plan generation timed out", e)
            Result.failure(Exception("Program oluşturma zaman aşımına uğradı. Lütfen tekrar deneyin."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Study plan generation failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "AiFeedbackRepository"
    }
}
