package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DiamondMilestone
import com.example.data.model.StudyDesk
import com.example.data.model.StudyDeskCatalog
import com.example.data.model.TeaCatalog
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.DiamondCheckbox
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.components.LunarSecondaryButton
import com.example.ui.components.SearchScrollField
import com.example.ui.components.lunarFieldColors
import com.example.ui.state.UiState
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.TablesViewModel

@Composable
fun TablesScreen(
    viewModel: TablesViewModel,
    onSelectStudyDesk: (StudyDesk) -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val lunar = LocalLunarColors.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val milestonesState by viewModel.filteredMilestones.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    var showMilestoneDialog by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = "CASTLE HALL · TABLES",
                        style = LunarTextStyles.StatusMono,
                        color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Study Desks & Milestones",
                        style = MaterialTheme.typography.headlineMedium,
                        color = lunar.primaryText
                    )
                }
            }

            // Study Desk selection cards
            item {
                Text(
                    text = "CHOOSE A STUDY DESK",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
            }

            items(StudyDeskCatalog.desks, key = { it.id }) { desk ->
                val pairedTea = TeaCatalog.byIdOrFirst(desk.pairedTeaId)
                LunarCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_desk_${desk.id}"),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TableRestaurant,
                                contentDescription = desk.name,
                                tint = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = desk.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = lunar.primaryText
                                )
                                Text(
                                    text = desk.atmosphere,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = lunar.canvas,
                            border = BorderStroke(1.dp, lunar.cardBorder)
                        ) {
                            Text(
                                text = "${desk.recommendedMinutes}m · ${pairedTea.name}",
                                style = LunarTextStyles.StatusMono,
                                color = lunar.mutedMetaText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        LunarSecondaryButton(
                            text = "Sit at Desk",
                            onClick = { onSelectStudyDesk(desk) },
                            modifier = Modifier.testTag("sit_desk_${desk.id}"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = "Begin at ${desk.name}",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            item { ConstellationDivider() }

            // Diamond Milestone Checklist
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DESK MILESTONES",
                            style = LunarTextStyles.StatusMono,
                            color = lunar.mutedMetaText
                        )
                        Text(
                            text = "Diamond Checklist",
                            style = MaterialTheme.typography.headlineSmall,
                            color = lunar.primaryText
                        )
                    }
                    LunarPrimaryButton(
                        text = "Add Task",
                        onClick = { showMilestoneDialog = true },
                        modifier = Modifier.testTag("add_milestone_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add milestone",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            item {
                SearchScrollField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = "Filter milestones or tea pairings...",
                    testTag = "tables_search_input"
                )
            }

            when (val state = milestonesState) {
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
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                Text(
                                    text = "Your study table is clear. Add a diamond milestone to anchor your next Moon Room sprint.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    } else {
                        items(state.data, key = { it.id }) { milestone ->
                            TableMilestoneRow(
                                milestone = milestone,
                                onToggle = { viewModel.toggleMilestone(milestone) },
                                onDelete = { viewModel.deleteMilestone(milestone.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMilestoneDialog) {
        AddTableMilestoneDialog(
            onDismiss = { showMilestoneDialog = false },
            onSave = { title, tea ->
                viewModel.addMilestone(title, tea)
                showMilestoneDialog = false
            }
        )
    }
}

@Composable
private fun TableMilestoneRow(
    milestone: DiamondMilestone,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val lunar = LocalLunarColors.current
    LunarCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_row_${milestone.id}"),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiamondCheckbox(
                checked = milestone.completed,
                onCheckedChange = onToggle,
                contentDescription = if (milestone.completed) {
                    "Mark ${milestone.title} incomplete"
                } else {
                    "Mark ${milestone.title} complete"
                },
                modifier = Modifier.testTag("milestone_checkbox_${milestone.id}")
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (milestone.completed) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (milestone.completed) lunar.secondaryText.copy(alpha = 0.65f) else lunar.primaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (milestone.linkedTea.isNotBlank()) {
                    Text(
                        text = milestone.linkedTea,
                        style = LunarTextStyles.StatusMono,
                        color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_milestone_${milestone.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete milestone",
                    tint = lunar.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTableMilestoneDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val lunar = LocalLunarColors.current
    var title by remember { mutableStateOf("") }
    var linkedTea by remember { mutableStateOf(TeaCatalog.blends.first().name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = lunar.cardSurface,
        titleContentColor = lunar.primaryText,
        textContentColor = lunar.secondaryText,
        title = { Text("New Desk Milestone", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(160) },
                    label = { Text("Milestone description") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("milestone_title_input")
                )
                Text(
                    text = "TEA PAIRING",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TeaCatalog.blends.forEach { blend ->
                        LunarChip(
                            label = blend.name,
                            selected = linkedTea == blend.name,
                            accentGold = true,
                            onClick = { linkedTea = blend.name }
                        )
                    }
                }
            }
        },
        confirmButton = {
            LunarPrimaryButton(
                text = "Save",
                onClick = { onSave(title, linkedTea) },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_milestone_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = lunar.secondaryText)
            }
        }
    )
}
