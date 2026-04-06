package com.boontory.backend.book

import com.boontory.backend.shelf.ShelfRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface CrudService<ID, DTO, REQUEST> {
    fun list(query: String?, status: ReadingStatus?, shelfId: Long? = null): List<DTO>
    fun get(id: ID): DTO
    fun create(request: REQUEST): DTO
    fun update(id: ID, request: REQUEST): DTO
    fun delete(id: ID)
}

@Service
class BookService(
    private val repository: BookRepository,
    private val shelfRepository: ShelfRepository,
) : CrudService<Long, BookDto, UpsertBookRequest> {
    @Transactional(readOnly = true)
    override fun list(query: String?, status: ReadingStatus?, shelfId: Long?): List<BookDto> =
        repository.search(query?.trim()?.ifBlank { null }, status, shelfId).map(BookEntity::toDto)

    @Transactional(readOnly = true)
    override fun get(id: Long): BookDto = findEntity(id).toDto()

    @Transactional
    override fun create(request: UpsertBookRequest): BookDto {
        ensureIsbnAvailable(request.isbn, null)
        val entity = request.applyTo(BookEntity())
        entity.shelf = resolveShelf(request.shelfId)
        return repository.save(entity).toDto()
    }

    @Transactional
    override fun update(id: Long, request: UpsertBookRequest): BookDto {
        val entity = findEntity(id)
        ensureIsbnAvailable(request.isbn, id)
        request.applyTo(entity)
        entity.shelf = resolveShelf(request.shelfId)
        return repository.save(entity).toDto()
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

    private fun resolveShelf(shelfId: Long?) = shelfId?.let {
        shelfRepository.findById(it).orElseThrow {
            ResponseStatusException(HttpStatus.BAD_REQUEST, "Shelf $it was not found")
        }
    }
}
