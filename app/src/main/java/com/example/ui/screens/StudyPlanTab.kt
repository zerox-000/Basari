package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyPlan
import com.example.data.model.StudySessionItem
import com.example.data.model.UiState
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AcademicDashboardSnapshot

private val daysOfWeekOrder = listOf(
    "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"
)

private val quickFocusPresets = listOf(
    "Eksik Konu Odaklı",
    "Hafta Sonu Denemeli",
    "Yazılı Hazırlığı Ağırlıklı",
    "Yoğun Soru Çözümü"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyPlanTab(
    snapshot: AcademicDashboardSnapshot,
    studyPlanGenState: UiState<Unit>,
    onGenerateStudyPlan: (String) -> Unit,
    onToggleStudySession: (StudyPlan, String) -> Unit,
    onDeleteStudyPlan: (String) -> Unit
) {
    val profile = snapshot.profile
    var focusNote by remember { mutableStateOf("") }
    var selectedPlanId by remember(snapshot.studyPlans) {
        mutableStateOf(snapshot.studyPlans.firstOrNull()?.id)
    }
    var selectedDayFilter by remember { mutableStateOf<String?>(null) }
    var pendingDeletePlanId by remember { mutableStateOf<String?>(null) }

    val activePlan = remember(snapshot.studyPlans, selectedPlanId) {
        snapshot.studyPlans.firstOrNull { it.id == selectedPlanId } ?: snapshot.studyPlans.firstOrNull()
    }

    val allSessions = remember(activePlan) {
        activePlan?.parsedSessions().orEmpty()
    }
    val completedCount = remember(allSessions) { allSessions.count { it.completed } }
    val totalMinutes = remember(allSessions) { allSessions.sumOf { it.durationMinutes } }
    val totalTargetQuestions = remember(allSessions) { allSessions.sumOf { it.targetQuestions } }
    val progressRatio = if (allSessions.isNotEmpty()) completedCount.toFloat() / allSessions.size.toFloat() else 0f

    val groupedByDay = remember(allSessions, selectedDayFilter) {
        val daySequence = if (selectedDayFilter != null) listOf(selectedDayFilter!!) else daysOfWeekOrder
        daySequence.mapNotNull { day ->
            val items = allSessions.filter { it.dayOfWeek == day }
            if (items.isEmpty()) null else day to items
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Generator Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Haftalık Çalışma Programı Oluşturucu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Haftalık Ders Çalışma Programı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Profilin, öğrenme stilin, ders saatlerin ve eksik konularına göre 7 günlük kişisel plan üretir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (profile != null) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfileContextPill("${profile.gradeLevel} • ${profile.branch}")
                            ProfileContextPill("Hedef: ${profile.targetExam}")
                            ProfileContextPill("Stil: ${profile.learningStyle}")
                            ProfileContextPill("${profile.preferredStudyHours}")
                            ProfileContextPill("${profile.dailyStudyMinutes} dk / ${profile.dailyQuestionGoal} soru")
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickFocusPresets.forEach { preset ->
                            FilterChip(
                                selected = focusNote.contains(preset),
                                onClick = {
                                    focusNote = if (focusNote.contains(preset)) {
                                        focusNote.replace(preset, "").trim(' ', ',', ';')
                                    } else if (focusNote.isBlank()) {
                                        preset
                                    } else {
                                        "$focusNote, $preset"
                                    }
                                },
                                label = { Text(preset) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = focusNote,
                        onValueChange = { focusNote = it },
                        label = { Text("Özel Program Tercihi (Opsiyonel)") },
                        placeholder = { Text("Örn: Matematik ve Fizik ağırlıklı, hafta sonu deneme olsun") },
                        singleLine = false,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_plan_focus_input")
                    )

                    val isGenerating = studyPlanGenState is UiState.Loading
                    Button(
                        onClick = { onGenerateStudyPlan(focusNote) },
                        enabled = !isGenerating && profile != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_study_plan_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Haftalık Program Hazırlanıyor...")
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Yapay Zeka ile Program Oluştur")
                        }
                    }

                    if (studyPlanGenState is UiState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = studyPlanGenState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Saved Plan Switcher (if multiple)
        if (snapshot.studyPlans.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    snapshot.studyPlans.forEachIndexed { idx, plan ->
                        FilterChip(
                            selected = plan.id == activePlan?.id,
                            onClick = { selectedPlanId = plan.id },
                            label = {
                                Text(
                                    text = if (idx == 0) "Güncel: ${plan.planTitle.take(24)}" else plan.planTitle.take(24),
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        }

        if (activePlan == null) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Henüz Haftalık Programın Yok",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Yukarıdaki butona dokunarak MEB müfredatı derslerine, yazılı notlarına ve günlük çalışma sürene uygun kişisel haftalık programını hemen oluştur.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Active Plan Overview Card
            item {
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = activePlan.planTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activePlan.coachNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { pendingDeletePlanId = activePlan.id },
                                modifier = Modifier.testTag("delete_study_plan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Programı Sil",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Haftalık İlerleme: $completedCount / ${allSessions.size} oturum",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${totalMinutes / 60} sa ${totalMinutes % 60} dk • $totalTargetQuestions soru",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // Day Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedDayFilter == null,
                        onClick = { selectedDayFilter = null },
                        label = { Text("Tüm Hafta") },
                        modifier = Modifier.testTag("day_filter_all")
                    )
                    daysOfWeekOrder.forEach { day ->
                        val dayCount = allSessions.count { it.dayOfWeek == day }
                        if (dayCount > 0) {
                            FilterChip(
                                selected = selectedDayFilter == day,
                                onClick = { selectedDayFilter = if (selectedDayFilter == day) null else day },
                                label = { Text("$day ($dayCount)") },
                                modifier = Modifier.testTag("day_filter_$day")
                            )
                        }
                    }
                }
            }

            // Grouped Sessions by Day
            groupedByDay.forEach { (day, daySessions) ->
                item(key = "header_$day") {
                    val dayDone = daySessions.count { it.completed }
                    val dayMins = daySessions.sumOf { it.durationMinutes }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$dayDone/${daySessions.size} tamam • $dayMins dk",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = daySessions,
                    key = { "${activePlan.id}_${it.sessionId}" }
                ) { session ->
                    StudySessionCard(
                        session = session,
                        onToggle = { onToggleStudySession(activePlan, session.sessionId) }
                    )
                }
            }
        }
    }

    pendingDeletePlanId?.let { planId ->
        AlertDialog(
            onDismissRequest = { pendingDeletePlanId = null },
            title = { Text("Programı Sil") },
            text = { Text("Bu haftalık çalışma programını kalıcı olarak silmek istediğine emin misin?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteStudyPlan(planId)
                        pendingDeletePlanId = null
                    }
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePlanId = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@Composable
private fun ProfileContextPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StudySessionCard(
    session: StudySessionItem,
    onToggle: () -> Unit
) {
    val activityColor = when (session.activityType) {
        "Soru Çözümü" -> StatusSuccess
        "Konu Tekrarı" -> StatusInfo
        "Deneme Analizi" -> StatusWarning
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (session.completed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (session.completed) StatusSuccess.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onToggle)
            .testTag("session_card_${session.sessionId}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = session.completed,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("session_check_${session.sessionId}")
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.timeSlot,
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = activityColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = session.activityType,
                            style = MaterialTheme.typography.labelSmall,
                            color = activityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = session.subjectName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (session.completed) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = session.topicName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${session.durationMinutes} dk",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (session.targetQuestions > 0) {
                        Text(
                            text = "• ${session.targetQuestions} soru hedefi",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (session.completed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Tamamlandı",
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Tamamlandı",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
