package com.boontory.backend.catalog

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
@Order(2)
class GoogleBooksCatalogProvider(
    private val restTemplate: RestTemplate,
    @Value("\${boontory.google-books-base-url}") private val baseUrl: String,
    @Value("\${boontory.google-books-api-key:}") private val apiKey: String,
) : CatalogProvider {
    override val providerName: String = "google-books"

    override fun search(query: String, limit: Int): List<CatalogBookDto> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        val root = getJson(volumesUri(normalizedQuery, limit)) ?: return emptyList()
        return root.path("items")
            .takeIf(JsonNode::isArray)
            ?.mapNotNull(::mapVolume)
            .orEmpty()
    }

    override fun lookupByIsbn(isbn: String): CatalogBookDto? {
        val normalizedIsbn = isbn.normalizeIsbn()
        if (normalizedIsbn.isBlank()) {
            return null
        }

        val root = getJson(volumesUri("isbn:$normalizedIsbn", 1)) ?: return null
        return root.path("items")
            .takeIf(JsonNode::isArray)
            ?.mapNotNull(::mapVolume)
            ?.firstOrNull()
            ?.copy(isbn = normalizedIsbn)
    }

    private fun volumesUri(query: String, limit: Int): String {
        val builder = UriComponentsBuilder.fromHttpUrl("$baseUrl/volumes")
            .queryParam("q", query)
            .queryParam("maxResults", limit.coerceIn(1, 40))

        if (apiKey.isNotBlank()) {
            builder.queryParam("key", apiKey)
        }

        return builder.encode().build().toUri().toString()
    }

    private fun getJson(url: String): JsonNode? =
        try {
            restTemplate.getForObject(url, JsonNode::class.java)
        } catch (_: RestClientException) {
            null
        }

    private fun mapVolume(node: JsonNode): CatalogBookDto? {
        val info = node.path("volumeInfo")
        val title = info.path("title").asText("").trim()
        if (title.isBlank()) {
            return null
        }

        val isbn = info.path("industryIdentifiers")
            .takeIf(JsonNode::isArray)
            ?.firstNotNullOfOrNull { identifier ->
                identifier.path("identifier").asText("").normalizeIsbn().takeIf(String::isNotBlank)
            }

        return CatalogBookDto(
            isbn = isbn,
            title = title,
            authors = info.path("authors").takeIf(JsonNode::isArray)
                ?.mapNotNull { it.asText().trim().takeIf(String::isNotBlank) }
                .orEmpty(),
            description = info.path("description").asText(null),
            coverUrl = normalizeCoverUrl(
                info.path("imageLinks").path("thumbnail").asText(null)
                    ?: info.path("imageLinks").path("smallThumbnail").asText(null),
            ),
            publishedYear = info.path("publishedDate").asText()
                .takeIf(String::isNotBlank)
                ?.take(4)
                ?.toIntOrNull(),
        )
    }

    private fun normalizeCoverUrl(url: String?): String? =
        url?.replace("http://", "https://")
}
