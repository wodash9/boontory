package com.boontory.backend.catalog

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

@Service
@Order(1)
class BneSruCatalogProvider(
    private val restTemplate: RestTemplate,
    @Value("\${boontory.bne-sru-base-url}") private val baseUrl: String,
) : CatalogProvider {
    override val providerName: String = "bne-sru"

    override fun search(query: String, limit: Int): List<CatalogBookDto> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return emptyList()
        }

        return getXml(sruUri(query = cqlEquals("alma.title", normalizedQuery), limit = limit))
            ?.let(::parseMarcXml)
            .orEmpty()
    }

    override fun lookupByIsbn(isbn: String): CatalogBookDto? {
        val normalizedIsbn = isbn.normalizeIsbn()
        if (normalizedIsbn.isBlank()) {
            return null
        }

        return getXml(sruUri(query = cqlEquals("alma.isbn", normalizedIsbn), limit = 1))
            ?.let(::parseMarcXml)
            ?.firstOrNull()
            ?.copy(isbn = normalizedIsbn)
    }

    private fun cqlEquals(field: String, value: String): String =
        "$field=\"${value.escapeCqlValue()}\""

    private fun sruUri(query: String, limit: Int): String =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .queryParam("operation", "searchRetrieve")
            .queryParam("version", "1.2")
            .queryParam("query", query)
            .queryParam("recordSchema", "marcxml")
            .queryParam("maximumRecords", limit.coerceIn(1, 50))
            .encode()
            .build()
            .toUri()
            .toString()

    private fun getXml(url: String): String? =
        try {
            restTemplate.getForObject(url, String::class.java)
        } catch (_: RestClientException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun parseMarcXml(xml: String): List<CatalogBookDto> =
        try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setSafeFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setSafeFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setSafeFeature("http://xml.org/sax/features/external-general-entities", false)
                setSafeFeature("http://xml.org/sax/features/external-parameter-entities", false)
                setSafeFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                setSafeAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
                setSafeAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
                isExpandEntityReferences = false
            }
            val document = factory.newDocumentBuilder().parse(InputSource(StringReader(xml)))
            document.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "record")
                .asElements()
                .mapNotNull(::mapRecord)
        } catch (_: Exception) {
            emptyList()
        }

    private fun mapRecord(record: Element): CatalogBookDto? {
        val title = title(record) ?: return null
        return CatalogBookDto(
            isbn = firstSubfield(record, "020", "a")?.extractIsbn(),
            title = title,
            authors = authors(record),
            publishedYear = publishedYear(record),
        )
    }

    private fun title(record: Element): String? {
        val titleField = datafields(record, "245").firstOrNull() ?: return null
        val mainTitle = subfields(titleField, "a").firstOrNull()?.cleanMarcText() ?: return null
        val subtitle = subfields(titleField, "b").firstOrNull()?.cleanMarcText()
        return listOfNotNull(mainTitle, subtitle)
            .joinToString(": ")
            .ifBlank { null }
    }

    private fun authors(record: Element): List<String> =
        (datafields(record, "100") + datafields(record, "700"))
            .flatMap { subfields(it, "a") }
            .mapNotNull { it.cleanMarcText().takeIf(String::isNotBlank) }
            .distinct()

    private fun publishedYear(record: Element): Int? =
        (datafields(record, "264") + datafields(record, "260"))
            .flatMap { subfields(it, "c") }
            .firstNotNullOfOrNull { YEAR_REGEX.find(it)?.value?.toIntOrNull() }

    private fun firstSubfield(record: Element, tag: String, code: String): String? =
        datafields(record, tag).firstNotNullOfOrNull { field -> subfields(field, code).firstOrNull() }

    private fun datafields(record: Element, tag: String): List<Element> =
        record.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "datafield")
            .asElements()
            .filter { it.getAttribute("tag") == tag }

    private fun subfields(datafield: Element, code: String): List<String> =
        datafield.getElementsByTagNameNS("http://www.loc.gov/MARC21/slim", "subfield")
            .asElements()
            .filter { it.getAttribute("code") == code }
            .map { it.textContent.trim() }

    private fun org.w3c.dom.NodeList.asElements(): List<Element> =
        (0 until length).mapNotNull { item(it) as? Element }

    private fun String.cleanMarcText(): String =
        trim().trimEnd('/', ':', ';', ',', '.').trim()

    private fun String.extractIsbn(): String? =
        ISBN_REGEX.find(this)?.value?.normalizeIsbn()?.takeIf(String::isNotBlank)
            ?: normalizeIsbn().takeIf(String::isNotBlank)

    private fun String.escapeCqlValue(): String =
        replace("\\", "\\\\").replace("\"", "\\\"")

    private fun DocumentBuilderFactory.setSafeFeature(feature: String, enabled: Boolean) {
        try {
            setFeature(feature, enabled)
        } catch (_: Exception) {
            // Keep parser portable across JVM XML implementations.
        }
    }

    private fun DocumentBuilderFactory.setSafeAttribute(attribute: String, value: String) {
        try {
            setAttribute(attribute, value)
        } catch (_: Exception) {
            // Keep parser portable across JVM XML implementations.
        }
    }

    private companion object {
        val ISBN_REGEX = Regex("(?i)\\b(?:97[89][0-9\\- ]{10,16}|[0-9][0-9\\- ]{8}[0-9X])\\b")
        val YEAR_REGEX = Regex("\\b(1[5-9]\\d{2}|20\\d{2})\\b")
    }
}
