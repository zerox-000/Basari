package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    userId: String,
    initialFirstName: String,
    initialLastName: String,
    currentPalette: ThemePalette,
    currentDarkMode: DarkModePref,
    isSaving: Boolean,
    onSaveProfile: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0: Sınıf, 1: Bölüm, 2: Profil & MEB Ders Listesi
    var selectedGrade by remember { mutableStateOf("12. Sınıf") }
    val availableBranches = remember(selectedGrade) { CurriculumCatalog.branchesForGrade(selectedGrade) }
    var selectedBranch by remember(selectedGrade) { mutableStateOf(availableBranches.first()) }

    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var targetScoreText by remember { mutableStateOf("450") }
    var dailyGoalText by remember { mutableStateOf("100") }
    var dailyMinutesText by remember { mutableStateOf("180") }
    var learningStyle by remember { mutableStateOf(CurriculumCatalog.learningStyles.first()) }
    var studyHours by remember { mutableStateOf(CurriculumCatalog.studyHourWindows[2]) }

    val generatedSubjects = remember(selectedGrade, selectedBranch) {
        CurriculumCatalog.subjectsFor(selectedGrade, selectedBranch)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adım ${step + 1} / 3",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (step) {
                            0 -> "Sınıf Seviyesi"
                            1 -> "Bölüm / Alan Seçimi"
                            else -> "Profil & MEB Dersleri"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { (step + 1) / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedContent(targetState = step, label = "onboardingStep") { currentStep ->
                    when (currentStep) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "Hangi sınıf seviyesindesin?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Seçtiğin sınıfa göre MEB müfredat derslerin ve soru bankan otomatik hazırlanacak.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    CurriculumCatalog.gradeLevels.forEach { grade ->
                                        val selected = selectedGrade == grade
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedGrade = grade
                                                    selectedBranch = CurriculumCatalog.branchesForGrade(grade).first()
                                                }
                                                .testTag("grade_option_$grade")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.School,
                                                        contentDescription = grade,
                                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = grade,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                }
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.CheckCircle,
                                                        contentDescription = "Seçili",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "$selectedGrade için bölümünü seç",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bölümüne göre MEB ağırlıklı derslerin, yazılı takip alanların ve deneme şablonların oluşturulur.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    availableBranches.forEach { branch ->
                                        val selected = selectedBranch == branch
                                        val previewCount = CurriculumCatalog.subjectsFor(selectedGrade, branch).size
                                        Surface(
                                            shape = RoundedCornerShape(18.dp),
                                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedBranch = branch }
                                                .testTag("branch_option_$branch")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = branch,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "$previewCount MEB dersi otomatik tanımlanır",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (selected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.CheckCircle,
                                                        contentDescription = "Seçili",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = "Profilin ve Hedeflerin",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = firstName,
                                        onValueChange = { firstName = it },
                                        label = { Text("Ad") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("onboarding_first_name")
                                    )
                                    OutlinedTextField(
                                        value = lastName,
                                        onValueChange = { lastName = it },
                                        label = { Text("Soyad") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("onboarding_last_name")
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = targetScoreText,
                                        onValueChange = { targetScoreText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                        label = { Text("Hedef Puan") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = dailyGoalText,
                                        onValueChange = { dailyGoalText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Günlük Soru") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = dailyMinutesText,
                                        onValueChange = { dailyMinutesText = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Günlük Dk") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    text = "Öğrenme Stilin",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CurriculumCatalog.learningStyles.forEach { style ->
                                        FilterChip(
                                            selected = learningStyle == style,
                                            onClick = { learningStyle = style },
                                            label = { Text(style) }
                                        )
                                    }
                                }

                                Text(
                                    text = "Tercih Ettiğin Çalışma Saatleri",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CurriculumCatalog.studyHourWindows.forEach { win ->
                                        FilterChip(
                                            selected = studyHours == win,
                                            onClick = { studyHours = win },
                                            label = { Text(win) }
                                        )
                                    }
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MenuBook,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Otomatik MEB Ders Listen (${generatedSubjects.size} Ders)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            generatedSubjects.forEach { sub ->
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = RoundedCornerShape(10.dp),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                                ) {
                                                    Text(
                                                        text = "${sub.name} (${sub.weeklyHours}s)",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
            }

            // Navigation Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("onboarding_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Geri")
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Geri")
                    }
                }
                Button(
                    onClick = {
                        if (step < 2) {
                            step += 1
                        } else {
                            val profile = UserProfile(
                                userId = userId,
                                firstName = firstName.ifBlank { "Öğrenci" },
                                lastName = lastName.ifBlank { "Aday" },
                                gradeLevel = selectedGrade,
                                branch = selectedBranch,
                                targetExam = CurriculumCatalog.defaultTargetExamForGrade(selectedGrade),
                                targetScore = targetScoreText.toDoubleOrNull()?.coerceIn(0.0, 600.0) ?: 450.0,
                                dailyQuestionGoal = dailyGoalText.toLongOrNull()?.coerceIn(1L, 1000L) ?: 100L,
                                learningStyle = learningStyle,
                                preferredStudyHours = studyHours,
                                dailyStudyMinutes = dailyMinutesText.toLongOrNull()?.coerceIn(15L, 720L) ?: 180L,
                                themePalette = currentPalette.key,
                                darkModePref = currentDarkMode.key
                            )
                            onSaveProfile(profile)
                        }
                    },
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag(if (step < 2) "onboarding_next_button" else "onboarding_finish_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (step < 2) "Devam Et" else "Başla")
                        Spacer(modifier = Modifier.size(6.dp))
                        Icon(
                            imageVector = if (step < 2) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.CheckCircle,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
