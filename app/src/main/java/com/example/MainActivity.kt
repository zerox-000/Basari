package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.firebase.AcademicRepository
import com.example.data.firebase.AiFeedbackRepository
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.configureAppCheck
import com.example.data.firebase.signOut
import com.example.data.local.AuthPreferences
import com.example.data.local.AuthPreferencesRepository
import com.example.data.local.ThemePreferencesRepository
import com.example.data.model.UiState
import com.example.ui.screens.AnalyticsAndAiScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileAndThemeScreen
import com.example.ui.screens.QuestionBankAndMocksScreen
import com.example.ui.theme.BasariPusulasiTheme
import com.example.ui.viewmodel.AcademicViewModel
import com.example.ui.viewmodel.ThemeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_QUESTION_BANK = "question_bank"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_LEADERBOARD = "leaderboard"
const val ROUTE_COUNTDOWN = "countdown"

private data class MainTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val mainTabs = listOf(
    MainTabItem(ROUTE_HOME, "Ana Sayfa", Icons.Rounded.Home),
    MainTabItem(ROUTE_QUESTION_BANK, "Soru", Icons.Rounded.Quiz),
    MainTabItem(ROUTE_ANALYTICS, "Analiz", Icons.Rounded.Insights),
    MainTabItem(ROUTE_LEADERBOARD, "Sıralama", Icons.Rounded.EmojiEvents),
    MainTabItem(ROUTE_COUNTDOWN, "Sayaç", Icons.Rounded.Timer)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: App Check MUST run before setContent / ViewModels / Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                        ThemeViewModel(
                            prefsRepo = ThemePreferencesRepository(app),
                            academicRepo = AcademicRepository(db)
                        )
                    }
                }
            )
            val themePrefs by themeViewModel.themePreferences.collectAsStateWithLifecycle()

            BasariPusulasiTheme(
                palette = themePrefs.palette,
                darkModePref = themePrefs.darkModePref
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(themeViewModel = themeViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    themeViewModel: ThemeViewModel,
    auth: FirebaseAuth = Firebase.auth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authPrefsRepo = remember { AuthPreferencesRepository(context.applicationContext) }
    val authPrefs by authPrefsRepo.authPreferences.collectAsStateWithLifecycle(initialValue = AuthPreferences())
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var skipAutoSignIn by rememberSaveable { mutableStateOf(false) }
    var coldStartChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = authPrefsRepo.authPreferences.first()
        if (!stored.rememberLogin && auth.currentUser != null) {
            auth.signOut()
        }
        coldStartChecked = true
    }

    if (!coldStartChecked) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val user = currentUser

    if (user == null) {
        AuthScreen(
            authPrefs = authPrefs,
            skipAutoSignIn = skipAutoSignIn,
            onRememberLoginChange = { enabled ->
                scope.launch { authPrefsRepo.setRememberLogin(enabled) }
            },
            onClearSavedAccount = {
                scope.launch { authPrefsRepo.clearSavedAccount() }
            },
            onAuthSuccess = {
                skipAutoSignIn = false
            }
        )
    } else {
        LaunchedEffect(user.uid, user.email, user.displayName, authPrefs.rememberLogin) {
            skipAutoSignIn = false
            if (authPrefs.rememberLogin) {
                authPrefsRepo.saveSignedInAccount(
                    email = user.email,
                    displayName = user.displayName,
                    rememberLogin = true
                )
            }
        }
        key(user.uid) {
            val displayNameParts = remember(user.displayName) {
                val parts = user.displayName?.trim()?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
                val first = parts.firstOrNull() ?: "Öğrenci"
                val last = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Aday"
                first to last
            }
            AuthenticatedRoot(
                currentUserId = user.uid,
                accountEmail = user.email.orEmpty(),
                initialFirstName = displayNameParts.first,
                initialLastName = displayNameParts.second,
                rememberLogin = authPrefs.rememberLogin,
                onToggleRememberLogin = { enabled ->
                    scope.launch {
                        authPrefsRepo.setRememberLogin(enabled)
                        if (enabled) {
                            authPrefsRepo.saveSignedInAccount(
                                email = user.email,
                                displayName = user.displayName,
                                rememberLogin = true
                            )
                        }
                    }
                },
                onManualSignOut = {
                    skipAutoSignIn = true
                },
                themeViewModel = themeViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedRoot(
    currentUserId: String,
    accountEmail: String,
    initialFirstName: String,
    initialLastName: String,
    rememberLogin: Boolean,
    onToggleRememberLogin: (Boolean) -> Unit,
    onManualSignOut: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: AcademicViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                AcademicViewModel(
                    repository = AcademicRepository(db),
                    aiRepository = AiFeedbackRepository(),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val leaderboardState by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val granularity by viewModel.selectedGranularity.collectAsStateWithLifecycle()
    val subjectFilter by viewModel.selectedSubjectIdFilter.collectAsStateWithLifecycle()
    val aiState by viewModel.aiGenerationState.collectAsStateWithLifecycle()
    val studyPlanState by viewModel.studyPlanGenState.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val themePrefs by themeViewModel.themePreferences.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    when (val state = dashboardState) {
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
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        is UiState.Success -> {
            val snapshot = state.data
            val profile = snapshot.profile

            LaunchedEffect(profile?.themePalette, profile?.darkModePref) {
                if (profile != null) {
                    themeViewModel.syncFromRemoteIfPresent(profile.themePalette, profile.darkModePref)
                }
            }

            if (profile == null) {
                OnboardingScreen(
                    userId = currentUserId,
                    initialFirstName = initialFirstName,
                    initialLastName = initialLastName,
                    currentPalette = themePrefs.palette,
                    currentDarkMode = themePrefs.darkModePref,
                    isSaving = isSaving,
                    onSaveProfile = { newProfile -> viewModel.saveProfile(newProfile) }
                )
            } else {
                var selectedRoute by rememberSaveable { mutableStateOf(ROUTE_HOME) }
                var questionSubTab by rememberSaveable { mutableIntStateOf(0) }
                var analyticsSubTab by rememberSaveable { mutableIntStateOf(0) }
                var showProfileScreen by rememberSaveable { mutableStateOf(false) }

                BackHandler(enabled = !showProfileScreen && selectedRoute != ROUTE_HOME) {
                    selectedRoute = ROUTE_HOME
                }

                if (showProfileScreen) {
                    ProfileAndThemeScreen(
                        userId = currentUserId,
                        profile = profile,
                        accountEmail = accountEmail,
                        rememberLogin = rememberLogin,
                        currentPalette = themePrefs.palette,
                        currentDarkMode = themePrefs.darkModePref,
                        isSaving = isSaving,
                        onToggleRememberLogin = onToggleRememberLogin,
                        onSelectPalette = { themeViewModel.selectPalette(it) },
                        onSelectDarkMode = { themeViewModel.selectDarkMode(it) },
                        onSaveProfile = { updated -> viewModel.saveProfile(updated) },
                        onSignOut = {
                            onManualSignOut()
                            signOut(credentialManager, scope)
                        },
                        onBack = { showProfileScreen = false }
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useRail = maxWidth >= 700.dp

                        Scaffold(
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Başarı Pusulası",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = { showProfileScreen = true },
                                            modifier = Modifier.testTag("open_theme_profile_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Palette,
                                                contentDescription = "Tema & Renk Değiştir"
                                            )
                                        }
                                        IconButton(
                                            onClick = { showProfileScreen = true },
                                            modifier = Modifier.testTag("open_profile_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = "Profil Ayarları"
                                            )
                                        }
                                    }
                                )
                            },
                            bottomBar = {
                                if (!useRail) {
                                    NavigationBar {
                                        mainTabs.forEach { tab ->
                                            NavigationBarItem(
                                                selected = selectedRoute == tab.route,
                                                onClick = {
                                                    if (tab.route == ROUTE_QUESTION_BANK) questionSubTab = 0
                                                    if (tab.route == ROUTE_ANALYTICS) analyticsSubTab = 0
                                                    selectedRoute = tab.route
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = tab.icon,
                                                        contentDescription = tab.label,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = tab.label,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                modifier = Modifier.testTag("nav_${tab.route}")
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (useRail) {
                                    NavigationRail {
                                        mainTabs.forEach { tab ->
                                            NavigationRailItem(
                                                selected = selectedRoute == tab.route,
                                                onClick = {
                                                    if (tab.route == ROUTE_QUESTION_BANK) questionSubTab = 0
                                                    if (tab.route == ROUTE_ANALYTICS) analyticsSubTab = 0
                                                    selectedRoute = tab.route
                                                },
                                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                                label = { Text(tab.label) }
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    when (selectedRoute) {
                                        ROUTE_HOME -> DashboardScreen(
                                            snapshot = snapshot,
                                            onAddWrittenGrade = viewModel::addWrittenGrade,
                                            onDeleteWrittenGrade = viewModel::deleteWrittenGrade,
                                            onAddQuestionLog = viewModel::addQuestionLog,
                                            onNavigateToMocks = {
                                                questionSubTab = 1
                                                selectedRoute = ROUTE_QUESTION_BANK
                                            },
                                            onNavigateToStudyPlan = {
                                                analyticsSubTab = 1
                                                selectedRoute = ROUTE_ANALYTICS
                                            }
                                        )
                                        ROUTE_QUESTION_BANK -> QuestionBankAndMocksScreen(
                                            snapshot = snapshot,
                                            initialSubTab = questionSubTab,
                                            onAddQuestionLog = viewModel::addQuestionLog,
                                            onAddMockExam = viewModel::addMockExam,
                                            onDeleteMockExam = viewModel::deleteMockExam
                                        )
                                        ROUTE_ANALYTICS -> {
                                            val buckets = remember(snapshot, granularity, subjectFilter) {
                                                viewModel.computeChartBuckets(snapshot, granularity, subjectFilter)
                                            }
                                            val comparison = remember(snapshot, granularity, subjectFilter) {
                                                viewModel.computePeriodComparison(snapshot, granularity, subjectFilter)
                                            }
                                            AnalyticsAndAiScreen(
                                                snapshot = snapshot,
                                                initialSubTab = analyticsSubTab,
                                                granularity = granularity,
                                                selectedSubjectId = subjectFilter,
                                                chartBuckets = buckets,
                                                comparison = comparison,
                                                aiGenerationState = aiState,
                                                studyPlanGenState = studyPlanState,
                                                onGranularityChange = viewModel::setGranularity,
                                                onSubjectFilterChange = viewModel::setSubjectFilter,
                                                onRunAiAnalysis = viewModel::runAiAnalysis,
                                                onGenerateStudyPlan = viewModel::generateWeeklyStudyPlan,
                                                onToggleStudySession = viewModel::toggleStudySession,
                                                onDeleteStudyPlan = viewModel::deleteStudyPlan
                                            )
                                        }
                                        ROUTE_LEADERBOARD -> LeaderboardScreen(
                                            currentUserId = currentUserId,
                                            currentProfile = profile,
                                            leaderboardState = leaderboardState
                                        )
                                        ROUTE_COUNTDOWN -> CountdownScreen(
                                            customCountdowns = snapshot.customCountdowns,
                                            onAddCustomCountdown = viewModel::addCustomCountdown,
                                            onDeleteCustomCountdown = viewModel::deleteCustomCountdown
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
