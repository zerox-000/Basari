package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CountdownCatalog
import com.example.data.model.CustomCountdown
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountdownScreen(
    customCountdowns: List<CustomCountdown>,
    onAddCustomCountdown: (String, String, Long) -> Unit,
    onDeleteCustomCountdown: (String) -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var filterCategory by remember { mutableStateOf("Tümü") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val officialList = remember(nowMillis) {
        CountdownCatalog.upcomingOfficialMilestones(nowMillis)
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Sınav & Tatil Sayaçları",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_custom_countdown_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Ekle")
                }
            }
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Tümü", "YKS", "LGS", "Yazılı", "Tatil").forEach { cat ->
                    FilterChip(
                        selected = filterCategory == cat,
                        onClick = { filterCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        val filteredCustom = customCountdowns.filter {
            filterCategory == "Tümü" || it.category == filterCategory
        }
        if (filteredCustom.isNotEmpty()) {
            item {
                Text(
                    text = "Özel Sayaçların",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(filteredCustom, key = { it.id }) { item ->
                CountdownCard(
                    title = item.title,
                    subtitle = dateFormatter.format(Date(item.targetDateMillis)),
                    category = item.category,
                    targetMillis = item.targetDateMillis,
                    nowMillis = nowMillis,
                    onDelete = { onDeleteCustomCountdown(item.id) }
                )
            }
        }

        item {
            Text(
                text = "Resmi YKS, Tatil ve Yazılı Dönemleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val filteredOfficial = officialList.filter { (m, _) ->
            filterCategory == "Tümü" || m.category == filterCategory
        }
        items(filteredOfficial, key = { (m, _) -> m.id }) { (milestone, targetMillis) ->
            CountdownCard(
                title = milestone.title,
                subtitle = "${dateFormatter.format(Date(targetMillis))} • ${milestone.subtitle}",
                category = milestone.category,
                targetMillis = targetMillis,
                nowMillis = nowMillis,
                onDelete = null
            )
        }
    }

    if (showAddDialog) {
        AddCountdownDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, cat, daysFromNow ->
                val target = System.currentTimeMillis() + (daysFromNow * 86_400_000L)
                onAddCustomCountdown(title, cat, target)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CountdownCard(
    title: String,
    subtitle: String,
    category: String,
    targetMillis: Long,
    nowMillis: Long,
    onDelete: (() -> Unit)?
) {
    val diff = (targetMillis - nowMillis).coerceAtLeast(0L)
    val days = diff / 86_400_000L
    val hours = (diff % 86_400_000L) / 3_600_000L
    val mins = (diff % 3_600_000L) / 60_000L

    val (accent, icon) = when (category) {
        "YKS", "LGS" -> MaterialTheme.colorScheme.primary to Icons.Rounded.School
        "Tatil" -> StatusSuccess to Icons.Rounded.BeachAccess
        "Yazılı" -> StatusWarning to Icons.Rounded.EditCalendar
        else -> StatusInfo to Icons.Rounded.Event
    }

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = accent.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = category, tint = accent)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$days Gün",
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${hours}sa ${mins}dk",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Sayacı Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCountdownDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Yazılı") }
    var daysText by remember { mutableStateOf("14") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Özel Sayaç Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık (Örn: Matematik Yazılısı)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_countdown_title_input")
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Yazılı", "Sınav", "Tatil", "Hedef").forEach { c ->
                        FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                    }
                }
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Kaç Gün Sonra?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_countdown_days_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = daysText.toLongOrNull()?.coerceIn(1L, 1500L) ?: return@Button
                    if (title.isNotBlank()) {
                        onSave(title.trim(), category, d)
                    }
                },
                enabled = title.isNotBlank() && daysText.toLongOrNull() != null,
                modifier = Modifier.testTag("save_custom_countdown_button")
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
