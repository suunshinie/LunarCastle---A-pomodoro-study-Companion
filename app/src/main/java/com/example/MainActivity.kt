package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.authStateFlow
import com.example.data.firebase.configureAppCheck
import com.example.data.preferences.LunarPreferencesRepository
import com.example.data.preferences.LunarUserPreferences
import com.example.ui.components.LunarMoonCrest
import com.example.ui.screens.MainShell
import com.example.ui.screens.MakeItYoursScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CastleThemeMode
import com.example.ui.theme.LunarCastleTheme
import com.example.ui.theme.Midnight
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check before setContent or any Firebase operation
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val preferencesRepository = LunarPreferencesRepository(applicationContext)

        setContent {
            val preferences by preferencesRepository.preferencesFlow
                .collectAsStateWithLifecycle(initialValue = null)

            val resolvedPrefs = preferences ?: LunarUserPreferences()

            LaunchedEffect(resolvedPrefs.themeMode) {
                val style = if (resolvedPrefs.themeMode == CastleThemeMode.MOONLIT_CASTLE) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            LunarCastleTheme(
                themeMode = resolvedPrefs.themeMode,
                reducedMotionPreference = resolvedPrefs.reducedMotion
            ) {
                if (preferences == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Midnight),
                        contentAlignment = Alignment.Center
                    ) {
                        LunarMoonCrest(size = 88.dp, animateBounce = false)
                    }
                } else {
                    LunarCastleAppRoot(
                        preferences = resolvedPrefs,
                        preferencesRepository = preferencesRepository
                    )
                }
            }
        }
    }
}

@Composable
fun LunarCastleAppRoot(
    preferences: LunarUserPreferences,
    preferencesRepository: LunarPreferencesRepository
) {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val scope = rememberCoroutineScope()
    var showMakeItYoursOverride by remember { mutableStateOf(false) }

    val user = currentUser
    when {
        user == null -> {
            showMakeItYoursOverride = false
            WelcomeScreen(onAuthSuccess = {})
        }
        !preferences.onboardingCompleted || showMakeItYoursOverride -> {
            MakeItYoursScreen(
                currentPreferences = preferences,
                onPreviewThemeChange = { mode ->
                    scope.launch { preferencesRepository.setThemeMode(mode) }
                },
                onPreviewReducedMotionChange = { reduced ->
                    scope.launch { preferencesRepository.setReducedMotion(reduced) }
                },
                onCompleteSetup = { mode, minutes, teaId, reduced ->
                    scope.launch {
                        preferencesRepository.saveMakeItYours(
                            themeMode = mode,
                            defaultFocusMinutes = minutes,
                            defaultTeaId = teaId,
                            reducedMotion = reduced
                        )
                        showMakeItYoursOverride = false
                    }
                }
            )
        }
        else -> {
            key(user.uid) {
                MainShell(
                    currentUserId = user.uid,
                    userDisplayName = user.displayName ?: user.email ?: "Scholar",
                    preferences = preferences,
                    onSelectThemeMode = { mode ->
                        scope.launch { preferencesRepository.setThemeMode(mode) }
                    },
                    onToggleReducedMotion = { reduced ->
                        scope.launch { preferencesRepository.setReducedMotion(reduced) }
                    },
                    onSelectDefaultFocusMinutes = { mins ->
                        scope.launch { preferencesRepository.setDefaultFocusMinutes(mins) }
                    },
                    onSelectDefaultTeaId = { teaId ->
                        scope.launch { preferencesRepository.setDefaultTeaId(teaId) }
                    },
                    onOpenMakeItYours = { showMakeItYoursOverride = true }
                )
            }
        }
    }
}
