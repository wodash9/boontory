package com.boontory.backend.catalog

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
@Order(0)
class OpenLibraryService(
    private val restTemplate: RestTemplate,
    @Value("\${boontory.open-library-base-url}") private val baseUrl: String,
) : CatalogProvider {
    override val providerName: String = "open-library"

    override fun search(query: String, limit: Int): List<CatalogBookDto> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        val uri = UriComponentsBuilder.fromHttpUrl("$baseUrl/search.json")
            .queryParam("q", normalizedQuery)
            .queryParam("limit", limit)
            .encode()
            .build()
            .toUri()

        val root = getJson(uri.toString()) ?: return emptyList()
        return root.path("docs")
            .takeIf(JsonNode::isArray)
            ?.mapNotNull(::mapSearchDoc)
            .orEmpty()
    }

    override fun lookupByIsbn(isbn: String): CatalogBookDto? {
        val normalizedIsbn = isbn.normalizeIsbn()
        if (normalizedIsbn.isBlank()) {
            return null
        }

        val uri = UriComponentsBuilder.fromHttpUrl("$baseUrl/api/books")
            .queryParam("bibkeys", "ISBN:$normalizedIsbn")
            .queryParam("jscmd", "data")
            .queryParam("format", "json")
            .encode()
            .build()
            .toUri()

        val root = getJson(uri.toString())
        val bookNode = root?.path("ISBN:$normalizedIsbn")
        return if (bookNode != null && !bookNode.isMissingNode && !bookNode.isNull) {
            mapBookNode(normalizedIsbn, bookNode)
        } else {
            null
        }
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
