package com.boontory.backend.catalog

import com.boontory.backend.book.BookService
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class OpenLibraryService(
    private val restTemplate: RestTemplate,
    private val bookService: BookService,
    @Value("\${boontory.open-library-base-url}") private val baseUrl: String,
) {
    fun search(query: String): CatalogSearchResponse {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return CatalogSearchResponse(query = "", results = emptyList())
        }

        val uri = UriComponentsBuilder.fromHttpUrl("$baseUrl/search.json")
            .queryParam("q", normalizedQuery)
            .queryParam("limit", 12)
            .build(true)
            .toUri()

        val root = getJson(uri.toString()) ?: return CatalogSearchResponse(normalizedQuery, emptyList())
        val results = root.path("docs")
            .takeIf(JsonNode::isArray)
            ?.mapNotNull(::mapSearchDoc)
            .orEmpty()

        return CatalogSearchResponse(normalizedQuery, results)
    }

    fun lookupByIsbn(isbn: String): IsbnLookupResponse {
        val normalizedIsbn = isbn.filter(Char::isDigit)
        val inLibrary = bookService.findByIsbn(normalizedIsbn) != null
        if (normalizedIsbn.isBlank()) {
            return IsbnLookupResponse(isbn = normalizedIsbn, book = null, alreadyInLibrary = inLibrary)
        }

        val uri = UriComponentsBuilder.fromHttpUrl("$baseUrl/api/books")
            .queryParam("bibkeys", "ISBN:$normalizedIsbn")
            .queryParam("jscmd", "data")
            .queryParam("format", "json")
            .build(true)
            .toUri()

        val root = getJson(uri.toString())
        val bookNode = root?.path("ISBN:$normalizedIsbn")
        val book = if (bookNode != null && !bookNode.isMissingNode && !bookNode.isNull) {
            mapBookNode(normalizedIsbn, bookNode)
        } else {
            null
        }

        return IsbnLookupResponse(isbn = normalizedIsbn, book = book, alreadyInLibrary = inLibrary)
    }

    private fun getJson(url: String): JsonNode? =
        try {
            restTemplate.getForObject(url, JsonNode::class.java)
        } catch (_: RestClientException) {
            null
        }

    private fun mapSearchDoc(node: JsonNode): CatalogBookDto? {
        val title = node.path("title").asText("").trim()
        if (title.isBlank()) {
            return null
        }

        val coverId = node.path("cover_i").takeIf { it.isInt }?.asInt()
        return CatalogBookDto(
            isbn = node.path("isbn").takeIf(JsonNode::isArray)?.firstOrNull()?.asText(),
            title = title,
            authors = node.path("author_name").takeIf(JsonNode::isArray)?.mapNotNull { it.asText().takeIf(String::isNotBlank) }.orEmpty(),
            coverUrl = coverId?.let(::coverUrl),
            publishedYear = node.path("first_publish_year").takeIf(JsonNode::isInt)?.asInt(),
        )
    }

    private fun mapBookNode(isbn: String, node: JsonNode): CatalogBookDto =
        CatalogBookDto(
            isbn = isbn,
            title = node.path("title").asText("Unknown title"),
            authors = node.path("authors").takeIf(JsonNode::isArray)?.mapNotNull { author ->
                author.path("name").asText().takeIf(String::isNotBlank)
            }.orEmpty(),
            description = node.path("description").let { descriptionNode ->
                when {
                    descriptionNode.isTextual -> descriptionNode.asText()
                    descriptionNode.isObject -> descriptionNode.path("value").asText(null)
                    else -> null
                }
            },
            coverUrl = node.path("cover").path("medium").asText(null)
                ?: node.path("cover").path("large").asText(null),
            publishedYear = node.path("publish_date").asText()
                .takeIf(String::isNotBlank)
                ?.takeLast(4)
                ?.toIntOrNull(),
        )

    private fun coverUrl(coverId: Int): String = "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
}
