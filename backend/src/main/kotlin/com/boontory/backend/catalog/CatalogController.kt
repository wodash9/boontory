package com.boontory.backend.catalog

import com.boontory.backend.book.BookService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val catalogProviderPipeline: CatalogProviderPipeline,
    private val bookService: BookService,
) {
    @GetMapping("/search")
    fun search(@RequestParam query: String): CatalogSearchResponse {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return CatalogSearchResponse(query = "", results = emptyList())
        }

        return CatalogSearchResponse(
            query = normalizedQuery,
            results = catalogProviderPipeline.search(normalizedQuery),
        )
    }

    @GetMapping("/isbn/{isbn}")
    fun lookupByIsbn(@PathVariable isbn: String): IsbnLookupResponse {
        val normalizedIsbn = isbn.normalizeIsbn()
        val inLibrary = normalizedIsbn.isNotBlank() && bookService.findByIsbn(normalizedIsbn) != null
        if (normalizedIsbn.isBlank()) {
            return IsbnLookupResponse(isbn = normalizedIsbn, book = null, alreadyInLibrary = inLibrary)
        }

        return IsbnLookupResponse(
            isbn = normalizedIsbn,
            book = catalogProviderPipeline.lookupByIsbn(normalizedIsbn),
            alreadyInLibrary = inLibrary,
        )
    }
}
