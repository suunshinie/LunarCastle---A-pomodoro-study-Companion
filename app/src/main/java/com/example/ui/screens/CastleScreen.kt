package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntry
import com.example.data.model.BookStatus
import com.example.data.model.DiamondMilestone
import com.example.data.model.FocusSession
import com.example.data.model.StudyNote
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarMoonCrest
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.components.LunarSecondaryButton
import com.example.ui.state.UiState
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.FocusViewModel
import com.example.ui.viewmodel.JournalViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.TablesViewModel

@Composable
fun CastleScreen(
    userDisplayName: String,
    focusViewModel: FocusViewModel,
    tablesViewModel: TablesViewModel,
    libraryViewModel: LibraryViewModel,
    journalViewModel: JournalViewModel,
    onNavigateToFocus: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToJournal: () -> Unit
) {
    val lunar = LocalLunarColors.current
    val timerState by focusViewModel.timerState.collectAsStateWithLifecycle()
    val sessionsState by focusViewModel.sessionsState.collectAsStateWithLifecycle()
    val milestonesState by tablesViewModel.filteredMilestones.collectAsStateWithLifecycle()
    val booksState by libraryViewModel.booksState.collectAsStateWithLifecycle()
    val notesState by journalViewModel.filteredNotes.collectAsStateWithLifecycle()

    val sessions: List<FocusSession> = (sessionsState as? UiState.Success)?.data ?: emptyList()
    val milestones: List<DiamondMilestone> = (milestonesState as? UiState.Success)?.data ?: emptyList()
    val books: List<BookEntry> = (booksState as? UiState.Success)?.data ?: emptyList()
    val notes: List<StudyNote> = (notesState as? UiState.Success)?.data ?: emptyList()

    val totalFocusMinutes = sessions.sumOf { it.durationMinutes }
    val pendingMilestones = milestones.filter { !it.completed }
    val activeBook = books.firstOrNull { it.resolvedStatus() == BookStatus.Reading } ?: books.firstOrNull()
    val latestNote = notes.firstOrNull()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Castle welcome banner
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LunarMoonCrest(size = 56.dp, animateBounce = true)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LUNAR CASTLE · HOME",
                                style = LunarTextStyles.StatusMono,
                                color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Welcome back, ${userDisplayName.substringBefore(' ').ifBlank { "Scholar" }}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = lunar.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Candlelit focus, study tables & quiet pages await.",
                                style = LunarTextStyles.TaglineItalic,
                                color = lunar.secondaryText
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBadgePill(
                            label = "${totalFocusMinutes}m focused",
                            modifier = Modifier.weight(1f)
                        )
                        StatBadgePill(
                            label = "${sessions.size} teas brewed",
                            modifier = Modifier.weight(1f)
                        )
                        StatBadgePill(
                            label = "${pendingMilestones.size} tasks open",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Moon Room quick launch card
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = "Moon Room",
                            tint = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "MOON ROOM · FOCUS",
                            style = LunarTextStyles.StatusMono,
                            color = lunar.mutedMetaText
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = timerState.intentionLabel.ifBlank { "Moonlit Study Session" },
                        style = MaterialTheme.typography.titleLarge,
                        color = lunar.primaryText
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalCafe,
                            contentDescription = null,
                            tint = lunar.lavender,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${timerState.selectedPreset.label} · ${timerState.selectedTea.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = lunar.secondaryText
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    LunarPrimaryButton(
                        text = if (timerState.isRunning) "Return to Active Timer" else "Enter Moon Room",
                        onClick = onNavigateToFocus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("castle_open_focus_button")
                    )
                }
            }

            item { ConstellationDivider() }

            // Tables (Study Desks & Milestones) preview card
            item {
                LunarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("castle_tables_card"),
                    onClick = onNavigateToTables,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TableRestaurant,
                                contentDescription = "Study Tables",
                                tint = lunar.lavender,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tables & Milestones",
                                style = MaterialTheme.typography.titleLarge,
                                color = lunar.primaryText
                            )
                        }
                        Text(
                            text = "${pendingMilestones.size} OPEN",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = pendingMilestones.firstOrNull()?.title
                            ?: "No open diamond milestones. Visit Tables to choose a desk and plan your session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = lunar.secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    LunarSecondaryButton(
                        text = "Open Tables",
                        onClick = onNavigateToTables,
                        modifier = Modifier.testTag("castle_open_tables_button")
                    )
                }
            }

            // Library & Secret Garden quick grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LunarCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("castle_library_card"),
                        onClick = onNavigateToLibrary,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = "Library",
                            tint = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.titleLarge,
                            color = lunar.primaryText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = activeBook?.let { "${it.title} (p.${it.currentPage})" }
                                ?: "${books.size} books archived",
                            style = MaterialTheme.typography.bodySmall,
                            color = lunar.secondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    LunarCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("castle_journal_card"),
                        onClick = onNavigateToJournal,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = "The Secret Garden (Journal)",
                            tint = lunar.lavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Journal",
                            style = MaterialTheme.typography.titleLarge,
                            color = lunar.primaryText
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = latestNote?.title ?: "The Secret Garden (${notes.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = lunar.secondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadgePill(
    label: String,
    modifier: Modifier = Modifier
) {
    val lunar = LocalLunarColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = lunar.canvas,
        border = BorderStroke(1.dp, lunar.cardBorder)
    ) {
        Text(
            text = label,
            style = LunarTextStyles.StatusMono,
            color = if (lunar.isDark) lunar.lightGold else lunar.midnight,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
