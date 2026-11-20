package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BookEntry
import com.example.data.model.BookStatus
import com.example.data.model.QuoteCard
import com.example.data.repository.LunarRepository
import com.example.ui.state.UiState
import com.example.ui.state.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: LunarRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedStatus = MutableStateFlow<BookStatus?>(null)
    val selectedStatus: StateFlow<BookStatus?> = _selectedStatus.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val rawBooks: StateFlow<UiState<List<BookEntry>>> =
        repository.observeBooks(currentUserId)
            .asUiState(TAG, "Unable to sync reading log")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val booksState: StateFlow<UiState<List<BookEntry>>> =
        combine(rawBooks, _selectedStatus) { state, filter ->
            when (state) {
                is UiState.Success -> {
                    if (filter == null) state
                    else UiState.Success(state.data.filter { it.resolvedStatus() == filter })
                }
                else -> state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val quotesState: StateFlow<UiState<List<QuoteCard>>> =
        repository.observeQuotes(currentUserId)
            .asUiState(TAG, "Unable to sync saved quotes")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    fun selectStatusFilter(status: BookStatus?) {
        _selectedStatus.value = status
    }

    fun addBook(
        title: String,
        author: String,
        status: BookStatus,
        currentPage: Int,
        totalPages: Int,
        bookmarkLabel: String
    ) {
        if (title.isBlank() || author.isBlank()) {
            _feedbackMessage.value = "Book title and author are required."
            return
        }
        viewModelScope.launch {
            repository.createBook(
                userId = currentUserId,
                title = title,
                author = author,
                status = status,
                currentPage = currentPage,
                totalPages = totalPages.coerceAtLeast(1),
                bookmarkLabel = bookmarkLabel
            ).fold(
                onSuccess = { _feedbackMessage.value = "Added \"$title\" to your reading shelf." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Could not add book." }
            )
        }
    }

    fun stepBookPage(book: BookEntry, delta: Int) {
        val total = book.totalPages.toInt().coerceAtLeast(1)
        val nextPage = (book.currentPage.toInt() + delta).coerceIn(0, total)
        val nextStatus = when {
            nextPage >= total -> BookStatus.Finished
            nextPage > 0 && book.resolvedStatus() == BookStatus.Shelf -> BookStatus.Reading
            else -> book.resolvedStatus()
        }
        viewModelScope.launch {
            repository.updateBookProgress(
                userId = currentUserId,
                bookId = book.id,
                currentPage = nextPage,
                totalPages = total,
                status = nextStatus,
                bookmarkLabel = book.bookmarkLabel
            ).onFailure { err ->
                _feedbackMessage.value = err.message ?: "Failed to update page."
            }
        }
    }

    fun cycleBookStatus(book: BookEntry) {
        val nextStatus = when (book.resolvedStatus()) {
            BookStatus.Shelf -> BookStatus.Reading
            BookStatus.Reading -> BookStatus.Finished
            BookStatus.Finished -> BookStatus.Shelf
        }
        viewModelScope.launch {
            repository.updateBookProgress(
                userId = currentUserId,
                bookId = book.id,
                currentPage = book.currentPage.toInt(),
                totalPages = book.totalPages.toInt().coerceAtLeast(1),
                status = nextStatus,
                bookmarkLabel = book.bookmarkLabel
            ).onFailure { err ->
                _feedbackMessage.value = err.message ?: "Failed to update bookmark status."
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            repository.deleteBook(currentUserId, bookId).fold(
                onSuccess = { _feedbackMessage.value = "Volume removed from reading log." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Could not delete book." }
            )
        }
    }

    fun addQuote(
        bookId: String,
        bookTitle: String,
        quoteText: String,
        pageNumber: Int,
        reflectionNote: String
    ) {
        if (bookTitle.isBlank() || quoteText.isBlank()) {
            _feedbackMessage.value = "Book title and excerpt text are required."
            return
        }
        viewModelScope.launch {
            repository.createQuote(
                userId = currentUserId,
                bookId = bookId,
                bookTitle = bookTitle,
                quoteText = quoteText,
                pageNumber = pageNumber,
                reflectionNote = reflectionNote
            ).fold(
                onSuccess = { _feedbackMessage.value = "Quote card tucked into library." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Failed to save quote." }
            )
        }
    }

    fun deleteQuote(quoteId: String) {
        viewModelScope.launch {
            repository.deleteQuote(currentUserId, quoteId).fold(
                onSuccess = { _feedbackMessage.value = "Quote removed." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Failed to delete quote." }
            )
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private companion object {
        const val TAG = "LibraryVM"
    }
}
