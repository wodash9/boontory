package com.boontory.backend.catalog

import org.springframework.stereotype.Service

@Service
class CatalogProviderPipeline(
    private val providers: List<CatalogProvider>,
) {
    fun search(query: String, limit: Int = 12): List<CatalogBookDto> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        providers.forEach { provider ->
            val providerResults = provider.search(normalizedQuery, limit)
            if (providerResults.isNotEmpty()) {
                return providerResults.deduplicate().take(limit)
            }
        }

        return emptyList()
    }

    fun lookupByIsbn(isbn: String): CatalogBookDto? {
        val normalizedIsbn = isbn.normalizeIsbn()
        if (normalizedIsbn.isBlank()) {
            return null
        }

        return providers.firstNotNullOfOrNull { provider -> provider.lookupByIsbn(normalizedIsbn) }
    }

    private fun List<CatalogBookDto>.deduplicate(): List<CatalogBookDto> {
        val seen = mutableSetOf<String>()
        return filter { book -> seen.add(book.dedupeKey()) }
    }

    private fun CatalogBookDto.dedupeKey(): String =
        isbn?.normalizeIsbn()?.takeIf(String::isNotBlank)
            ?: listOf(title.trim().lowercase(), authors.joinToString("|") { it.trim().lowercase() })
                .joinToString("|")
}

fun String.normalizeIsbn(): String =
    uppercase().filter { it.isDigit() || it == 'X' }
