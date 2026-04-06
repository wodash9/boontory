package com.boontory.backend.book

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) status: ReadingStatus?,
        @RequestParam(required = false) shelfId: Long?,
    ): List<BookDto> = bookService.list(query, status, shelfId)

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): BookDto = bookService.get(id)

    @PostMapping
    fun create(@Valid @RequestBody request: UpsertBookRequest): BookDto = bookService.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpsertBookRequest): BookDto =
        bookService.update(id, request)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = bookService.delete(id)

    @GetMapping("/stats")
    fun stats(): LibraryStatsDto = bookService.stats()
}
