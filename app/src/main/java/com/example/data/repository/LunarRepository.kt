package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.BookEntry
import com.example.data.model.BookStatus
import com.example.data.model.DiamondMilestone
import com.example.data.model.FocusSession
import com.example.data.model.QuoteCard
import com.example.data.model.StudyNote
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LunarRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireVerifiedUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please sign in with Google to sync Lunar Castle.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in account changed; refusing cross-user access." }
        }
        return uid
    }

    private fun <T> observeUserSubcollection(
        userId: String,
        subcollection: String,
        clazz: Class<T>,
        sorter: (List<T>) -> List<T>
    ): Flow<List<T>> = flow {
        val uid = requireVerifiedUserId(userId)
        val path = "users/$uid/$subcollection"
        emitAll(
            db.collection("users").document(uid).collection(subcollection)
                .snapshots()
                .map { snapshot ->
                    val list = snapshot.toObjects(
                        clazz,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                    sorter(list)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeFocusSessions(userId: String): Flow<List<FocusSession>> =
        observeUserSubcollection(userId, "focusSessions", FocusSession::class.java) { list ->
            list.sortedByDescending { it.completedAt ?: Timestamp(0, 0) }
        }

    fun observeNotes(userId: String): Flow<List<StudyNote>> =
        observeUserSubcollection(userId, "notes", StudyNote::class.java) { list ->
            list.sortedWith(
                compareByDescending<StudyNote> { it.pinned }
                    .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            )
        }

    fun observeMilestones(userId: String): Flow<List<DiamondMilestone>> =
        observeUserSubcollection(userId, "milestones", DiamondMilestone::class.java) { list ->
            list.sortedWith(
                compareBy<DiamondMilestone> { it.completed }
                    .thenByDescending { it.createdAt ?: Timestamp(0, 0) }
            )
        }

    fun observeBooks(userId: String): Flow<List<BookEntry>> =
        observeUserSubcollection(userId, "books", BookEntry::class.java) { list ->
            list.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
        }

    fun observeQuotes(userId: String): Flow<List<QuoteCard>> =
        observeUserSubcollection(userId, "quotes", QuoteCard::class.java) { list ->
            list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }

    suspend fun logFocusSession(
        userId: String,
        label: String,
        durationMinutes: Int,
        teaName: String,
        teaSteepMinutes: Int,
        soundscapes: List<String>
    ): Result<Unit> {
        var docPath = "users/$userId/focusSessions"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanLabel = label.trim().ifBlank { "Moonlit Study" }.take(120)
            val cleanTea = teaName.trim().ifBlank { "Herbal Steep" }.take(60)
            val clampedDuration = durationMinutes.coerceIn(1, 180)
            val clampedSteep = teaSteepMinutes.coerceIn(0, 30)
            val cleanSounds = soundscapes.take(6)

            val docRef = db.collection("users").document(uid).collection("focusSessions").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "label" to cleanLabel,
                "durationMinutes" to clampedDuration,
                "teaName" to cleanTea,
                "teaSteepMinutes" to clampedSteep,
                "soundscapes" to cleanSounds,
                "completedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteFocusSession(userId: String, sessionId: String): Result<Unit> {
        val docPath = "users/$userId/focusSessions/$sessionId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(sessionId.isNotBlank()) { "Session ID must not be blank." }
            db.collection("users").document(uid).collection("focusSessions").document(sessionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createNote(
        userId: String,
        title: String,
        body: String,
        tag: String,
        pinned: Boolean = false
    ): Result<Unit> {
        var docPath = "users/$userId/notes"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanTitle = title.trim().take(140)
            require(cleanTitle.isNotEmpty()) { "Note title cannot be empty." }
            val cleanBody = body.trim().take(10000)
            val cleanTag = tag.trim().ifBlank { "Study" }.take(40)

            val docRef = db.collection("users").document(uid).collection("notes").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "body" to cleanBody,
                "tag" to cleanTag,
                "pinned" to pinned,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateNote(
        userId: String,
        noteId: String,
        title: String,
        body: String,
        tag: String,
        pinned: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(noteId.isNotBlank()) { "Note ID must not be blank." }
            val cleanTitle = title.trim().take(140)
            require(cleanTitle.isNotEmpty()) { "Note title cannot be empty." }
            val cleanBody = body.trim().take(10000)
            val cleanTag = tag.trim().ifBlank { "Study" }.take(40)

            db.collection("users").document(uid).collection("notes").document(noteId)
                .update(
                    mapOf(
                        "title" to cleanTitle,
                        "body" to cleanBody,
                        "tag" to cleanTag,
                        "pinned" to pinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleNotePinned(userId: String, noteId: String, pinned: Boolean): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(noteId.isNotBlank()) { "Note ID must not be blank." }
            db.collection("users").document(uid).collection("notes").document(noteId)
                .update(
                    mapOf(
                        "pinned" to pinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(noteId.isNotBlank()) { "Note ID must not be blank." }
            db.collection("users").document(uid).collection("notes").document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addMilestone(
        userId: String,
        title: String,
        linkedTea: String = ""
    ): Result<Unit> {
        var docPath = "users/$userId/milestones"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanTitle = title.trim().take(160)
            require(cleanTitle.isNotEmpty()) { "Milestone title cannot be empty." }
            val cleanTea = linkedTea.trim().take(60)

            val docRef = db.collection("users").document(uid).collection("milestones").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "completed" to false,
                "linkedTea" to cleanTea,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleMilestone(
        userId: String,
        milestoneId: String,
        completed: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/milestones/$milestoneId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(milestoneId.isNotBlank()) { "Milestone ID must not be blank." }
            db.collection("users").document(uid).collection("milestones").document(milestoneId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMilestone(userId: String, milestoneId: String): Result<Unit> {
        val docPath = "users/$userId/milestones/$milestoneId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(milestoneId.isNotBlank()) { "Milestone ID must not be blank." }
            db.collection("users").document(uid).collection("milestones").document(milestoneId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createBook(
        userId: String,
        title: String,
        author: String,
        status: BookStatus,
        currentPage: Int,
        totalPages: Int,
        bookmarkLabel: String
    ): Result<Unit> {
        var docPath = "users/$userId/books"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanTitle = title.trim().take(160)
            val cleanAuthor = author.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Book title cannot be empty." }
            require(cleanAuthor.isNotEmpty()) { "Author name cannot be empty." }
            val validTotal = totalPages.coerceIn(1, 20000)
            val validCurrent = currentPage.coerceIn(0, validTotal)
            val cleanBookmark = bookmarkLabel.trim().take(60)

            val docRef = db.collection("users").document(uid).collection("books").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "author" to cleanAuthor,
                "status" to status.name,
                "currentPage" to validCurrent,
                "totalPages" to validTotal,
                "bookmarkLabel" to cleanBookmark,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateBookProgress(
        userId: String,
        bookId: String,
        currentPage: Int,
        totalPages: Int,
        status: BookStatus,
        bookmarkLabel: String
    ): Result<Unit> {
        val docPath = "users/$userId/books/$bookId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(bookId.isNotBlank()) { "Book ID must not be blank." }
            val validTotal = totalPages.coerceIn(1, 20000)
            val validCurrent = currentPage.coerceIn(0, validTotal)
            val resolvedStatus = if (validCurrent >= validTotal) BookStatus.Finished else status
            val cleanBookmark = bookmarkLabel.trim().take(60)

            db.collection("users").document(uid).collection("books").document(bookId)
                .update(
                    mapOf(
                        "currentPage" to validCurrent,
                        "status" to resolvedStatus.name,
                        "bookmarkLabel" to cleanBookmark,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteBook(userId: String, bookId: String): Result<Unit> {
        val docPath = "users/$userId/books/$bookId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(bookId.isNotBlank()) { "Book ID must not be blank." }
            db.collection("users").document(uid).collection("books").document(bookId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createQuote(
        userId: String,
        bookId: String,
        bookTitle: String,
        quoteText: String,
        pageNumber: Int,
        reflectionNote: String
    ): Result<Unit> {
        var docPath = "users/$userId/quotes"
        return try {
            val uid = requireVerifiedUserId(userId)
            val cleanBookTitle = bookTitle.trim().take(160)
            val cleanQuote = quoteText.trim().take(2000)
            require(cleanBookTitle.isNotEmpty()) { "Source book title is required." }
            require(cleanQuote.isNotEmpty()) { "Quote text cannot be empty." }
            val cleanPage = pageNumber.coerceIn(0, 20000)
            val cleanReflection = reflectionNote.trim().take(2000)

            val docRef = db.collection("users").document(uid).collection("quotes").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "bookId" to bookId.trim().take(128),
                "bookTitle" to cleanBookTitle,
                "quoteText" to cleanQuote,
                "pageNumber" to cleanPage,
                "reflectionNote" to cleanReflection,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteQuote(userId: String, quoteId: String): Result<Unit> {
        val docPath = "users/$userId/quotes/$quoteId"
        return try {
            val uid = requireVerifiedUserId(userId)
            require(quoteId.isNotBlank()) { "Quote ID must not be blank." }
            db.collection("users").document(uid).collection("quotes").document(quoteId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
