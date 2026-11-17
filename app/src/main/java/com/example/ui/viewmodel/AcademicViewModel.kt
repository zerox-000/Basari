package com.example.ui.viewmodel

import android.util.Log
import com.example.data.firebase.AcademicRepository
import com.example.data.firebase.AiFeedbackRepository
import com.example.data.model.AiReport
import com.example.data.model.CurriculumCatalog
import com.example.data.model.CustomCountdown
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MebSubject
import com.example.data.model.MockExam
import com.example.data.model.MockSubjectResult
import com.example.data.model.QuestionLog
import com.example.data.model.StudyPlan
import com.example.data.model.StudySessionItem
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.WrittenGrade
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeGranularity(val label: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    YEARLY("Yıllık")
}

data class SubjectSummary(
    val subject: MebSubject,
    val writtenAverage: Double?,
    val writtenCount: Int,
    val totalSolved: Long,
    val totalCorrect: Long,
    val totalWrong: Long,
    val totalBlank: Long,
    val accuracyPercent: Int,
    val latestMockNet: Double?
)

data class ChartBucket(
    val label: String,
    val solvedCount: Long,
    val correctCount: Long,
    val avgNet: Double
)

data class PeriodComparison(
    val currentSolved: Long,
    val previousSolved: Long,
    val currentAccuracy: Int,
    val previousAccuracy: Int,
    val currentAvgNet: Double,
    val previousAvgNet: Double
)

data class AcademicDashboardSnapshot(
    val profile: UserProfile?,
    val subjects: List<MebSubject>,
    val writtenGrades: List<WrittenGrade>,
    val questionLogs: List<QuestionLog>,
    val mockExams: List<MockExam>,
    val customCountdowns: List<CustomCountdown>,
    val aiReports: List<AiReport>,
    val studyPlans: List<StudyPlan>,
    val subjectSummaries: List<SubjectSummary>,
    val totalSolvedToday: Long,
    val totalSolvedAllTime: Long,
    val totalCorrectAllTime: Long,
    val overallWrittenAverage: Double,
    val latestMockNet: Double,
    val overallAccuracyPercent: Int
)

class AcademicViewModel(
    private val repository: AcademicRepository,
    private val aiRepository: AiFeedbackRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedGranularity = MutableStateFlow(TimeGranularity.WEEKLY)
    val selectedGranularity: StateFlow<TimeGranularity> = _selectedGranularity.asStateFlow()

    private val _selectedSubjectIdFilter = MutableStateFlow<String?>(null)
    val selectedSubjectIdFilter: StateFlow<String?> = _selectedSubjectIdFilter.asStateFlow()

    private val _aiGenerationState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val aiGenerationState: StateFlow<UiState<Unit>> = _aiGenerationState.asStateFlow()

    private val _studyPlanGenState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val studyPlanGenState: StateFlow<UiState<Unit>> = _studyPlanGenState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private fun <T> Flow<T>.toUiState(): Flow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Stream observation failed", root)
                emit(UiState.Error(root.localizedMessage ?: "Veri yüklenirken hata oluştu."))
            }

    val dashboardState: StateFlow<UiState<AcademicDashboardSnapshot>> = combine(
        repository.observeProfile(currentUserId),
        repository.observeWrittenGrades(currentUserId),
        repository.observeQuestionLogs(currentUserId),
        repository.observeMockExams(currentUserId),
        repository.observeCustomCountdowns(currentUserId),
        repository.observeAiReports(currentUserId),
        repository.observeStudyPlans(currentUserId)
    ) { arr ->
        val profile = arr[0] as UserProfile?
        @Suppress("UNCHECKED_CAST")
        val grades = arr[1] as List<WrittenGrade>
        @Suppress("UNCHECKED_CAST")
        val logs = arr[2] as List<QuestionLog>
        @Suppress("UNCHECKED_CAST")
        val mocks = arr[3] as List<MockExam>
        @Suppress("UNCHECKED_CAST")
        val countdowns = arr[4] as List<CustomCountdown>
        @Suppress("UNCHECKED_CAST")
        val reports = arr[5] as List<AiReport>
        @Suppress("UNCHECKED_CAST")
        val studyPlans = arr[6] as List<StudyPlan>

        val subjects = if (profile != null) {
            CurriculumCatalog.subjectsFor(profile.gradeLevel, profile.branch)
        } else {
            emptyList()
        }

        val startOfToday = startOfDayMillis(System.currentTimeMillis())
        val totalToday = logs.filter { it.recordedAtMillis >= startOfToday }.sumOf { it.totalCount }
        val totalSolved = logs.sumOf { it.totalCount }
        val totalCorrect = logs.sumOf { it.correctCount }
        val accuracy = if (totalSolved > 0) ((totalCorrect * 100.0) / totalSolved).toInt() else 0
        val avgWritten = if (grades.isNotEmpty()) grades.map { it.score }.average() else 0.0
        val latestNet = mocks.firstOrNull()?.totalNet ?: 0.0

        val summaries = subjects.map { subject ->
            val subGrades = grades.filter { it.subjectId == subject.id || it.subjectName == subject.name }
            val subLogs = logs.filter { it.subjectId == subject.id || it.subjectName == subject.name }
            val sSolved = subLogs.sumOf { it.totalCount }
            val sCorrect = subLogs.sumOf { it.correctCount }
            val sWrong = subLogs.sumOf { it.wrongCount }
            val sBlank = subLogs.sumOf { it.blankCount }
            val sAcc = if (sSolved > 0) ((sCorrect * 100.0) / sSolved).toInt() else 0
            val sLatestNet = mocks.firstOrNull()?.parsedSubjects()
                ?.firstOrNull { it.subjectId == subject.id || it.subjectName == subject.name }?.net

            SubjectSummary(
                subject = subject,
                writtenAverage = if (subGrades.isNotEmpty()) subGrades.map { it.score }.average() else null,
                writtenCount = subGrades.size,
                totalSolved = sSolved,
                totalCorrect = sCorrect,
                totalWrong = sWrong,
                totalBlank = sBlank,
                accuracyPercent = sAcc,
                latestMockNet = sLatestNet
            )
        }

        AcademicDashboardSnapshot(
            profile = profile,
            subjects = subjects,
            writtenGrades = grades,
            questionLogs = logs,
            mockExams = mocks,
            customCountdowns = countdowns,
            aiReports = reports,
            studyPlans = studyPlans,
            subjectSummaries = summaries,
            totalSolvedToday = totalToday,
            totalSolvedAllTime = totalSolved,
            totalCorrectAllTime = totalCorrect,
            overallWrittenAverage = avgWritten,
            latestMockNet = latestNet,
            overallAccuracyPercent = accuracy
        )
    }.toUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = UiState.Loading
    )

    val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard()
            .toUiState()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    fun setGranularity(granularity: TimeGranularity) {
        _selectedGranularity.value = granularity
    }

    fun setSubjectFilter(subjectId: String?) {
        _selectedSubjectIdFilter.value = subjectId
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun saveProfile(profile: UserProfile, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val res = repository.saveUserProfile(currentUserId, profile)
            _isSaving.value = false
            res.onSuccess {
                syncLeaderboardNow(profile)
                _actionMessage.value = "Profil ve MEB ders listesi kaydedildi."
                onSaved()
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Profil kaydedilemedi."
            }
        }
    }

    fun addWrittenGrade(
        subject: MebSubject,
        term: String,
        examType: String,
        score: Double,
        note: String?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val grade = WrittenGrade(
                userId = currentUserId,
                subjectId = subject.id,
                subjectName = subject.name,
                term = term,
                examType = examType,
                score = score,
                note = note,
                recordedAtMillis = System.currentTimeMillis()
            )
            val res = repository.addWrittenGrade(currentUserId, grade)
            _isSaving.value = false
            res.onSuccess {
                syncLeaderboardFromSnapshot()
                _actionMessage.value = "${subject.name} yazılı notu kaydedildi."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Yazılı notu kaydedilemedi."
            }
        }
    }

    fun deleteWrittenGrade(gradeId: String) {
        viewModelScope.launch {
            repository.deleteWrittenGrade(currentUserId, gradeId)
                .onSuccess { syncLeaderboardFromSnapshot() }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Silme işlemi başarısız." }
        }
    }

    fun addQuestionLog(
        subjectId: String,
        subjectName: String,
        topicName: String,
        difficulty: String,
        source: String,
        correct: Long,
        wrong: Long,
        blank: Long
    ) {
        viewModelScope.launch {
            val total = correct + wrong + blank
            if (total <= 0L) {
                _actionMessage.value = "En az 1 soru girmelisiniz."
                return@launch
            }
            val log = QuestionLog(
                userId = currentUserId,
                subjectId = subjectId,
                subjectName = subjectName,
                topicName = topicName,
                difficulty = difficulty,
                source = source,
                totalCount = total,
                correctCount = correct,
                wrongCount = wrong,
                blankCount = blank,
                recordedAtMillis = System.currentTimeMillis()
            )
            repository.addQuestionLog(currentUserId, log)
                .onSuccess {
                    syncLeaderboardFromSnapshot()
                    _actionMessage.value = "$subjectName ($topicName) için $total soru işlendi."
                }
                .onFailure {
                    _actionMessage.value = it.localizedMessage ?: "Soru kaydı eklenemedi."
                }
        }
    }

    fun deleteQuestionLog(logId: String) {
        viewModelScope.launch {
            repository.deleteQuestionLog(currentUserId, logId)
                .onSuccess { syncLeaderboardFromSnapshot() }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Kayıt silinemedi." }
        }
    }

    fun addMockExam(
        examTitle: String,
        examType: String,
        subjectResults: List<MockSubjectResult>
    ) {
        viewModelScope.launch {
            if (subjectResults.isEmpty()) {
                _actionMessage.value = "En az bir ders sonucu eklemelisiniz."
                return@launch
            }
            _isSaving.value = true
            val res = repository.addMockExam(
                userId = currentUserId,
                examTitle = examTitle,
                examType = examType,
                subjectResults = subjectResults,
                recordedAtMillis = System.currentTimeMillis()
            )
            _isSaving.value = false
            res.onSuccess {
                syncLeaderboardFromSnapshot()
                _actionMessage.value = "$examTitle deneme sonucu kaydedildi."
            }.onFailure {
                _actionMessage.value = it.localizedMessage ?: "Deneme kaydedilemedi."
            }
        }
    }

    fun deleteMockExam(examId: String) {
        viewModelScope.launch {
            repository.deleteMockExam(currentUserId, examId)
                .onSuccess { syncLeaderboardFromSnapshot() }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Deneme silinemedi." }
        }
    }

    fun addCustomCountdown(title: String, category: String, targetDateMillis: Long) {
        viewModelScope.launch {
            val item = CustomCountdown(
                userId = currentUserId,
                title = title,
                category = category,
                targetDateMillis = targetDateMillis
            )
            repository.addCustomCountdown(currentUserId, item)
                .onSuccess { _actionMessage.value = "Özel sayaç eklendi." }
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Sayaç eklenemedi." }
        }
    }

    fun deleteCustomCountdown(countdownId: String) {
        viewModelScope.launch {
            repository.deleteCustomCountdown(currentUserId, countdownId)
                .onFailure { _actionMessage.value = it.localizedMessage ?: "Sayaç silinemedi." }
        }
    }

    fun runAiAnalysis() {
        val currentSnap = (dashboardState.value as? UiState.Success)?.data ?: return
        val profile = currentSnap.profile ?: return
        viewModelScope.launch {
            _aiGenerationState.value = UiState.Loading
            val aiResult = aiRepository.analyzeStudentPerformance(
                profile = profile,
                writtenGrades = currentSnap.writtenGrades,
                questionLogs = currentSnap.questionLogs,
                mockExams = currentSnap.mockExams
            )
            aiResult.onSuccess { generated ->
                val saveRes = repository.saveAiReport(
                    userId = currentUserId,
                    summary = generated.summary,
                    weakAreas = generated.weakAreas,
                    priorityTasks = generated.priorityTasks,
                    studyTips = generated.studyTips,
                    resources = generated.resources
                )
                saveRes.onSuccess {
                    _aiGenerationState.value = UiState.Success(Unit)
                    _actionMessage.value = "Yapay zeka raporu oluşturuldu ve kaydedildi."
                }.onFailure { err ->
                    _aiGenerationState.value = UiState.Error(err.localizedMessage ?: "Rapor kaydedilemedi.")
                }
            }.onFailure { err ->
                _aiGenerationState.value = UiState.Error(err.localizedMessage ?: "Yapay zeka analizi başarısız.")
            }
        }
    }

    fun generateWeeklyStudyPlan(focusNote: String) {
        val currentSnap = (dashboardState.value as? UiState.Success)?.data ?: return
        val profile = currentSnap.profile ?: return
        viewModelScope.launch {
            _studyPlanGenState.value = UiState.Loading
            val aiResult = aiRepository.generateWeeklyStudyPlan(
                profile = profile,
                subjects = currentSnap.subjects,
                writtenGrades = currentSnap.writtenGrades,
                questionLogs = currentSnap.questionLogs,
                mockExams = currentSnap.mockExams,
                focusNote = focusNote.trim()
            )
            aiResult.onSuccess { generated ->
                val saveRes = repository.saveStudyPlan(
                    userId = currentUserId,
                    planTitle = generated.planTitle,
                    coachNote = generated.coachNote,
                    sessions = generated.sessions
                )
                saveRes.onSuccess {
                    _studyPlanGenState.value = UiState.Success(Unit)
                    _actionMessage.value = "Kişiselleştirilmiş haftalık çalışma programın hazır!"
                }.onFailure { err ->
                    _studyPlanGenState.value = UiState.Error(err.localizedMessage ?: "Çalışma programı kaydedilemedi.")
                }
            }.onFailure { err ->
                _studyPlanGenState.value = UiState.Error(err.localizedMessage ?: "Çalışma programı oluşturulamadı.")
            }
        }
    }

    fun toggleStudySession(plan: StudyPlan, sessionId: String) {
        viewModelScope.launch {
            val updatedSessions: List<StudySessionItem> = plan.parsedSessions().map { item ->
                if (item.sessionId == sessionId) item.copy(completed = !item.completed) else item
            }
            repository.updateStudyPlanSessions(currentUserId, plan.id, updatedSessions)
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Oturum durumu güncellenemedi."
                }
        }
    }

    fun deleteStudyPlan(planId: String) {
        viewModelScope.launch {
            repository.deleteStudyPlan(currentUserId, planId)
                .onSuccess { _actionMessage.value = "Çalışma programı silindi." }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "Program silinemedi."
                }
        }
    }

    private fun syncLeaderboardFromSnapshot() {
        val snap = (dashboardState.value as? UiState.Success)?.data ?: return
        val profile = snap.profile ?: return
        viewModelScope.launch {
            repository.syncLeaderboardEntry(
                userId = currentUserId,
                profile = profile,
                totalSolved = snap.totalSolvedAllTime,
                totalCorrect = snap.totalCorrectAllTime,
                avgWritten = snap.overallWrittenAverage,
                latestNet = snap.latestMockNet
            )
        }
    }

    private fun syncLeaderboardNow(profile: UserProfile) {
        val snap = (dashboardState.value as? UiState.Success)?.data
        viewModelScope.launch {
            repository.syncLeaderboardEntry(
                userId = currentUserId,
                profile = profile,
                totalSolved = snap?.totalSolvedAllTime ?: 0L,
                totalCorrect = snap?.totalCorrectAllTime ?: 0L,
                avgWritten = snap?.overallWrittenAverage ?: 0.0,
                latestNet = snap?.latestMockNet ?: 0.0
            )
        }
    }

    fun computeChartBuckets(
        snapshot: AcademicDashboardSnapshot,
        granularity: TimeGranularity,
        subjectIdFilter: String?
    ): List<ChartBucket> {
        val filteredLogs = if (subjectIdFilter == null) snapshot.questionLogs
        else snapshot.questionLogs.filter { it.subjectId == subjectIdFilter }

        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (granularity) {
            TimeGranularity.DAILY -> {
                val fmt = SimpleDateFormat("dd MMM", Locale("tr", "TR"))
                (6 downTo 0).map { daysAgo ->
                    cal.timeInMillis = now
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    val start = startOfDayMillis(cal.timeInMillis)
                    val end = start + 86_400_000L
                    val dayLogs = filteredLogs.filter { it.recordedAtMillis in start until end }
                    val dayMocks = snapshot.mockExams.filter { it.recordedAtMillis in start until end }
                    val avgNet = computeMockNetForBucket(dayMocks, subjectIdFilter)
                    ChartBucket(
                        label = fmt.format(Date(start)),
                        solvedCount = dayLogs.sumOf { it.totalCount },
                        correctCount = dayLogs.sumOf { it.correctCount },
                        avgNet = avgNet
                    )
                }
            }
            TimeGranularity.WEEKLY -> {
                (3 downTo 0).map { weeksAgo ->
                    val end = now - weeksAgo * 7L * 86_400_000L
                    val start = end - 7L * 86_400_000L
                    val logs = filteredLogs.filter { it.recordedAtMillis in start until end }
                    val mocks = snapshot.mockExams.filter { it.recordedAtMillis in start until end }
                    val label = if (weeksAgo == 0) "Bu Hft" else "-${weeksAgo} Hft"
                    ChartBucket(
                        label = label,
                        solvedCount = logs.sumOf { it.totalCount },
                        correctCount = logs.sumOf { it.correctCount },
                        avgNet = computeMockNetForBucket(mocks, subjectIdFilter)
                    )
                }
            }
            TimeGranularity.MONTHLY -> {
                val fmt = SimpleDateFormat("MMM", Locale("tr", "TR"))
                (5 downTo 0).map { monthsAgo ->
                    cal.timeInMillis = now
                    cal.add(Calendar.MONTH, -monthsAgo)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val start = startOfDayMillis(cal.timeInMillis)
                    cal.add(Calendar.MONTH, 1)
                    val end = startOfDayMillis(cal.timeInMillis)
                    val logs = filteredLogs.filter { it.recordedAtMillis in start until end }
                    val mocks = snapshot.mockExams.filter { it.recordedAtMillis in start until end }
                    ChartBucket(
                        label = fmt.format(Date(start)),
                        solvedCount = logs.sumOf { it.totalCount },
                        correctCount = logs.sumOf { it.correctCount },
                        avgNet = computeMockNetForBucket(mocks, subjectIdFilter)
                    )
                }
            }
            TimeGranularity.YEARLY -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                (2 downTo 0).map { yearsAgo ->
                    val yr = currentYear - yearsAgo
                    cal.set(yr, Calendar.JANUARY, 1, 0, 0, 0)
                    val start = cal.timeInMillis
                    cal.set(yr + 1, Calendar.JANUARY, 1, 0, 0, 0)
                    val end = cal.timeInMillis
                    val logs = filteredLogs.filter { it.recordedAtMillis in start until end }
                    val mocks = snapshot.mockExams.filter { it.recordedAtMillis in start until end }
                    ChartBucket(
                        label = yr.toString(),
                        solvedCount = logs.sumOf { it.totalCount },
                        correctCount = logs.sumOf { it.correctCount },
                        avgNet = computeMockNetForBucket(mocks, subjectIdFilter)
                    )
                }
            }
        }
    }

    fun computePeriodComparison(
        snapshot: AcademicDashboardSnapshot,
        granularity: TimeGranularity,
        subjectIdFilter: String?
    ): PeriodComparison {
        val windowMs = when (granularity) {
            TimeGranularity.DAILY -> 86_400_000L
            TimeGranularity.WEEKLY -> 7L * 86_400_000L
            TimeGranularity.MONTHLY -> 30L * 86_400_000L
            TimeGranularity.YEARLY -> 365L * 86_400_000L
        }
        val now = System.currentTimeMillis()
        val currentStart = now - windowMs
        val prevStart = currentStart - windowMs

        val logs = if (subjectIdFilter == null) snapshot.questionLogs
        else snapshot.questionLogs.filter { it.subjectId == subjectIdFilter }

        val curLogs = logs.filter { it.recordedAtMillis >= currentStart }
        val prevLogs = logs.filter { it.recordedAtMillis in prevStart until currentStart }

        val curSolved = curLogs.sumOf { it.totalCount }
        val curCor = curLogs.sumOf { it.correctCount }
        val prevSolved = prevLogs.sumOf { it.totalCount }
        val prevCor = prevLogs.sumOf { it.correctCount }

        val curMocks = snapshot.mockExams.filter { it.recordedAtMillis >= currentStart }
        val prevMocks = snapshot.mockExams.filter { it.recordedAtMillis in prevStart until currentStart }

        return PeriodComparison(
            currentSolved = curSolved,
            previousSolved = prevSolved,
            currentAccuracy = if (curSolved > 0) ((curCor * 100.0) / curSolved).toInt() else 0,
            previousAccuracy = if (prevSolved > 0) ((prevCor * 100.0) / prevSolved).toInt() else 0,
            currentAvgNet = computeMockNetForBucket(curMocks, subjectIdFilter),
            previousAvgNet = computeMockNetForBucket(prevMocks, subjectIdFilter)
        )
    }

    private fun computeMockNetForBucket(mocks: List<MockExam>, subjectIdFilter: String?): Double {
        if (mocks.isEmpty()) return 0.0
        return if (subjectIdFilter == null) {
            mocks.map { it.totalNet }.average()
        } else {
            val nets = mocks.mapNotNull { exam ->
                exam.parsedSubjects().firstOrNull { it.subjectId == subjectIdFilter }?.net
            }
            if (nets.isEmpty()) 0.0 else nets.average()
        }
    }

    private fun startOfDayMillis(millis: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = millis
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private companion object {
        const val TAG = "AcademicVM"
    }
}
