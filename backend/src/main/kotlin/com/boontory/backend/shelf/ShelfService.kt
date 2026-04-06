package com.boontory.backend.shelf

import com.boontory.backend.book.BookRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ShelfService(
    private val repository: ShelfRepository,
    private val bookRepository: BookRepository,
) {
    @Transactional(readOnly = true)
    fun list(): List<ShelfSummaryDto> =
        repository.findAllByOrderByNameAsc().map { shelf ->
            shelf.toSummaryDto(bookRepository.countByShelfId(requireNotNull(shelf.id)))
        }

    @Transactional(readOnly = true)
    fun get(id: Long): ShelfSummaryDto = findEntity(id).let { shelf ->
        shelf.toSummaryDto(bookRepository.countByShelfId(id))
    }

    @Transactional
    fun create(request: UpsertShelfRequest): ShelfSummaryDto {
        ensureNameAvailable(request.name, null)
        return repository.save(request.applyTo(ShelfEntity())).toSummaryDto()
    }

    @Transactional
    fun update(id: Long, request: UpsertShelfRequest): ShelfSummaryDto {
        val entity = findEntity(id)
        ensureNameAvailable(request.name, id)
        return repository.save(request.applyTo(entity)).toSummaryDto(bookRepository.countByShelfId(id))
    }

    @Transactional
    fun delete(id: Long) {
        findEntity(id)
        bookRepository.clearShelfForShelf(id)
        repository.deleteById(id)
    }

    private fun findEntity(id: Long): ShelfEntity =
        repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf $id was not found")
        }

    private fun ensureNameAvailable(name: String, currentId: Long?) {
        val normalized = name.trim()
        val existing = repository.findByNameIgnoreCase(normalized) ?: return
        if (existing.id != currentId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A shelf named '$normalized' already exists")
        }
    }
}
