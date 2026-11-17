package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.AiPriorityTask
import com.example.data.model.AiReport
import com.example.data.model.AiWeakArea
import com.example.data.model.CustomCountdown
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MockExam
import com.example.data.model.MockSubjectResult
import com.example.data.model.QuestionLog
import com.example.data.model.StudyPlan
import com.example.data.model.StudySessionItem
import com.example.data.model.UserProfile
import com.example.data.model.WrittenGrade
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AcademicRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Devam etmek için Google ile oturum açmalısınız.")

    private fun verifyUser(userId: String) {
        val uid = requireUserId()
        check(uid == userId) { "Oturum sahibi ile işlem kimliği uyuşmuyor." }
    }

    fun observeProfile(userId: String): Flow<UserProfile?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

    fun observeWrittenGrades(userId: String): Flow<List<WrittenGrade>> =
        db.collection("users").document(userId).collection("writtenGrades")
            .orderBy("recordedAtMillis", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(WrittenGrade::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeQuestionLogs(userId: String): Flow<List<QuestionLog>> =
        db.collection("users").document(userId).collection("questionLogs")
            .orderBy("recordedAtMillis", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(QuestionLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeMockExams(userId: String): Flow<List<MockExam>> =
        db.collection("users").document(userId).collection("mockExams")
            .orderBy("recordedAtMillis", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(MockExam::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeCustomCountdowns(userId: String): Flow<List<CustomCountdown>> =
        db.collection("users").document(userId).collection("customCountdowns")
            .orderBy("targetDateMillis", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(CustomCountdown::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeAiReports(userId: String): Flow<List<AiReport>> =
        db.collection("users").document(userId).collection("aiReports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(AiReport::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeStudyPlans(userId: String): Flow<List<StudyPlan>> =
        db.collection("users").document(userId).collection("studyPlans")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(StudyPlan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> =
        db.collection("leaderboard")
            .orderBy("rankingScore", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { it.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }

    suspend fun saveUserProfile(userId: String, profile: UserProfile): Result<Unit> {
        val path = "users/$userId"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "firstName" to profile.firstName.trim().take(50).ifEmpty { "Öğrenci" },
                        "lastName" to profile.lastName.trim().take(50).ifEmpty { "Aday" },
                        "gradeLevel" to profile.gradeLevel.take(30),
                        "branch" to profile.branch.take(40),
                        "targetExam" to profile.targetExam.take(30),
                        "targetScore" to profile.targetScore.coerceIn(0.0, 600.0),
                        "dailyQuestionGoal" to profile.dailyQuestionGoal.coerceIn(1L, 1000L),
                        "learningStyle" to profile.learningStyle.take(40),
                        "preferredStudyHours" to profile.preferredStudyHours.take(50),
                        "dailyStudyMinutes" to profile.dailyStudyMinutes.coerceIn(15L, 720L),
                        "themePalette" to profile.themePalette,
                        "darkModePref" to profile.darkModePref,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(profile.toCreateMap(userId)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateThemePreferences(
        userId: String,
        paletteKey: String,
        darkModeKey: String
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "themePalette" to paletteKey,
                        "darkModePref" to darkModeKey,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun addWrittenGrade(userId: String, grade: WrittenGrade): Result<Unit> {
        val colPath = "users/$userId/writtenGrades"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId).collection("writtenGrades").document()
            docRef.set(grade.toCreateMap(userId)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun deleteWrittenGrade(userId: String, gradeId: String): Result<Unit> {
        val path = "users/$userId/writtenGrades/$gradeId"
        return try {
            verifyUser(userId)
            require(gradeId.isNotBlank())
            db.collection("users").document(userId).collection("writtenGrades").document(gradeId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addQuestionLog(userId: String, log: QuestionLog): Result<Unit> {
        val colPath = "users/$userId/questionLogs"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId).collection("questionLogs").document()
            docRef.set(log.toCreateMap(userId)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun deleteQuestionLog(userId: String, logId: String): Result<Unit> {
        val path = "users/$userId/questionLogs/$logId"
        return try {
            verifyUser(userId)
            require(logId.isNotBlank())
            db.collection("users").document(userId).collection("questionLogs").document(logId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addMockExam(
        userId: String,
        examTitle: String,
        examType: String,
        subjectResults: List<MockSubjectResult>,
        recordedAtMillis: Long
    ): Result<Unit> {
        val colPath = "users/$userId/mockExams"
        return try {
            verifyUser(userId)
            val totalCorrect = subjectResults.sumOf { it.correct }
            val totalWrong = subjectResults.sumOf { it.wrong }
            val totalBlank = subjectResults.sumOf { it.blank }
            val totalNet = subjectResults.sumOf { it.net }
            val estimatedScore = (100.0 + (totalNet.coerceAtLeast(0.0) * 3.33)).coerceIn(0.0, 500.0)

            val exam = MockExam(
                userId = userId,
                examTitle = examTitle,
                examType = examType,
                totalCorrect = totalCorrect,
                totalWrong = totalWrong,
                totalBlank = totalBlank,
                totalNet = totalNet,
                estimatedScore = estimatedScore,
                recordedAtMillis = recordedAtMillis
            )
            val docRef = db.collection("users").document(userId).collection("mockExams").document()
            docRef.set(exam.toCreateMap(userId, subjectResults)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMockExam(userId: String, examId: String): Result<Unit> {
        val path = "users/$userId/mockExams/$examId"
        return try {
            verifyUser(userId)
            require(examId.isNotBlank())
            db.collection("users").document(userId).collection("mockExams").document(examId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun addCustomCountdown(userId: String, countdown: CustomCountdown): Result<Unit> {
        val colPath = "users/$userId/customCountdowns"
        return try {
            verifyUser(userId)
            require(countdown.title.trim().isNotEmpty()) { "Sayaç başlığı boş olamaz." }
            val docRef = db.collection("users").document(userId).collection("customCountdowns").document()
            docRef.set(countdown.toCreateMap(userId)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomCountdown(userId: String, countdownId: String): Result<Unit> {
        val path = "users/$userId/customCountdowns/$countdownId"
        return try {
            verifyUser(userId)
            require(countdownId.isNotBlank())
            db.collection("users").document(userId).collection("customCountdowns").document(countdownId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveAiReport(
        userId: String,
        summary: String,
        weakAreas: List<AiWeakArea>,
        priorityTasks: List<AiPriorityTask>,
        studyTips: List<String>,
        resources: List<String>
    ): Result<Unit> {
        val colPath = "users/$userId/aiReports"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId).collection("aiReports").document()
            val payload = mapOf(
                "userId" to userId,
                "summary" to summary.take(2000).ifEmpty { "Analiz tamamlandı." },
                "weakAreas" to weakAreas.take(15).map {
                    mapOf(
                        "subject" to it.subject,
                        "topic" to it.topic,
                        "severity" to it.severity,
                        "reason" to it.reason
                    )
                },
                "priorityTasks" to priorityTasks.take(15).map {
                    mapOf(
                        "title" to it.title,
                        "priority" to it.priority,
                        "durationMinutes" to it.durationMinutes
                    )
                },
                "studyTips" to studyTips.take(15),
                "resources" to resources.take(15),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun saveStudyPlan(
        userId: String,
        planTitle: String,
        coachNote: String,
        sessions: List<StudySessionItem>
    ): Result<Unit> {
        val colPath = "users/$userId/studyPlans"
        return try {
            verifyUser(userId)
            val docRef = db.collection("users").document(userId).collection("studyPlans").document()
            val payload = mapOf(
                "userId" to userId,
                "planTitle" to planTitle.trim().take(120).ifEmpty { "Haftalık Çalışma Programı" },
                "coachNote" to coachNote.trim().take(1200).ifEmpty { "Kişiselleştirilmiş haftalık plan." },
                "sessions" to sessions.take(35).map { it.toMap() },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, colPath)
            Result.failure(e)
        }
    }

    suspend fun updateStudyPlanSessions(
        userId: String,
        planId: String,
        sessions: List<StudySessionItem>
    ): Result<Unit> {
        val path = "users/$userId/studyPlans/$planId"
        return try {
            verifyUser(userId)
            require(planId.isNotBlank()) { "Plan ID boş olamaz." }
            db.collection("users").document(userId).collection("studyPlans").document(planId)
                .update(
                    mapOf(
                        "sessions" to sessions.take(35).map { it.toMap() },
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudyPlan(userId: String, planId: String): Result<Unit> {
        val path = "users/$userId/studyPlans/$planId"
        return try {
            verifyUser(userId)
            require(planId.isNotBlank())
            db.collection("users").document(userId).collection("studyPlans").document(planId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun syncLeaderboardEntry(
        userId: String,
        profile: UserProfile,
        totalSolved: Long,
        totalCorrect: Long,
        avgWritten: Double,
        latestNet: Double
    ): Result<Unit> {
        val path = "leaderboard/$userId"
        return try {
            verifyUser(userId)
            val rankingScore = (totalCorrect * 2.0) + (avgWritten * 5.0) + (latestNet.coerceAtLeast(0.0) * 10.0)
            val docRef = db.collection("leaderboard").document(userId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to profile.displayShortName().take(60),
                        "gradeLevel" to profile.gradeLevel.take(30),
                        "branch" to profile.branch.take(40),
                        "targetExam" to profile.targetExam.take(30),
                        "totalSolvedQuestions" to totalSolved.coerceIn(0L, 1_000_000L),
                        "totalCorrectQuestions" to totalCorrect.coerceIn(0L, 1_000_000L),
                        "averageWrittenGrade" to avgWritten.coerceIn(0.0, 100.0),
                        "latestMockNet" to latestNet.coerceIn(-100.0, 300.0),
                        "rankingScore" to rankingScore.coerceIn(0.0, 1_000_000.0),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "displayName" to profile.displayShortName().take(60),
                        "gradeLevel" to profile.gradeLevel.take(30),
                        "branch" to profile.branch.take(40),
                        "targetExam" to profile.targetExam.take(30),
                        "totalSolvedQuestions" to totalSolved.coerceIn(0L, 1_000_000L),
                        "totalCorrectQuestions" to totalCorrect.coerceIn(0L, 1_000_000L),
                        "averageWrittenGrade" to avgWritten.coerceIn(0.0, 100.0),
                        "latestMockNet" to latestNet.coerceIn(-100.0, 300.0),
                        "rankingScore" to rankingScore.coerceIn(0.0, 1_000_000.0),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
