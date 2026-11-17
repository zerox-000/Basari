package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyPlan
import com.example.data.model.UiState
import com.example.ui.components.AccuracyDonutChart
import com.example.ui.components.SubjectComparisonChart
import com.example.ui.components.TrendLineAreaChart
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.viewmodel.AcademicDashboardSnapshot
import com.example.ui.viewmodel.ChartBucket
import com.example.ui.viewmodel.PeriodComparison
import com.example.ui.viewmodel.TimeGranularity

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsAndAiScreen(
    snapshot: AcademicDashboardSnapshot,
    initialSubTab: Int = 0,
    granularity: TimeGranularity,
    selectedSubjectId: String?,
    chartBuckets: List<ChartBucket>,
    comparison: PeriodComparison,
    aiGenerationState: UiState<Unit>,
    studyPlanGenState: UiState<Unit>,
    onGranularityChange: (TimeGranularity) -> Unit,
    onSubjectFilterChange: (String?) -> Unit,
    onRunAiAnalysis: () -> Unit,
    onGenerateStudyPlan: (String) -> Unit,
    onToggleStudySession: (StudyPlan, String) -> Unit,
    onDeleteStudyPlan: (String) -> Unit
) {
    var selectedSubTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }
    var showNetChart by remember { mutableStateOf(false) }

    val filteredLogs = remember(snapshot.questionLogs, selectedSubjectId) {
        if (selectedSubjectId == null) snapshot.questionLogs
        else snapshot.questionLogs.filter { it.subjectId == selectedSubjectId }
    }
    val totalCorrect = filteredLogs.sumOf { it.correctCount }
    val totalWrong = filteredLogs.sumOf { it.wrongCount }
    val totalBlank = filteredLogs.sumOf { it.blankCount }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Analiz & AI Koçu") },
                modifier = Modifier.testTag("tab_analytics_ai")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Çalışma Programı") },
                modifier = Modifier.testTag("tab_study_plan")
            )
        }

        if (selectedSubTab == 1) {
            StudyPlanTab(
                snapshot = snapshot,
                studyPlanGenState = studyPlanGenState,
                onGenerateStudyPlan = onGenerateStudyPlan,
                onToggleStudySession = onToggleStudySession,
                onDeleteStudyPlan = onDeleteStudyPlan
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Timeframe Switcher: Günlük, Haftalık, Aylık, Yıllık
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Zaman Aralığı & Ders Analizi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    TimeGranularity.entries.forEachIndexed { idx, g ->
                        SegmentedButton(
                            selected = granularity == g,
                            onClick = { onGranularityChange(g) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = idx,
                                count = TimeGranularity.entries.size
                            ),
                            modifier = Modifier.testTag("granularity_${g.name}")
                        ) {
                            Text(
                                text = g.label,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { onSubjectFilterChange(null) },
                        label = { Text("Tümü (Total)") }
                    )
                    snapshot.subjects.forEach { s ->
                        FilterChip(
                            selected = selectedSubjectId == s.id,
                            onClick = { onSubjectFilterChange(if (selectedSubjectId == s.id) null else s.id) },
                            label = { Text(s.shortName) }
                        )
                    }
                }
            }
        }

        // Period Comparison Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CompareArrows, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${granularity.label} Karşılaştırma (Bu Dönem vs Önceki)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ComparisonBadge(
                            label = "Çözülen Soru",
                            current = comparison.currentSolved.toString(),
                            delta = comparison.currentSolved - comparison.previousSolved,
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonBadge(
                            label = "Doğruluk",
                            current = "%${comparison.currentAccuracy}",
                            delta = (comparison.currentAccuracy - comparison.previousAccuracy).toLong(),
                            suffix = "%",
                            modifier = Modifier.weight(1f)
                        )
                        ComparisonBadge(
                            label = "Ort. Net",
                            current = "%.1f".format(comparison.currentAvgNet),
                            delta = (comparison.currentAvgNet - comparison.previousAvgNet).toLong(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Trend Chart Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showNetChart) "Deneme Neti Trendi" else "Çözülen Soru Trendi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !showNetChart,
                                onClick = { showNetChart = false },
                                label = { Text("Soru") }
                            )
                            FilterChip(
                                selected = showNetChart,
                                onClick = { showNetChart = true },
                                label = { Text("Net") }
                            )
                        }
                    }
                    TrendLineAreaChart(
                        buckets = chartBuckets,
                        showNetInsteadOfSolved = showNetChart
                    )
                }
            }
        }

        // Accuracy Donut & Subject Comparison
        item {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Doğru / Yanlış / Boş Dağılımı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    AccuracyDonutChart(
                        correct = totalCorrect,
                        wrong = totalWrong,
                        blank = totalBlank
                    )
                }
            }
        }

        if (snapshot.subjectSummaries.isNotEmpty()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ders Bazlı Performans Karşılaştırması",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        SubjectComparisonChart(summaries = snapshot.subjectSummaries)
                    }
                }
            }
        }

        // AI Feedback Section
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
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Yapay Zeka Koçu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yapay Zeka Geri Bildirim Sistemi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Yazılı, soru bankası ve deneme verilerini analiz ederek sana özel çalışma planı çıkarır.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val isAiLoading = aiGenerationState is UiState.Loading
                    Button(
                        onClick = onRunAiAnalysis,
                        enabled = !isAiLoading,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("run_ai_analysis_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Verilerin Analiz Ediliyor...")
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Yapay Zeka ile Analiz Et")
                        }
                    }

                    if (aiGenerationState is UiState.Error) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiGenerationState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    val latestReport = snapshot.aiReports.firstOrNull()
                    if (latestReport != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Genel Değerlendirme",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = latestReport.summary,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                val weakAreas = latestReport.parsedWeakAreas()
                                if (weakAreas.isNotEmpty()) {
                                    Text(
                                        text = "Tespit Edilen Eksik Konu & Dersler",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    weakAreas.forEach { w ->
                                        val sevColor = when (w.severity) {
                                            "Yüksek" -> StatusError
                                            "Düşük" -> StatusSuccess
                                            else -> StatusWarning
                                        }
                                        Surface(
                                            color = sevColor.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, sevColor.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${w.subject} • ${w.topic}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = w.severity,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = sevColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                if (w.reason.isNotBlank()) {
                                                    Text(
                                                        text = w.reason,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val tasks = latestReport.parsedPriorityTasks()
                                if (tasks.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.PriorityHigh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Önceliklendirme Listesi",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    tasks.forEachIndexed { i, task ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${i + 1}. ${task.title}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${task.priority} • ${task.durationMinutes} dk",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (latestReport.studyTips.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.TipsAndUpdates, null, tint = StatusWarning, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Sana Özel Çalışma Önerileri",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    latestReport.studyTips.forEach { tip ->
                                        Text("• $tip", style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                if (latestReport.resources.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.Book, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Ek Kaynak Tavsiyeleri",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    latestReport.resources.forEach { res ->
                                        Text("• $res", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
private fun ComparisonBadge(
    label: String,
    current: String,
    delta: Long,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    val deltaColor = when {
        delta > 0L -> StatusSuccess
        delta < 0L -> StatusError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val deltaPrefix = if (delta > 0L) "+" else ""

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = current,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$deltaPrefix$delta$suffix",
                style = MaterialTheme.typography.labelSmall,
                color = deltaColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
