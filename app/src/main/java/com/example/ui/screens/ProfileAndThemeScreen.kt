package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CurriculumCatalog
import com.example.data.model.UserProfile
import com.example.ui.theme.DarkModePref
import com.example.ui.theme.ThemePalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileAndThemeScreen(
    userId: String,
    profile: UserProfile,
    accountEmail: String,
    rememberLogin: Boolean,
    currentPalette: ThemePalette,
    currentDarkMode: DarkModePref,
    isSaving: Boolean,
    onToggleRememberLogin: (Boolean) -> Unit,
    onSelectPalette: (ThemePalette) -> Unit,
    onSelectDarkMode: (DarkModePref) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var firstName by remember(profile) { mutableStateOf(profile.firstName) }
    var lastName by remember(profile) { mutableStateOf(profile.lastName) }
    var gradeLevel by remember(profile) { mutableStateOf(profile.gradeLevel) }
    val branches = remember(gradeLevel) { CurriculumCatalog.branchesForGrade(gradeLevel) }
    var branch by remember(profile, gradeLevel) {
        mutableStateOf(if (profile.branch in branches) profile.branch else branches.first())
    }
    var targetScore by remember(profile) { mutableStateOf(profile.targetScore.toInt().toString()) }
    var dailyGoal by remember(profile) { mutableStateOf(profile.dailyQuestionGoal.toString()) }
    var dailyMinutes by remember(profile) { mutableStateOf(profile.dailyStudyMinutes.toString()) }
    var learningStyle by remember(profile) { mutableStateOf(profile.learningStyle) }
    var studyHours by remember(profile) { mutableStateOf(profile.preferredStudyHours) }

    val previewSubjects = remember(gradeLevel, branch) {
        CurriculumCatalog.subjectsFor(gradeLevel, branch)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Arayüz Teması") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme & Color Customization Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
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
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Arayüz & Renk Özelleştirme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text("Renk Paleti Seç", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemePalette.entries.forEach { pal ->
                                val isSelected = currentPalette == pal
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .clickable { onSelectPalette(pal) }
                                        .testTag("theme_palette_${pal.key}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(pal.previewPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = "Seçili",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = pal.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Text("Görünüm Modu", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DarkModePref.entries.forEach { mode ->
                                FilterChip(
                                    selected = currentDarkMode == mode,
                                    onClick = { onSelectDarkMode(mode) },
                                    label = { Text(mode.displayName) },
                                    modifier = Modifier.testTag("dark_mode_${mode.key}")
                                )
                            }
                        }
                    }
                }
            }

            // Student Profile & Preferences Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
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
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Öğrenci Profili & Akademik Hedefler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("Ad") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Soyad") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text("Sınıf Seviyesi", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CurriculumCatalog.gradeLevels.forEach { g ->
                                FilterChip(
                                    selected = gradeLevel == g,
                                    onClick = {
                                        gradeLevel = g
                                        val bList = CurriculumCatalog.branchesForGrade(g)
                                        if (branch !in bList) branch = bList.first()
                                    },
                                    label = { Text(g) }
                                )
                            }
                        }

                        Text("Bölüm / Alan", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            branches.forEach { b ->
                                FilterChip(
                                    selected = branch == b,
                                    onClick = { branch = b },
                                    label = { Text(b) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = targetScore,
                                onValueChange = { targetScore = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Hedef Puan") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dailyGoal,
                                onValueChange = { dailyGoal = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Günlük Soru Hedefi") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = dailyMinutes,
                            onValueChange = { dailyMinutes = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Günlük Çalışma Süresi (dk)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Öğrenme Stili", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CurriculumCatalog.learningStyles.forEach { st ->
                                FilterChip(
                                    selected = learningStyle == st,
                                    onClick = { learningStyle = st },
                                    label = { Text(st) }
                                )
                            }
                        }

                        Text("Çalışma Saatleri", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CurriculumCatalog.studyHourWindows.forEach { w ->
                                FilterChip(
                                    selected = studyHours == w,
                                    onClick = { studyHours = w },
                                    label = { Text(w) }
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "MEB Müfredatı Ders Listesi (${previewSubjects.size} Ders)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = previewSubjects.joinToString(" • ") { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val updated = profile.copy(
                                    userId = userId,
                                    firstName = firstName.ifBlank { "Öğrenci" },
                                    lastName = lastName.ifBlank { "Aday" },
                                    gradeLevel = gradeLevel,
                                    branch = branch,
                                    targetExam = CurriculumCatalog.defaultTargetExamForGrade(gradeLevel),
                                    targetScore = targetScore.toDoubleOrNull()?.coerceIn(0.0, 600.0) ?: 450.0,
                                    dailyQuestionGoal = dailyGoal.toLongOrNull()?.coerceIn(1L, 1000L) ?: 100L,
                                    dailyStudyMinutes = dailyMinutes.toLongOrNull()?.coerceIn(15L, 720L) ?: 180L,
                                    learningStyle = learningStyle,
                                    preferredStudyHours = studyHours,
                                    themePalette = currentPalette.key,
                                    darkModePref = currentDarkMode.key
                                )
                                onSaveProfile(updated)
                            },
                            enabled = !isSaving,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_profile_button")
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Profili & Dersleri Güncelle")
                            }
                        }
                    }
                }
            }

            // Session Persistence / Remember Login Card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Oturumu ve Giriş Bilgilerini Hatırla",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (accountEmail.isNotBlank()) {
                                    "Kayıtlı hesap: $accountEmail • Uygulama açılışında oturumun korunur."
                                } else {
                                    "Uygulama her açıldığında otomatik oturum açık kalsın."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = rememberLogin,
                            onCheckedChange = onToggleRememberLogin,
                            modifier = Modifier.testTag("profile_remember_login_switch")
                        )
                    }
                }
            }

            // Sign Out
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Hesaptan Çıkış Yap")
                }
            }
        }
    }
}
