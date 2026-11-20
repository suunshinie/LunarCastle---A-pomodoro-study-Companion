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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudyNote
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.components.SearchScrollField
import com.example.ui.components.lunarFieldColors
import com.example.ui.state.UiState
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.JournalViewModel

private val JOURNAL_TAGS = listOf("All", "Study", "Reflection", "Draft", "Tea Note")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val lunar = LocalLunarColors.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val notesState by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    var showEntryDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<StudyNote?>(null) }

    LaunchedEffect(feedback) {
        if (!feedback.isNullOrBlank()) {
            onShowSnackbar(feedback!!)
            viewModel.clearFeedback()
        }
    }

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "THE SECRET GARDEN · JOURNAL",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "The Secret Garden (Journal)",
                            style = MaterialTheme.typography.headlineMedium,
                            color = lunar.primaryText
                        )
                    }
                    LunarPrimaryButton(
                        text = "New Entry",
                        onClick = {
                            editingNote = null
                            showEntryDialog = true
                        },
                        modifier = Modifier.testTag("new_journal_entry_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "New journal entry",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            item {
                Text(
                    text = "A quiet, notes-only parchment garden for essays, study notes, and late-night reflections.",
                    style = LunarTextStyles.TaglineItalic,
                    color = lunar.secondaryText
                )
            }

            item {
                SearchScrollField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = "Search journal entries or tags...",
                    testTag = "journal_search_input"
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JOURNAL_TAGS.forEach { tag ->
                        LunarChip(
                            label = tag,
                            selected = selectedTag == tag,
                            onClick = { viewModel.selectTag(tag) },
                            modifier = Modifier.testTag("journal_tag_${tag.lowercase().replace(' ', '_')}")
                        )
                    }
                }
            }

            item { ConstellationDivider() }

            when (val state = notesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = lunar.gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        LunarCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            LunarCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(20.dp)
                            ) {
                                Text(
                                    text = "No entries inscribed yet.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = lunar.primaryText
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tap New Entry to write notes, tea impressions, or study summaries in The Secret Garden.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    } else {
                        items(state.data, key = { it.id }) { note ->
                            JournalEntryCard(
                                note = note,
                                onTogglePin = { viewModel.toggleNotePinned(note) },
                                onEdit = {
                                    editingNote = note
                                    showEntryDialog = true
                                },
                                onDelete = { viewModel.deleteNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEntryDialog) {
        JournalEntryEditorDialog(
            initialNote = editingNote,
            onDismiss = { showEntryDialog = false },
            onSave = { title, body, tag, pinned ->
                viewModel.saveNote(editingNote?.id, title, body, tag, pinned)
                showEntryDialog = false
            }
        )
    }
}

@Composable
private fun JournalEntryCard(
    note: StudyNote,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val lunar = LocalLunarColors.current
    LunarCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_card_${note.id}"),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.tag.uppercase(),
                style = LunarTextStyles.StatusMono,
                color = if (lunar.isDark) lunar.gold else lunar.deepLavender
            )
            Row {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pin_journal_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.pinned) "Unpin entry" else "Pin entry",
                        tint = if (note.pinned) lunar.gold else lunar.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("edit_journal_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit entry",
                        tint = lunar.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_journal_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = lunar.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleLarge,
            color = lunar.primaryText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (note.body.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyMedium,
                color = lunar.secondaryText,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryEditorDialog(
    initialNote: StudyNote?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean) -> Unit
) {
    val lunar = LocalLunarColors.current
    var title by remember(initialNote) { mutableStateOf(initialNote?.title.orEmpty()) }
    var body by remember(initialNote) { mutableStateOf(initialNote?.body.orEmpty()) }
    var tag by remember(initialNote) { mutableStateOf(initialNote?.tag ?: "Study") }
    var pinned by remember(initialNote) { mutableStateOf(initialNote?.pinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = lunar.cardSurface,
        titleContentColor = lunar.primaryText,
        textContentColor = lunar.secondaryText,
        title = {
            Text(
                text = if (initialNote == null) "Inscribe in The Secret Garden" else "Edit Journal Entry",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(140) },
                    label = { Text("Entry Heading") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_title_input")
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it.take(10000) },
                    label = { Text("Thoughts, summary, or reflection") },
                    minLines = 4,
                    maxLines = 8,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_body_input")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JOURNAL_TAGS.filter { it != "All" }.forEach { option ->
                        LunarChip(
                            label = option,
                            selected = tag == option,
                            onClick = { tag = option }
                        )
                    }
                    LunarChip(
                        label = if (pinned) "Pinned" else "Pin Entry",
                        selected = pinned,
                        accentGold = true,
                        onClick = { pinned = !pinned }
                    )
                }
            }
        },
        confirmButton = {
            LunarPrimaryButton(
                text = "Save",
                onClick = { onSave(title, body, tag, pinned) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_journal_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = lunar.secondaryText)
            }
        }
    )
}
