package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.auth.signOut
import com.example.data.preferences.LunarUserPreferences
import com.example.data.repository.LunarRepository
import com.example.ui.components.CelestialBackdrop
import com.example.ui.components.LunarMoonCrest
import com.example.ui.theme.CastleThemeMode
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.JournalViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.TablesViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object LunarRoutes {
    const val CASTLE = "castle"
    const val TABLES = "tables"
    const val FOCUS = "focus"
    const val LIBRARY = "library"
    const val JOURNAL = "journal"
    const val SETTINGS = "settings"
}

private enum class CastleDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    CASTLE(
        route = LunarRoutes.CASTLE,
        label = "Castle",
        icon = Icons.Outlined.Castle,
        testTag = "nav_castle"
    ),
    TABLES(
        route = LunarRoutes.TABLES,
        label = "Tables",
        icon = Icons.Outlined.TableRestaurant,
        testTag = "nav_tables"
    ),
    FOCUS(
        route = LunarRoutes.FOCUS,
        label = "Focus",
        icon = Icons.Outlined.Bedtime,
        testTag = "nav_focus"
    ),
    LIBRARY(
        route = LunarRoutes.LIBRARY,
        label = "Library",
        icon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_library"
    ),
    JOURNAL(
        route = LunarRoutes.JOURNAL,
        label = "Journal",
        icon = Icons.Outlined.EditNote,
        testTag = "nav_journal"
    )
}

@Composable
fun MainShell(
    currentUserId: String,
    userDisplayName: String,
    preferences: LunarUserPreferences,
    onSelectThemeMode: (CastleThemeMode) -> Unit,
    onToggleReducedMotion: (Boolean) -> Unit,
    onSelectDefaultFocusMinutes: (Int) -> Unit,
    onSelectDefaultTeaId: (String) -> Unit,
    onOpenMakeItYours: () -> Unit
) {
    val lunar = LocalLunarColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: LunarRoutes.CASTLE
    val isSettingsRoute = currentRoute == LunarRoutes.SETTINGS

    val focusViewModel: FocusViewModel = viewModel(
        key = "focus_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                FocusViewModel(
                    repository = LunarRepository(db),
                    currentUserId = currentUserId,
                    initialFocusMinutes = preferences.defaultFocusMinutes,
                    initialTeaId = preferences.defaultTeaId,
                    initialIntention = preferences.studyDeskIntention
                )
            }
        }
    )

    val tablesViewModel: TablesViewModel = viewModel(
        key = "tables_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                TablesViewModel(LunarRepository(db), currentUserId)
            }
        }
    )

    val libraryViewModel: LibraryViewModel = viewModel(
        key = "library_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                LibraryViewModel(LunarRepository(db), currentUserId)
            }
        }
    )

    val journalViewModel: JournalViewModel = viewModel(
        key = "journal_$currentUserId",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val db = FirebaseFirestore.getInstance(app.getString(R.string.firestore_database_id))
                JournalViewModel(LunarRepository(db), currentUserId)
            }
        }
    )

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lunar.canvas)
    ) {
        val useNavRail = maxWidth >= 600.dp

        CelestialBackdrop(showCastleSilhouette = false)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (!isSettingsRoute) {
                    Surface(
                        color = lunar.canvasDeep.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, lunar.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LunarMoonCrest(
                                    size = 38.dp,
                                    animateBounce = false,
                                    contentDescription = "Lunar Castle crest"
                                )
                                Column {
                                    Text(
                                        text = "Lunar Castle",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = lunar.primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = preferences.themeMode.title.uppercase(),
                                        style = LunarTextStyles.StatusMono,
                                        color = lunar.mutedMetaText,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val nextMode = if (preferences.themeMode == CastleThemeMode.MOONLIT_CASTLE) {
                                            CastleThemeMode.DAYLIGHT_LIBRARY
                                        } else {
                                            CastleThemeMode.MOONLIT_CASTLE
                                        }
                                        onSelectThemeMode(nextMode)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("header_theme_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (preferences.themeMode == CastleThemeMode.MOONLIT_CASTLE) {
                                            Icons.Outlined.LightMode
                                        } else {
                                            Icons.Outlined.DarkMode
                                        },
                                        contentDescription = "Switch theme",
                                        tint = if (lunar.isDark) lunar.gold else lunar.deepLavender
                                    )
                                }
                                IconButton(
                                    onClick = { navController.navigate(LunarRoutes.SETTINGS) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("header_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Settings",
                                        tint = lunar.secondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!useNavRail && !isSettingsRoute) {
                    NavigationBar(
                        containerColor = lunar.canvasDeep.copy(alpha = 0.96f),
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        CastleDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (lunar.isDark) lunar.lightGold else lunar.midnight,
                                    selectedTextColor = if (lunar.isDark) lunar.lightGold else lunar.midnight,
                                    indicatorColor = lunar.goldWash,
                                    unselectedIconColor = lunar.mutedMetaText,
                                    unselectedTextColor = lunar.mutedMetaText
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = lunar.cardSurface,
                        contentColor = lunar.primaryText,
                        actionColor = lunar.gold
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail && !isSettingsRoute) {
                    NavigationRail(
                        containerColor = lunar.canvasDeep.copy(alpha = 0.96f),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.weight(0.2f))
                        CastleDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = if (lunar.isDark) lunar.lightGold else lunar.midnight,
                                    selectedTextColor = if (lunar.isDark) lunar.lightGold else lunar.midnight,
                                    indicatorColor = lunar.goldWash,
                                    unselectedIconColor = lunar.mutedMetaText,
                                    unselectedTextColor = lunar.mutedMetaText
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = LunarRoutes.CASTLE,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    composable(LunarRoutes.CASTLE) {
                        CastleScreen(
                            userDisplayName = userDisplayName,
                            focusViewModel = focusViewModel,
                            tablesViewModel = tablesViewModel,
                            libraryViewModel = libraryViewModel,
                            journalViewModel = journalViewModel,
                            onNavigateToFocus = { navigateToTab(LunarRoutes.FOCUS) },
                            onNavigateToTables = { navigateToTab(LunarRoutes.TABLES) },
                            onNavigateToLibrary = { navigateToTab(LunarRoutes.LIBRARY) },
                            onNavigateToJournal = { navigateToTab(LunarRoutes.JOURNAL) }
                        )
                    }
                    composable(LunarRoutes.TABLES) {
                        TablesScreen(
                            viewModel = tablesViewModel,
                            onSelectStudyDesk = { desk ->
                                focusViewModel.applyDeskSetup(desk)
                                navigateToTab(LunarRoutes.FOCUS)
                            },
                            onShowSnackbar = ::showMessage
                        )
                    }
                    composable(LunarRoutes.FOCUS) {
                        FocusScreen(
                            viewModel = focusViewModel,
                            onShowSnackbar = ::showMessage
                        )
                    }
                    composable(LunarRoutes.LIBRARY) {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onShowSnackbar = ::showMessage
                        )
                    }
                    composable(LunarRoutes.JOURNAL) {
                        JournalScreen(
                            viewModel = journalViewModel,
                            onShowSnackbar = ::showMessage
                        )
                    }
                    composable(LunarRoutes.SETTINGS) {
                        SettingsScreen(
                            userDisplayName = userDisplayName,
                            preferences = preferences,
                            onSelectThemeMode = onSelectThemeMode,
                            onToggleReducedMotion = onToggleReducedMotion,
                            onSelectDefaultFocusMinutes = onSelectDefaultFocusMinutes,
                            onSelectDefaultTeaId = onSelectDefaultTeaId,
                            onOpenMakeItYours = {
                                navController.popBackStack()
                                onOpenMakeItYours()
                            },
                            onSignOutConfirmed = {
                                signOut(
                                    credentialManager = credentialManager,
                                    scope = scope
                                )
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
