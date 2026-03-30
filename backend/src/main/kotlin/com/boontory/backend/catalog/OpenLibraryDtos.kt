package com.boontory.backend.catalog

data class CatalogBookDto(
    val isbn: String? = null,
    val title: String,
    val authors: List<String>,
    val description: String? = null,
    val coverUrl: String? = null,
    val publishedYear: Int? = null,
)

data class CatalogSearchResponse(
    val query: String,
    val results: List<CatalogBookDto>,
)

data class IsbnLookupResponse(
    val isbn: String,
    val book: CatalogBookDto?,
    val alreadyInLibrary: Boolean,
)
