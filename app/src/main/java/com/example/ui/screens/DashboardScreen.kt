package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CurriculumCatalog
import com.example.data.model.MebSubject
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.ChartColors
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AcademicDashboardSnapshot
import com.example.ui.viewmodel.SubjectSummary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    snapshot: AcademicDashboardSnapshot,
    onAddWrittenGrade: (MebSubject, String, String, Double, String?) -> Unit,
    onDeleteWrittenGrade: (String) -> Unit,
    onAddQuestionLog: (String, String, String, String, String, Long, Long, Long) -> Unit,
    onNavigateToMocks: () -> Unit,
    onNavigateToStudyPlan: () -> Unit
) {
    val profile = snapshot.profile ?: return
    var selectedSubjectForSheet by remember { mutableStateOf<SubjectSummary?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }
    var showQuestionLogDialog by remember { mutableStateOf(false) }
    var preselectedSubject by remember { mutableStateOf(snapshot.subjects.firstOrNull()) }

    val dailyGoal = profile.dailyQuestionGoal.coerceAtLeast(1L)
    val goalProgress = (snapshot.totalSolvedToday.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Greeting & Daily Target Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Merhaba, ${profile.firstName}!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${profile.gradeLevel} • ${profile.branch} • Hedef: ${profile.targetExam}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${profile.subjectsCountLabel(snapshot.subjects.size)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bugünkü Soru Hedefi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "${snapshot.totalSolvedToday} / $dailyGoal soru",
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { goalProgress },
                                color = MaterialTheme.colorScheme.tertiary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Summary Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Yazılı Ortalaması",
                        value = if (snapshot.writtenGrades.isNotEmpty()) "%.1f".format(snapshot.overallWrittenAverage) else "--",
                        subtitle = "${snapshot.writtenGrades.size} yazılı kaydı",
                        icon = Icons.Rounded.Grade,
                        accentColor = StatusWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Toplam Soru",
                        value = snapshot.totalSolvedAllTime.toString(),
                        subtitle = "%${snapshot.overallAccuracyPercent} doğruluk",
                        icon = Icons.Rounded.FactCheck,
                        accentColor = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Son Deneme Neti",
                        value = if (snapshot.mockExams.isNotEmpty()) "%.2f".format(snapshot.latestMockNet) else "--",
                        subtitle = "${snapshot.mockExams.size} deneme kaydı",
                        icon = Icons.Rounded.TrendingUp,
                        accentColor = StatusInfo,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Doğru Sayısı",
                        value = snapshot.totalCorrectAllTime.toString(),
                        subtitle = "Toplam doğru yanıt",
                        icon = Icons.Rounded.Quiz,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Entry Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        preselectedSubject = snapshot.subjects.firstOrNull()
                        showGradeDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_grade_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Yazılı Ekle", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = {
                        preselectedSubject = snapshot.subjects.firstOrNull()
                        showQuestionLogDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_question_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Soru Gir", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = onNavigateToMocks,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_mock_button")
                ) {
                    Icon(Icons.Rounded.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Deneme", maxLines = 1)
                }
            }
        }

        // AI Weekly Study Plan Quick Access Banner
        item {
            val latestPlan = snapshot.studyPlans.firstOrNull()
            val sessions = remember(latestPlan) { latestPlan?.parsedSessions().orEmpty() }
            val completedCount = remember(sessions) { sessions.count { it.completed } }
            ElevatedCard(
                onClick = onNavigateToStudyPlan,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_study_plan_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Haftalık Çalışma Programı",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Haftalık Çalışma Programım",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = if (latestPlan != null && sessions.isNotEmpty()) {
                                "${latestPlan.planTitle} • $completedCount/${sessions.size} oturum tamamlandı"
                            } else {
                                "Profiline ve hedeflerine uygun AI haftalık ders programı oluştur"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // MEB Curriculum Subject List
        item {
            Text(
                text = "MEB Müfredat Derslerin (${snapshot.subjectSummaries.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(snapshot.subjectSummaries, key = { _, item -> item.subject.id }) { idx, summary ->
            val accent = ChartColors[idx % ChartColors.size]
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedSubjectForSheet = summary }
                    .testTag("subject_card_${summary.subject.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = summary.subject.shortName,
                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFamily),
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = summary.subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${summary.totalSolved} soru (%${summary.accuracyPercent} başarı) • ${summary.writtenCount} yazılı",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = summary.writtenAverage?.let { "%.1f".format(it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Not Ort.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Recent Written Grades Section
        if (snapshot.writtenGrades.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Son Girilen Yazılı Notları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            itemsIndexed(snapshot.writtenGrades.take(5), key = { _, g -> g.id }) { _, grade ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${grade.subjectName} — ${grade.term} ${grade.examType}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!grade.note.isNullOrBlank()) {
                                Text(
                                    text = grade.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.1f".format(grade.score),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onDeleteWrittenGrade(grade.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Notu Sil",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Subject Detail ModalBottomSheet
    selectedSubjectForSheet?.let { sum ->
        val subjectGrades = snapshot.writtenGrades.filter { it.subjectId == sum.subject.id }
        ModalBottomSheet(onDismissRequest = { selectedSubjectForSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = sum.subject.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Haftalık ${sum.subject.weeklyHours} ders saati • ${sum.totalSolved} çözülen soru (%${sum.accuracyPercent} doğru)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "MEB Müfredat Üniteleri:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sum.subject.topics.forEach { topic ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                if (subjectGrades.isNotEmpty()) {
                    Text(
                        text = "Yazılı Notları (${subjectGrades.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    subjectGrades.take(4).forEach { g ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${g.term} ${g.examType}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "%.1f / 100".format(g.score),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            preselectedSubject = sum.subject
                            selectedSubjectForSheet = null
                            showGradeDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Yazılı Notu Ekle")
                    }
                    FilledTonalButton(
                        onClick = {
                            preselectedSubject = sum.subject
                            selectedSubjectForSheet = null
                            showQuestionLogDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Soru Sayısı Gir")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showGradeDialog && snapshot.subjects.isNotEmpty()) {
        AddWrittenGradeDialog(
            subjects = snapshot.subjects,
            initialSubject = preselectedSubject ?: snapshot.subjects.first(),
            onDismiss = { showGradeDialog = false },
            onSave = { sub, term, examType, score, note ->
                onAddWrittenGrade(sub, term, examType, score, note)
                showGradeDialog = false
            }
        )
    }

    if (showQuestionLogDialog && snapshot.subjects.isNotEmpty()) {
        AddQuestionLogDialog(
            subjects = snapshot.subjects,
            initialSubject = preselectedSubject ?: snapshot.subjects.first(),
            onDismiss = { showQuestionLogDialog = false },
            onSave = { sub, topic, diff, correct, wrong, blank ->
                onAddQuestionLog(
                    sub.id,
                    sub.name,
                    topic,
                    diff,
                    "Bireysel Çalışma",
                    correct,
                    wrong,
                    blank
                )
                showQuestionLogDialog = false
            }
        )
    }
}

private fun com.example.data.model.UserProfile.subjectsCountLabel(count: Int): String = "$count MEB Dersi"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddWrittenGradeDialog(
    subjects: List<MebSubject>,
    initialSubject: MebSubject,
    onDismiss: () -> Unit,
    onSave: (MebSubject, String, String, Double, String?) -> Unit
) {
    var selectedSub by remember { mutableStateOf(initialSubject) }
    var term by remember { mutableStateOf("1. Dönem") }
    var examType by remember { mutableStateOf("1. Yazılı") }
    var scoreText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yazılı Notu Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ders", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjects.forEach { s ->
                        FilterChip(
                            selected = selectedSub.id == s.id,
                            onClick = { selectedSub = s },
                            label = { Text(s.shortName) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1. Dönem", "2. Dönem").forEach { t ->
                        FilterChip(selected = term == t, onClick = { term = t }, label = { Text(t) })
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1. Yazılı", "2. Yazılı", "Performans", "Proje").forEach { et ->
                        FilterChip(selected = examType == et, onClick = { examType = et }, label = { Text(et) })
                    }
                }
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { scoreText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Puan (0 - 100)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("written_score_input")
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Kısa Not / Eksik Konu (Opsiyonel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = scoreText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: return@Button
                    onSave(selectedSub, term, examType, s, note.ifBlank { null })
                },
                enabled = scoreText.toDoubleOrNull() != null,
                modifier = Modifier.testTag("save_written_grade_button")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddQuestionLogDialog(
    subjects: List<MebSubject>,
    initialSubject: MebSubject,
    onDismiss: () -> Unit,
    onSave: (MebSubject, String, String, Long, Long, Long) -> Unit
) {
    var selectedSub by remember { mutableStateOf(initialSubject) }
    var selectedTopic by remember(selectedSub) { mutableStateOf(selectedSub.topics.firstOrNull() ?: "Genel") }
    var difficulty by remember { mutableStateOf("Orta") }
    var correctText by remember { mutableStateOf("15") }
    var wrongText by remember { mutableStateOf("3") }
    var blankText by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Çözülen Soru Girişi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ders", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjects.forEach { s ->
                        FilterChip(
                            selected = selectedSub.id == s.id,
                            onClick = { selectedSub = s },
                            label = { Text(s.shortName) }
                        )
                    }
                }
                Text("Konu", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedSub.topics.take(5).forEach { topic ->
                        FilterChip(
                            selected = selectedTopic == topic,
                            onClick = { selectedTopic = topic },
                            label = { Text(topic) }
                        )
                    }
                }
                Text("Zorluk", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurriculumCatalog.difficulties.forEach { d ->
                        FilterChip(selected = difficulty == d, onClick = { difficulty = d }, label = { Text(d) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = correctText,
                        onValueChange = { correctText = it.filter { c -> c.isDigit() } },
                        label = { Text("Doğru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wrongText,
                        onValueChange = { wrongText = it.filter { c -> c.isDigit() } },
                        label = { Text("Yanlış") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = blankText,
                        onValueChange = { blankText = it.filter { c -> c.isDigit() } },
                        label = { Text("Boş") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = correctText.toLongOrNull() ?: 0L
                    val w = wrongText.toLongOrNull() ?: 0L
                    val b = blankText.toLongOrNull() ?: 0L
                    if (c + w + b > 0L) {
                        onSave(selectedSub, selectedTopic, difficulty, c, w, b)
                    }
                },
                modifier = Modifier.testTag("save_question_log_button")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
