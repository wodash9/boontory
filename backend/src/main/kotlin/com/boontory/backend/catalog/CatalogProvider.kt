package com.boontory.backend.catalog

interface CatalogProvider {
    val providerName: String

    fun search(query: String, limit: Int = 12): List<CatalogBookDto>

    fun lookupByIsbn(isbn: String): CatalogBookDto?
}
