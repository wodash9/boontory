package com.boontory.backend.shelf

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class ShelfSummaryDto(
    val id: Long,
    val name: String,
    val columns: Int,
    val rows: Int,
    val layoutCapacity: Int,
    val bookCount: Long,
)

data class UpsertShelfRequest(
    @field:NotBlank @field:Size(max = 80) val name: String,
    @field:Min(1) @field:Max(12) val columns: Int,
    @field:Min(1) @field:Max(12) val rows: Int,
)

fun ShelfEntity.toSummaryDto(bookCount: Long = 0): ShelfSummaryDto =
    ShelfSummaryDto(
        id = requireNotNull(id),
        name = name,
        columns = columns,
        rows = rows,
        layoutCapacity = columns * rows,
        bookCount = bookCount,
    )

fun UpsertShelfRequest.applyTo(entity: ShelfEntity): ShelfEntity {
    entity.name = name.trim()
    entity.columns = columns
    entity.rows = rows
    return entity
}
