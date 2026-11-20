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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookEntry
import com.example.data.model.BookStatus
import com.example.data.model.QuoteCard
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.HangingBookmarkTag
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.components.LunarSecondaryButton
import com.example.ui.components.lunarFieldColors
import com.example.ui.state.UiState
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val lunar = LocalLunarColors.current
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val booksState by viewModel.booksState.collectAsStateWithLifecycle()
    val quotesState by viewModel.quotesState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    var showAddBookDialog by remember { mutableStateOf(false) }
    var quoteTargetBook by remember { mutableStateOf<BookEntry?>(null) }
    var showAddQuoteDialog by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LUNAR CASTLE · LIBRARY",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Reading Log & Quotes",
                            style = MaterialTheme.typography.headlineMedium,
                            color = lunar.primaryText
                        )
                    }
                    LunarPrimaryButton(
                        text = "Add Book",
                        onClick = { showAddBookDialog = true },
                        modifier = Modifier.testTag("add_book_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add book",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Status Filter Pills
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LunarChip(
                        label = "All",
                        selected = selectedStatus == null,
                        onClick = { viewModel.selectStatusFilter(null) },
                        modifier = Modifier.testTag("book_filter_all")
                    )
                    BookStatus.entries.forEach { status ->
                        LunarChip(
                            label = status.label,
                            selected = selectedStatus == status,
                            accentGold = status == BookStatus.Reading,
                            onClick = { viewModel.selectStatusFilter(status) },
                            modifier = Modifier.testTag("book_filter_${status.name.lowercase()}")
                        )
                    }
                }
            }

            // Bookshelf
            when (val state = booksState) {
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
                                    text = "Your castle shelves are awaiting their first book.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = lunar.primaryText
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tap Add Book to track reading progress with hanging gold bookmark tags and save literary quotes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    } else {
                        items(state.data, key = { it.id }) { book ->
                            BookBookmarkCard(
                                book = book,
                                onStepBack = { viewModel.stepBookPage(book, -10) },
                                onStepForward = { viewModel.stepBookPage(book, 10) },
                                onCycleStatus = { viewModel.cycleBookStatus(book) },
                                onAddQuote = {
                                    quoteTargetBook = book
                                    showAddQuoteDialog = true
                                },
                                onDelete = { viewModel.deleteBook(book.id) }
                            )
                        }
                    }
                }
            }

            item { ConstellationDivider() }

            // Quotes Archive Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SAVED MARGINALIA",
                            style = LunarTextStyles.StatusMono,
                            color = lunar.mutedMetaText
                        )
                        Text(
                            text = "Collected Quote Cards",
                            style = MaterialTheme.typography.headlineSmall,
                            color = lunar.primaryText
                        )
                    }
                    LunarSecondaryButton(
                        text = "Save Quote",
                        onClick = {
                            quoteTargetBook = null
                            showAddQuoteDialog = true
                        },
                        modifier = Modifier.testTag("add_quote_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FormatQuote,
                                contentDescription = "Save quote",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            when (val qState = quotesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = lunar.gold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        LunarCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = qState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (qState.data.isEmpty()) {
                        item {
                            LunarCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                Text(
                                    text = "Save memorable lines from your reading with page footnotes and personal reflections.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    } else {
                        items(qState.data, key = { it.id }) { quote ->
                            QuoteArchiveCard(
                                quote = quote,
                                onDelete = { viewModel.deleteQuote(quote.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onSave = { title, author, status, current, total, bookmark ->
                viewModel.addBook(title, author, status, current, total, bookmark)
                showAddBookDialog = false
            }
        )
    }

    if (showAddQuoteDialog) {
        AddQuoteDialog(
            initialBook = quoteTargetBook,
            onDismiss = { showAddQuoteDialog = false },
            onSave = { bookId, bookTitle, quoteText, page, reflection ->
                viewModel.addQuote(bookId, bookTitle, quoteText, page, reflection)
                showAddQuoteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookBookmarkCard(
    book: BookEntry,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onCycleStatus: () -> Unit,
    onAddQuote: () -> Unit,
    onDelete: () -> Unit
) {
    val lunar = LocalLunarColors.current
    val total = book.totalPages.coerceAtLeast(1L)
    val progress = (book.currentPage.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val status = book.resolvedStatus()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}")
    ) {
        LunarCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 90.dp)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = lunar.primaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = lunar.secondaryText
                    )
                }
            }

            if (book.bookmarkLabel.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Bookmark: ${book.bookmarkLabel}",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.lavender
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "p. ${book.currentPage} / $total",
                    style = LunarTextStyles.StatusMono,
                    color = if (lunar.isDark) lunar.lightGold else lunar.midnight
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = lunar.gold,
                trackColor = lunar.cardBorder
            )

            Spacer(Modifier.height(14.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LunarChip(
                    label = "-10p",
                    selected = false,
                    onClick = onStepBack,
                    modifier = Modifier.testTag("book_minus_${book.id}")
                )
                LunarChip(
                    label = "+10p",
                    selected = true,
                    accentGold = true,
                    onClick = onStepForward,
                    modifier = Modifier.testTag("book_plus_${book.id}")
                )
                LunarChip(
                    label = "Quote",
                    selected = false,
                    onClick = onAddQuote,
                    modifier = Modifier.testTag("book_quote_${book.id}")
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("delete_book_${book.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete book",
                        tint = lunar.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Hanging gold bookmark tag anchored over top-right border
        HangingBookmarkTag(
            label = status.label,
            onClick = onCycleStatus,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 18.dp)
                .testTag("book_bookmark_${book.id}")
        )
    }
}

@Composable
private fun QuoteArchiveCard(
    quote: QuoteCard,
    onDelete: () -> Unit
) {
    val lunar = LocalLunarColors.current
    LunarCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quote_card_${quote.id}"),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildString {
                    append(quote.bookTitle)
                    if (quote.pageNumber > 0) append(" · p. ${quote.pageNumber}")
                },
                style = LunarTextStyles.StatusMono,
                color = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_quote_${quote.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete quote",
                    tint = lunar.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "“${quote.quoteText}”",
            style = LunarTextStyles.QuoteSerif,
            color = lunar.primaryText
        )
        if (quote.reflectionNote.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = quote.reflectionNote,
                style = MaterialTheme.typography.bodySmall,
                color = lunar.secondaryText
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, BookStatus, Int, Int, String) -> Unit
) {
    val lunar = LocalLunarColors.current
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(BookStatus.Reading) }
    var currentPageText by remember { mutableStateOf("0") }
    var totalPagesText by remember { mutableStateOf("320") }
    var bookmarkLabel by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = lunar.cardSurface,
        titleContentColor = lunar.primaryText,
        textContentColor = lunar.secondaryText,
        title = { Text("Add Volume to Library", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(160) },
                    label = { Text("Book Title") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_input")
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(120) },
                    label = { Text("Author") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_author_input")
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BookStatus.entries.forEach { st ->
                        LunarChip(
                            label = st.label,
                            selected = status == st,
                            accentGold = st == BookStatus.Reading,
                            onClick = { status = st }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPageText,
                        onValueChange = { currentPageText = it.filter(Char::isDigit).take(5) },
                        label = { Text("Current Page") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = lunarFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalPagesText,
                        onValueChange = { totalPagesText = it.filter(Char::isDigit).take(5) },
                        label = { Text("Total Pages") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = lunarFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = bookmarkLabel,
                    onValueChange = { bookmarkLabel = it.take(60) },
                    label = { Text("Bookmark note (e.g. Ch. 4)") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            LunarPrimaryButton(
                text = "Shelve Book",
                onClick = {
                    val total = totalPagesText.toIntOrNull()?.coerceAtLeast(1) ?: 100
                    val current = currentPageText.toIntOrNull()?.coerceIn(0, total) ?: 0
                    onSave(title, author, status, current, total, bookmarkLabel)
                },
                enabled = title.isNotBlank() && author.isNotBlank(),
                modifier = Modifier.testTag("save_book_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = lunar.secondaryText)
            }
        }
    )
}

@Composable
private fun AddQuoteDialog(
    initialBook: BookEntry?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, String) -> Unit
) {
    val lunar = LocalLunarColors.current
    var bookTitle by remember { mutableStateOf(initialBook?.title.orEmpty()) }
    var quoteText by remember { mutableStateOf("") }
    var pageNumberText by remember { mutableStateOf(initialBook?.currentPage?.toString().orEmpty()) }
    var reflectionNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = lunar.cardSurface,
        titleContentColor = lunar.primaryText,
        textContentColor = lunar.secondaryText,
        title = { Text("Save Quote Card", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it.take(160) },
                    label = { Text("Book Title") },
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quote_book_input")
                )
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it.take(2000) },
                    label = { Text("Highlighted Excerpt") },
                    minLines = 3,
                    maxLines = 5,
                    colors = lunarFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quote_text_input")
                )
                OutlinedTextField(
                    value = pageNumberText,
                    onValueChange = { pageNumberText = it.filter(Char::isDigit).take(5) },
                    label = { Text("Page Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = lunarFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = { reflectionNote = it.take(2000) },
                    label = { Text("Margin Reflection (Optional)") },
                    minLines = 2,
                    colors = lunarFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            LunarPrimaryButton(
                text = "Save Quote",
                onClick = {
                    onSave(
                        initialBook?.id.orEmpty(),
                        bookTitle,
                        quoteText,
                        pageNumberText.toIntOrNull() ?: 0,
                        reflectionNote
                    )
                },
                enabled = bookTitle.isNotBlank() && quoteText.isNotBlank(),
                modifier = Modifier.testTag("save_quote_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = lunar.secondaryText)
            }
        }
    )
}
