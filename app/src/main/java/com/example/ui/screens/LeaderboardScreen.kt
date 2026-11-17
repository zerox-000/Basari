package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusWarning

@Composable
fun LeaderboardScreen(
    currentUserId: String,
    currentProfile: UserProfile?,
    leaderboardState: UiState<List<LeaderboardEntry>>
) {
    var filterMode by remember { mutableStateOf("Tümü") } // Tümü, Sınıfım, Bölümüm
    var inspectedUser by remember { mutableStateOf<LeaderboardEntry?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = StatusWarning)
                Text(
                    text = "Online Başarı Sıralaması",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Tümü", "Sınıfım", "Bölümüm").forEach { mode ->
                    FilterChip(
                        selected = filterMode == mode,
                        onClick = { filterMode = mode },
                        label = { Text(mode) },
                        modifier = Modifier.testTag("leaderboard_filter_$mode")
                    )
                }
            }
        }

        when (leaderboardState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = leaderboardState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is UiState.Success -> {
                val filtered = remember(leaderboardState.data, filterMode, currentProfile) {
                    leaderboardState.data.filter { entry ->
                        when (filterMode) {
                            "Sınıfım" -> currentProfile == null || entry.gradeLevel == currentProfile.gradeLevel
                            "Bölümüm" -> currentProfile == null || entry.branch == currentProfile.branch
                            else -> true
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu kategoride henüz sıralama verisi bulunmuyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(filtered, key = { _, e -> e.userId }) { index, entry ->
                            val isMe = entry.userId == currentUserId
                            val rank = index + 1
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { inspectedUser = entry }
                                    .testTag("leaderboard_row_${entry.userId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (rank) {
                                            1 -> StatusWarning.copy(alpha = 0.22f)
                                            2 -> MaterialTheme.colorScheme.secondaryContainer
                                            3 -> MaterialTheme.colorScheme.tertiaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#$rank",
                                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMonoFamily),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isMe) "${entry.displayName} (Sen)" else entry.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${entry.gradeLevel} • ${entry.branch} • ${entry.totalSolvedQuestions} soru",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "%.0f P".format(entry.rankingScore),
                                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Net %.1f".format(entry.latestMockNet),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectedUser?.let { u ->
        val accuracy = if (u.totalSolvedQuestions > 0) {
            ((u.totalCorrectQuestions * 100.0) / u.totalSolvedQuestions).toInt()
        } else 0
        AlertDialog(
            onDismissRequest = { inspectedUser = null },
            icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
            title = { Text(u.displayName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatLine("Sınıf & Bölüm", "${u.gradeLevel} • ${u.branch}")
                    ProfileStatLine("Hedef Sınav", u.targetExam)
                    ProfileStatLine("Çözülen Soru", "${u.totalSolvedQuestions} (%$accuracy başarı)")
                    ProfileStatLine("Yazılı Ortalaması", "%.1f".format(u.averageWrittenGrade))
                    ProfileStatLine("Son Deneme Neti", "%.2f".format(u.latestMockNet))
                    ProfileStatLine("Sıralama Puanı", "%.0f Puan".format(u.rankingScore))
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedUser = null }) { Text("Kapat") }
            }
        )
    }
}

@Composable
private fun ProfileStatLine(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}
