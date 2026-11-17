package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CurriculumCatalog
import com.example.data.model.MebSubject
import com.example.data.model.MockSubjectResult
import com.example.data.model.QuestionBankItem
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AcademicDashboardSnapshot

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankAndMocksScreen(
    snapshot: AcademicDashboardSnapshot,
    initialSubTab: Int = 0,
    onAddQuestionLog: (String, String, String, String, String, Long, Long, Long) -> Unit,
    onAddMockExam: (String, String, List<MockSubjectResult>) -> Unit,
    onDeleteMockExam: (String) -> Unit
) {
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("MEB Soru Bankası") },
                modifier = Modifier.testTag("tab_question_bank")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Deneme Sınavları") },
                modifier = Modifier.testTag("tab_mock_exams")
            )
        }

        if (selectedTab == 0) {
            QuestionBankTab(
                subjects = snapshot.subjects,
                onAddQuestionLog = onAddQuestionLog
            )
        } else {
            MockExamsTab(
                snapshot = snapshot,
                onAddMockExam = onAddMockExam,
                onDeleteMockExam = onDeleteMockExam
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionBankTab(
    subjects: List<MebSubject>,
    onAddQuestionLog: (String, String, String, String, String, Long, Long, Long) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    val selectedOptionMap = remember { mutableStateMapOf<String, Int>() }
    val submittedMap = remember { mutableStateMapOf<String, String>() } // "Doğru", "Yanlış", "Boş"

    val subjectIds = remember(subjects) { subjects.map { it.id }.toSet() }
    val filteredQuestions = remember(selectedSubjectId, selectedDifficulty, subjectIds) {
        val relevant = CurriculumCatalog.questionBank.filter {
            subjectIds.isEmpty() || it.subjectId in subjectIds || it.subjectId == "matematik" || it.subjectId == "ingilizce"
        }
        relevant.filter { q ->
            (selectedSubjectId == null || q.subjectId == selectedSubjectId) &&
                (selectedDifficulty == null || q.difficulty == selectedDifficulty)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ders Filtresi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text("Tümü") }
                    )
                    subjects.forEach { s ->
                        FilterChip(
                            selected = selectedSubjectId == s.id,
                            onClick = { selectedSubjectId = if (selectedSubjectId == s.id) null else s.id },
                            label = { Text(s.shortName) }
                        )
                    }
                }
                Text(
                    text = "Zorluk Seviyesi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedDifficulty == null,
                        onClick = { selectedDifficulty = null },
                        label = { Text("Tümü") }
                    )
                    CurriculumCatalog.difficulties.forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = if (selectedDifficulty == diff) null else diff },
                            label = { Text(diff) }
                        )
                    }
                }
            }
        }

        if (filteredQuestions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Bu filtre için soru bulunamadı. Diğer ders veya zorluk seviyelerini seçebilirsin.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(filteredQuestions, key = { it.id }) { q ->
                QuestionCard(
                    question = q,
                    selectedOption = selectedOptionMap[q.id],
                    submittedStatus = submittedMap[q.id],
                    onSelectOption = { idx ->
                        selectedOptionMap[q.id] = idx
                        val isCorrect = idx == q.correctOptionIndex
                        submittedMap[q.id] = if (isCorrect) "Doğru" else "Yanlış"
                        onAddQuestionLog(
                            q.subjectId,
                            q.subjectName,
                            q.topicName,
                            q.difficulty,
                            "Soru Bankası",
                            if (isCorrect) 1L else 0L,
                            if (!isCorrect) 1L else 0L,
                            0L
                        )
                    },
                    onManualMark = { status ->
                        submittedMap[q.id] = status
                        onAddQuestionLog(
                            q.subjectId,
                            q.subjectName,
                            q.topicName,
                            q.difficulty,
                            "Soru Bankası",
                            if (status == "Doğru") 1L else 0L,
                            if (status == "Yanlış") 1L else 0L,
                            if (status == "Boş") 1L else 0L
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionBankItem,
    selectedOption: Int?,
    submittedStatus: String?,
    onSelectOption: (Int) -> Unit,
    onManualMark: (String) -> Unit
) {
    val diffColor = when (question.difficulty) {
        "Kolay" -> StatusSuccess
        "Zor" -> StatusError
        else -> StatusWarning
    }
    val isAnswered = submittedStatus != null

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${question.subjectName} • ${question.topicName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = diffColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = question.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = diffColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            val letters = listOf("A", "B", "C", "D", "E")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEachIndexed { idx, optText ->
                    val isChosen = selectedOption == idx
                    val isCorrectOption = idx == question.correctOptionIndex
                    val borderColor = when {
                        isAnswered && isCorrectOption -> StatusSuccess
                        isAnswered && isChosen && !isCorrectOption -> StatusError
                        isChosen -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    }
                    val bgColor = when {
                        isAnswered && isCorrectOption -> StatusSuccess.copy(alpha = 0.10f)
                        isAnswered && isChosen && !isCorrectOption -> StatusError.copy(alpha = 0.10f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(if (isChosen || (isAnswered && isCorrectOption)) 1.5.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswered) { onSelectOption(idx) }
                            .testTag("option_${question.id}_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "${letters.getOrElse(idx) { "?" }})",
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (!isAnswered) {
                Text(
                    text = "Veya kağıt üzerinde çözdüysen durumunu işaretle (analize işlenir):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onManualMark("Doğru") },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Check, null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Doğru", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onManualMark("Yanlış") },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Close, null, tint = StatusError, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Yanlış", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onManualMark("Boş") },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Rounded.HelpOutline, null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Boş", maxLines = 1)
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Çözüm",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "İşaretlendi: $submittedStatus • Analize İşlendi",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockExamsTab(
    snapshot: AcademicDashboardSnapshot,
    onAddMockExam: (String, String, List<MockSubjectResult>) -> Unit,
    onDeleteMockExam: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_mock_exam_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Yeni Deneme Oluştur & Sonuç Gir")
            }
        }

        if (snapshot.mockExams.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Henüz deneme sınavı kaydın yok",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "TYT, AYT, LGS veya Branş denemesi oluşturup Doğru/Yanlış sonuçlarını gir; netlerin ve eksik konuların otomatik analiz edilsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(snapshot.mockExams, key = { it.id }) { exam ->
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.examTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${exam.examType} • ${exam.totalCorrect}D / ${exam.totalWrong}Y / ${exam.totalBlank}B",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "%.2f Net".format(exam.totalNet),
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "~%.1f Puan".format(exam.estimatedScore),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteMockExam(exam.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Denemeyi Sil",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        exam.parsedSubjects().forEach { sub ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sub.subjectName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${sub.correct}D ${sub.wrong}Y (${"%.2f".format(sub.net)} net)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateMockExamDialog(
            subjects = snapshot.subjects,
            defaultType = snapshot.profile?.targetExam ?: "TYT",
            onDismiss = { showCreateDialog = false },
            onSave = { title, type, results ->
                onAddMockExam(title, type, results)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateMockExamDialog(
    subjects: List<MebSubject>,
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (String, String, List<MockSubjectResult>) -> Unit
) {
    var title by remember { mutableStateOf("Deneme Sınavı #1") }
    var examType by remember {
        mutableStateOf(
            when (defaultType) {
                "LGS" -> "LGS"
                "YKS" -> "TYT"
                else -> "Genel Deneme"
            }
        )
    }
    val correctMap = remember { mutableStateMapOf<String, String>() }
    val wrongMap = remember { mutableStateMapOf<String, String>() }
    val blankMap = remember { mutableStateMapOf<String, String>() }
    val weakTopicMap = remember { mutableStateMapOf<String, String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deneme Sınavı Oluştur") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Deneme Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mock_exam_title_input")
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TYT", "AYT", "LGS", "Branş Denemesi", "Genel Deneme").forEach { t ->
                        FilterChip(selected = examType == t, onClick = { examType = t }, label = { Text(t) })
                    }
                }

                Text(
                    text = "Ders Bazlı Sonuçlar (4 Yanlış 1 Doğruyu Götürür):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                subjects.forEach { sub ->
                    val c = correctMap[sub.id]?.toLongOrNull() ?: 0L
                    val w = wrongMap[sub.id]?.toLongOrNull() ?: 0L
                    val net = c - (w / 4.0)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sub.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Net: %.2f".format(net),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = correctMap[sub.id] ?: "",
                                    onValueChange = { correctMap[sub.id] = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("D") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = wrongMap[sub.id] ?: "",
                                    onValueChange = { wrongMap[sub.id] = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Y") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = blankMap[sub.id] ?: "",
                                    onValueChange = { blankMap[sub.id] = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("B") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = weakTopicMap[sub.id] ?: "",
                                onValueChange = { weakTopicMap[sub.id] = it },
                                label = { Text("Eksik Konu (Opsiyonel)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val results = subjects.mapNotNull { sub ->
                        val c = correctMap[sub.id]?.toLongOrNull() ?: 0L
                        val w = wrongMap[sub.id]?.toLongOrNull() ?: 0L
                        val b = blankMap[sub.id]?.toLongOrNull() ?: 0L
                        if (c == 0L && w == 0L && b == 0L) null
                        else {
                            val weak = weakTopicMap[sub.id]?.trim().orEmpty()
                            MockSubjectResult(
                                subjectId = sub.id,
                                subjectName = sub.name,
                                correct = c,
                                wrong = w,
                                blank = b,
                                net = c - (w / 4.0),
                                weakTopics = if (weak.isNotEmpty()) listOf(weak) else emptyList()
                            )
                        }
                    }
                    if (results.isNotEmpty()) {
                        onSave(title.ifBlank { "$examType Denemesi" }, examType, results)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_mock_exam_button")
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
