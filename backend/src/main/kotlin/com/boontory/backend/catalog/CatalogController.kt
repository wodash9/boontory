package com.boontory.backend.catalog

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val openLibraryService: OpenLibraryService,
) {
    @GetMapping("/search")
    fun search(@RequestParam query: String): CatalogSearchResponse = openLibraryService.search(query)

    @GetMapping("/isbn/{isbn}")
    fun lookupByIsbn(@PathVariable isbn: String): IsbnLookupResponse = openLibraryService.lookupByIsbn(isbn)
}
