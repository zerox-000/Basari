package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gradeLevel: String = "12. Sınıf",
    val branch: String = "Sayısal",
    val targetExam: String = "YKS",
    val targetScore: Double = 450.0,
    val dailyQuestionGoal: Long = 100L,
    val learningStyle: String = "Görsel",
    val preferredStudyHours: String = "Akşam (18:00-22:00)",
    val dailyStudyMinutes: Long = 180L,
    val themePalette: String = "OCEAN",
    val darkModePref: String = "SYSTEM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun displayShortName(): String {
        val f = firstName.trim()
        val l = lastName.trim()
        return when {
            f.isNotEmpty() && l.isNotEmpty() -> "$f ${l.first().uppercaseChar()}."
            f.isNotEmpty() -> f
            else -> "Öğrenci"
        }
    }

    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "firstName" to firstName.trim().take(50).ifEmpty { "Öğrenci" },
        "lastName" to lastName.trim().take(50).ifEmpty { "Aday" },
        "gradeLevel" to gradeLevel.take(30),
        "branch" to branch.take(40),
        "targetExam" to targetExam.take(30),
        "targetScore" to targetScore.coerceIn(0.0, 600.0),
        "dailyQuestionGoal" to dailyQuestionGoal.coerceIn(1L, 1000L),
        "learningStyle" to learningStyle.take(40),
        "preferredStudyHours" to preferredStudyHours.take(50),
        "dailyStudyMinutes" to dailyStudyMinutes.coerceIn(15L, 720L),
        "themePalette" to themePalette,
        "darkModePref" to darkModePref,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class WrittenGrade(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val term: String = "1. Dönem",
    val examType: String = "1. Yazılı",
    val score: Double = 0.0,
    val note: String? = null,
    val recordedAtMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> {
        val base = mutableMapOf<String, Any>(
            "userId" to uid,
            "subjectId" to subjectId.take(64),
            "subjectName" to subjectName.take(80),
            "term" to term.take(30),
            "examType" to examType.take(30),
            "score" to score.coerceIn(0.0, 100.0),
            "recordedAtMillis" to recordedAtMillis.coerceAtLeast(0L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val cleanNote = note?.trim()?.take(300)
        if (!cleanNote.isNullOrEmpty()) {
            base["note"] = cleanNote
        }
        return base
    }
}

@Keep
@IgnoreExtraProperties
data class QuestionLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val topicName: String = "",
    val difficulty: String = "Orta",
    val source: String = "Soru Bankası",
    val totalCount: Long = 0L,
    val correctCount: Long = 0L,
    val wrongCount: Long = 0L,
    val blankCount: Long = 0L,
    val recordedAtMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> {
        val c = correctCount.coerceIn(0L, 1000L)
        val w = wrongCount.coerceIn(0L, 1000L)
        val b = blankCount.coerceIn(0L, 1000L)
        val total = (c + w + b).coerceIn(1L, 1000L)
        return mapOf(
            "userId" to uid,
            "subjectId" to subjectId.take(64),
            "subjectName" to subjectName.take(80),
            "topicName" to topicName.take(120).ifEmpty { "Genel" },
            "difficulty" to if (difficulty in listOf("Kolay", "Orta", "Zor")) difficulty else "Orta",
            "source" to source.take(40).ifEmpty { "Soru Bankası" },
            "totalCount" to total,
            "correctCount" to c,
            "wrongCount" to w,
            "blankCount" to b,
            "recordedAtMillis" to recordedAtMillis.coerceAtLeast(0L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@Keep
@IgnoreExtraProperties
data class MockSubjectResult(
    val subjectId: String = "",
    val subjectName: String = "",
    val correct: Long = 0L,
    val wrong: Long = 0L,
    val blank: Long = 0L,
    val net: Double = 0.0,
    val weakTopics: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "correct" to correct,
        "wrong" to wrong,
        "blank" to blank,
        "net" to net,
        "weakTopics" to weakTopics.take(5)
    )
}

@Keep
@IgnoreExtraProperties
data class MockExam(
    @DocumentId val id: String = "",
    val userId: String = "",
    val examTitle: String = "",
    val examType: String = "TYT",
    val totalCorrect: Long = 0L,
    val totalWrong: Long = 0L,
    val totalBlank: Long = 0L,
    val totalNet: Double = 0.0,
    val estimatedScore: Double = 0.0,
    val subjectResults: List<Map<String, Any>> = emptyList(),
    val recordedAtMillis: Long = System.currentTimeMillis(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedSubjects(): List<MockSubjectResult> = subjectResults.map { map ->
        val rawTopics = (map["weakTopics"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        MockSubjectResult(
            subjectId = map["subjectId"] as? String ?: "",
            subjectName = map["subjectName"] as? String ?: "",
            correct = (map["correct"] as? Number)?.toLong() ?: 0L,
            wrong = (map["wrong"] as? Number)?.toLong() ?: 0L,
            blank = (map["blank"] as? Number)?.toLong() ?: 0L,
            net = (map["net"] as? Number)?.toDouble() ?: 0.0,
            weakTopics = rawTopics
        )
    }

    fun toCreateMap(uid: String, parsed: List<MockSubjectResult>): Map<String, Any> = mapOf(
        "userId" to uid,
        "examTitle" to examTitle.trim().take(100).ifEmpty { "$examType Denemesi" },
        "examType" to examType.take(40),
        "totalCorrect" to totalCorrect.coerceIn(0L, 300L),
        "totalWrong" to totalWrong.coerceIn(0L, 300L),
        "totalBlank" to totalBlank.coerceIn(0L, 300L),
        "totalNet" to totalNet.coerceIn(-100.0, 300.0),
        "estimatedScore" to estimatedScore.coerceIn(0.0, 600.0),
        "subjectResults" to parsed.take(20).map { it.toMap() },
        "recordedAtMillis" to recordedAtMillis.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CustomCountdown(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Sınav",
    val targetDateMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(uid: String): Map<String, Any> = mapOf(
        "userId" to uid,
        "title" to title.trim().take(80),
        "category" to category.take(30),
        "targetDateMillis" to targetDateMillis.coerceAtLeast(0L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

data class AiWeakArea(
    val subject: String = "",
    val topic: String = "",
    val severity: String = "Orta",
    val reason: String = ""
)

data class AiPriorityTask(
    val title: String = "",
    val priority: String = "Yüksek",
    val durationMinutes: Int = 45
)

@Keep
@IgnoreExtraProperties
data class AiReport(
    @DocumentId val id: String = "",
    val userId: String = "",
    val summary: String = "",
    val weakAreas: List<Map<String, Any>> = emptyList(),
    val priorityTasks: List<Map<String, Any>> = emptyList(),
    val studyTips: List<String> = emptyList(),
    val resources: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedWeakAreas(): List<AiWeakArea> = weakAreas.map { m ->
        AiWeakArea(
            subject = m["subject"] as? String ?: "",
            topic = m["topic"] as? String ?: "",
            severity = m["severity"] as? String ?: "Orta",
            reason = m["reason"] as? String ?: ""
        )
    }

    fun parsedPriorityTasks(): List<AiPriorityTask> = priorityTasks.map { m ->
        AiPriorityTask(
            title = m["title"] as? String ?: "",
            priority = m["priority"] as? String ?: "Orta",
            durationMinutes = (m["durationMinutes"] as? Number)?.toInt() ?: 45
        )
    }
}

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val gradeLevel: String = "",
    val branch: String = "",
    val targetExam: String = "",
    val totalSolvedQuestions: Long = 0L,
    val totalCorrectQuestions: Long = 0L,
    val averageWrittenGrade: Double = 0.0,
    val latestMockNet: Double = 0.0,
    val rankingScore: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class StudySessionItem(
    val sessionId: String = "",
    val dayOfWeek: String = "Pazartesi",
    val timeSlot: String = "18:00 - 19:00",
    val subjectName: String = "",
    val topicName: String = "",
    val activityType: String = "Soru Çözümü",
    val targetQuestions: Int = 30,
    val durationMinutes: Int = 50,
    val completed: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "sessionId" to sessionId,
        "dayOfWeek" to dayOfWeek,
        "timeSlot" to timeSlot,
        "subjectName" to subjectName,
        "topicName" to topicName,
        "activityType" to activityType,
        "targetQuestions" to targetQuestions,
        "durationMinutes" to durationMinutes,
        "completed" to completed
    )
}

@Keep
@IgnoreExtraProperties
data class StudyPlan(
    @DocumentId val id: String = "",
    val userId: String = "",
    val planTitle: String = "",
    val coachNote: String = "",
    val sessions: List<Map<String, Any>> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedSessions(): List<StudySessionItem> = sessions.mapIndexed { idx, m ->
        StudySessionItem(
            sessionId = m["sessionId"] as? String ?: "s_$idx",
            dayOfWeek = m["dayOfWeek"] as? String ?: "Pazartesi",
            timeSlot = m["timeSlot"] as? String ?: "18:00 - 19:00",
            subjectName = m["subjectName"] as? String ?: "",
            topicName = m["topicName"] as? String ?: "",
            activityType = m["activityType"] as? String ?: "Soru Çözümü",
            targetQuestions = (m["targetQuestions"] as? Number)?.toInt() ?: 0,
            durationMinutes = (m["durationMinutes"] as? Number)?.toInt() ?: 45,
            completed = (m["completed"] as? Boolean) ?: false
        )
    }
}

