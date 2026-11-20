package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class FocusSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val label: String = "",
    val durationMinutes: Long = 25L,
    val teaName: String = "",
    val teaSteepMinutes: Long = 4L,
    val soundscapes: List<String> = emptyList(),
    @ServerTimestamp val completedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val tag: String = "Study",
    val pinned: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DiamondMilestone(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val linkedTea: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class BookStatus(val label: String) {
    Reading("Reading"),
    Finished("Finished"),
    Shelf("Shelf");

    companion object {
        fun fromRaw(raw: String?): BookStatus =
            entries.firstOrNull { it.name == raw } ?: Reading
    }
}

@Keep
@IgnoreExtraProperties
data class BookEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val author: String = "",
    val status: String = BookStatus.Reading.name,
    val currentPage: Long = 0L,
    val totalPages: Long = 100L,
    val bookmarkLabel: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedStatus(): BookStatus = BookStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class QuoteCard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val quoteText: String = "",
    val pageNumber: Long = 0L,
    val reflectionNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class TeaBlend(
    val id: String,
    val name: String,
    val steepMinutes: Int,
    val tastingNote: String,
    val temperatureCelsius: Int
)

object TeaCatalog {
    val blends = listOf(
        TeaBlend(
            id = "earl_grey",
            name = "Earl Grey Moonlight",
            steepMinutes = 4,
            tastingNote = "Bergamot & vanilla dusk",
            temperatureCelsius = 95
        ),
        TeaBlend(
            id = "chamomile",
            name = "Chamomile Hearth",
            steepMinutes = 5,
            tastingNote = "Honeyed blossom & cedar",
            temperatureCelsius = 98
        ),
        TeaBlend(
            id = "oolong",
            name = "Oolong Mist",
            steepMinutes = 3,
            tastingNote = "Toasted orchid & stone",
            temperatureCelsius = 88
        ),
        TeaBlend(
            id = "peppermint",
            name = "Peppermint Rain",
            steepMinutes = 4,
            tastingNote = "Crisp garden mint",
            temperatureCelsius = 96
        )
    )

    fun byIdOrFirst(id: String): TeaBlend =
        blends.firstOrNull { it.id == id } ?: blends.first()
}

enum class FocusPreset(val label: String, val minutes: Int, val isBreak: Boolean) {
    POMODORO_25("25m Deep Study", 25, false),
    EXTENDED_45("45m Long Chapter", 45, false),
    MANUSCRIPT_60("60m Manuscript", 60, false),
    TEA_BREAK_5("5m Steep Break", 5, true),
    REST_15("15m Hearth Rest", 15, true);

    companion object {
        fun byMinutesOrDefault(minutes: Int): FocusPreset =
            entries.firstOrNull { it.minutes == minutes } ?: POMODORO_25
    }
}

enum class SoundscapeChannel(val title: String, val subtitle: String) {
    NIGHT_RAIN("Night Rain", "Windowpane drizzle"),
    LIBRARY_HEARTH("Library Hearth", "Soft ember crackle"),
    PARCHMENT_SCROLL("Parchment Scroll", "Soft page rhythm"),
    DISTANT_THUNDER("Distant Thunder", "Low moonlit roll")
}

data class StudyDesk(
    val id: String,
    val name: String,
    val atmosphere: String,
    val defaultIntention: String,
    val recommendedMinutes: Int,
    val pairedTeaId: String
)

object StudyDeskCatalog {
    val desks = listOf(
        StudyDesk(
            id = "turret_window",
            name = "East Turret Desk",
            atmosphere = "Rain-washed alcove for deep reading & outlines",
            defaultIntention = "Chapter Study & Marginalia",
            recommendedMinutes = 25,
            pairedTeaId = "earl_grey"
        ),
        StudyDesk(
            id = "observatory_table",
            name = "Observatory Reading Table",
            atmosphere = "Long timber table beneath quiet star charts",
            defaultIntention = "Long Chapter Synthesis",
            recommendedMinutes = 45,
            pairedTeaId = "oolong"
        ),
        StudyDesk(
            id = "garden_bench",
            name = "Secret Garden Bench",
            atmosphere = "Lantern-lit stone bench for prose & drafting",
            defaultIntention = "Manuscript Drafting",
            recommendedMinutes = 60,
            pairedTeaId = "chamomile"
        )
    )
}
