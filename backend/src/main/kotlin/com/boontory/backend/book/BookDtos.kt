package com.boontory.backend.book

import java.time.Instant
import java.time.LocalDate
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class BookDto(
    val id: Long,
    val isbn: String?,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val coverUrl: String?,
    val status: ReadingStatus,
    val rating: Int,
    val notes: String?,
    val publishedYear: Int?,
    val dateRead: LocalDate?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)

data class UpsertBookRequest(
    val isbn: String? = null,
    @field:NotBlank val title: String,
    @field:Size(max = 10) val authors: List<@NotBlank String> = emptyList(),
    @field:Size(max = 2000) val description: String? = null,
    val coverUrl: String? = null,
    val status: ReadingStatus = ReadingStatus.WANT_TO_READ,
    @field:Min(0) @field:Max(5) val rating: Int = 0,
    @field:Size(max = 4000) val notes: String? = null,
    val publishedYear: Int? = null,
    val dateRead: LocalDate? = null,
)

data class LibraryStatsDto(
    val totalBooks: Long,
    val byStatus: Map<ReadingStatus, Long>,
    val readByYear: Map<Int, Long>,
)

fun BookEntity.toDto(): BookDto =
    BookDto(
        id = requireNotNull(id),
        isbn = isbn,
        title = title,
        authors = authors.split("|").filter { it.isNotBlank() },
        description = description,
        coverUrl = coverUrl,
        status = status,
        rating = rating,
        notes = notes,
        publishedYear = publishedYear,
        dateRead = dateRead,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun UpsertBookRequest.applyTo(entity: BookEntity): BookEntity {
    entity.isbn = isbn?.trim()?.ifBlank { null }
    entity.title = title.trim()
    entity.authors = authors.map(String::trim).filter(String::isNotBlank).joinToString("|")
    entity.description = description?.trim()?.ifBlank { null }
    entity.coverUrl = coverUrl?.trim()?.ifBlank { null }
    entity.status = status
    entity.rating = rating
    entity.notes = notes?.trim()?.ifBlank { null }
    entity.publishedYear = publishedYear
    entity.dateRead = dateRead
    return entity
}
