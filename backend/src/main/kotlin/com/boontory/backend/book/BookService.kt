package com.boontory.backend.book

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface CrudService<ID, DTO, REQUEST> {
    fun list(query: String?, status: ReadingStatus?): List<DTO>
    fun get(id: ID): DTO
    fun create(request: REQUEST): DTO
    fun update(id: ID, request: REQUEST): DTO
    fun delete(id: ID)
}

@Service
class BookService(
    private val repository: BookRepository,
) : CrudService<Long, BookDto, UpsertBookRequest> {
    @Transactional(readOnly = true)
    override fun list(query: String?, status: ReadingStatus?): List<BookDto> =
        repository.search(query?.trim()?.ifBlank { null }, status).map(BookEntity::toDto)

    @Transactional(readOnly = true)
    override fun get(id: Long): BookDto = findEntity(id).toDto()

    @Transactional
    override fun create(request: UpsertBookRequest): BookDto {
        ensureIsbnAvailable(request.isbn, null)
        return repository.save(request.applyTo(BookEntity())).toDto()
    }

    @Transactional
    override fun update(id: Long, request: UpsertBookRequest): BookDto {
        val entity = findEntity(id)
        ensureIsbnAvailable(request.isbn, id)
        return repository.save(request.applyTo(entity)).toDto()
    }

    @Transactional
    override fun delete(id: Long) {
        repository.delete(findEntity(id))
    }

    @Transactional(readOnly = true)
    fun stats(): LibraryStatsDto {
        val books = repository.findAll()
        val byStatus = ReadingStatus.values().associateWith { status ->
            books.count { it.status == status }.toLong()
        }
        val readByYear = books.asSequence()
            .filter { it.status == ReadingStatus.READ && it.dateRead != null }
            .groupingBy { requireNotNull(it.dateRead).year }
            .eachCount()
            .toSortedMap()
            .mapValues { it.value.toLong() }

        return LibraryStatsDto(
            totalBooks = books.size.toLong(),
            byStatus = byStatus,
            readByYear = readByYear,
        )
    }

    @Transactional(readOnly = true)
    fun findByIsbn(isbn: String): BookDto? = repository.findByIsbn(isbn)?.toDto()

    private fun findEntity(id: Long): BookEntity =
        repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Book $id was not found")
        }

    private fun ensureIsbnAvailable(isbn: String?, currentId: Long?) {
        val normalized = isbn?.trim()?.ifBlank { null } ?: return
        val existing = repository.findByIsbn(normalized) ?: return
        if (existing.id != currentId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A book with ISBN $normalized already exists")
        }
    }
}
