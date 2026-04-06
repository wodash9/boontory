package com.boontory.backend

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import com.boontory.backend.book.BookRepository
import com.boontory.backend.shelf.ShelfRepository
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever

@SpringBootTest
@AutoConfigureMockMvc
class ApiEndpointsTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var shelfRepository: ShelfRepository

    @MockBean
    private lateinit var restTemplate: RestTemplate

    @BeforeEach
    fun resetDatabase() {
        bookRepository.deleteAll()
        shelfRepository.deleteAll()
    }

    @Test
    fun `shelf endpoints support full CRUD and unassign books on delete`() {
        val shelfId = createShelf("Living Room", columns = 4, rows = 3)
        val bookId = createBook(title = "Dune", isbn = "9780441013593", shelfId = shelfId)

        mockMvc.perform(get("/api/shelves"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].id").value(shelfId.toInt()))
            .andExpect(jsonPath("$[0].name").value("Living Room"))
            .andExpect(jsonPath("$[0].layoutCapacity").value(12))
            .andExpect(jsonPath("$[0].bookCount").value(1))

        mockMvc.perform(get("/api/shelves/{id}", shelfId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Living Room"))
            .andExpect(jsonPath("$.columns").value(4))
            .andExpect(jsonPath("$.rows").value(3))

        mockMvc.perform(
            put("/api/shelves/{id}", shelfId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"Hallway","columns":2,"rows":5}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Hallway"))
            .andExpect(jsonPath("$.layoutCapacity").value(10))
            .andExpect(jsonPath("$.bookCount").value(1))

        mockMvc.perform(delete("/api/shelves/{id}", shelfId))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/books/{id}", bookId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.shelf").doesNotExist())

        mockMvc.perform(get("/api/shelves/{id}", shelfId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Shelf $shelfId was not found"))
    }

    @Test
    fun `shelf endpoints validate input and reject duplicate names`() {
        createShelf("Office", columns = 3, rows = 2)

        mockMvc.perform(
            post("/api/shelves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"Office","columns":1,"rows":1}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("A shelf named 'Office' already exists"))

        mockMvc.perform(
            post("/api/shelves")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"","columns":0,"rows":13}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.columns").exists())
            .andExpect(jsonPath("$.errors.rows").exists())
    }

    @Test
    fun `book endpoints support CRUD stats and filtering`() {
        val fictionShelfId = createShelf("Fiction", columns = 5, rows = 2)
        val historyShelfId = createShelf("History", columns = 2, rows = 2)

        val duneId = createBook(
            title = "Dune",
            isbn = "9780441013593",
            status = "READ",
            rating = 5,
            dateRead = "2024-03-10",
            shelfId = fictionShelfId,
            authors = listOf("Frank Herbert"),
        )
        createBook(
            title = "Sapiens",
            isbn = "9780062316097",
            status = "READING",
            rating = 4,
            shelfId = historyShelfId,
            authors = listOf("Yuval Noah Harari"),
        )

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(2)))

        mockMvc.perform(get("/api/books").param("query", "dune"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].title").value("Dune"))

        mockMvc.perform(get("/api/books").param("status", "READ"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].status").value("READ"))

        mockMvc.perform(get("/api/books").param("shelfId", fictionShelfId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].shelf.id").value(fictionShelfId.toInt()))

        mockMvc.perform(get("/api/books/{id}", duneId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value("9780441013593"))
            .andExpect(jsonPath("$.authors[0]").value("Frank Herbert"))

        mockMvc.perform(
            put("/api/books/{id}", duneId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    bookJson(
                        title = "Dune Messiah",
                        isbn = "9780441172696",
                        status = "READING",
                        rating = 3,
                        shelfId = historyShelfId,
                        authors = listOf("Frank Herbert"),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Dune Messiah"))
            .andExpect(jsonPath("$.status").value("READING"))
            .andExpect(jsonPath("$.shelf.id").value(historyShelfId.toInt()))

        mockMvc.perform(get("/api/books/stats"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalBooks").value(2))
            .andExpect(jsonPath("$.byStatus.READ").value(0))
            .andExpect(jsonPath("$.byStatus.READING").value(2))
            .andExpect(jsonPath("$.readByYear.2024").doesNotExist())

        mockMvc.perform(delete("/api/books/{id}", duneId))
            .andExpect(status().isOk)

        mockMvc.perform(get("/api/books/{id}", duneId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Book $duneId was not found"))
    }

    @Test
    fun `book endpoints validate input and reject duplicates or missing shelves`() {
        val shelfId = createShelf("Bedroom", columns = 2, rows = 3)
        createBook(title = "Existing", isbn = "9780140449266", shelfId = shelfId)

        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    bookJson(
                        title = "Duplicate ISBN",
                        isbn = "9780140449266",
                        authors = listOf("Author"),
                    ),
                ),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("A book with ISBN 9780140449266 already exists"))

        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    bookJson(
                        title = "Missing Shelf",
                        isbn = "9780140449273",
                        shelfId = 99999L,
                        authors = listOf("Author"),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Shelf 99999 was not found"))

        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"","authors":[""],"rating":7}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.title").exists())
            .andExpect(jsonPath("$.errors.rating").exists())
    }

    @Test
    fun `catalog endpoints proxy search and isbn lookup`() {
        val existingBookIsbn = "9780061120084"
        createBook(title = "To Kill a Mockingbird", isbn = existingBookIsbn)

        whenever(restTemplate.getForObject(eq("https://openlibrary.org/search.json?q=mockingbird&limit=12"), eq(JsonNode::class.java)))
            .thenReturn(
                json(
                    """
                    {
                      "docs": [
                        {
                          "title": "To Kill a Mockingbird",
                          "author_name": ["Harper Lee"],
                          "isbn": ["9780061120084"],
                          "cover_i": 123,
                          "first_publish_year": 1960
                        }
                      ]
                    }
                    """.trimIndent(),
                ),
            )

        whenever(
            restTemplate.getForObject(
                eq("https://openlibrary.org/api/books?bibkeys=ISBN:9780061120084&jscmd=data&format=json"),
                eq(JsonNode::class.java),
            ),
        ).thenReturn(
            json(
                """
                {
                  "ISBN:9780061120084": {
                    "title": "To Kill a Mockingbird",
                    "authors": [{"name": "Harper Lee"}],
                    "description": {"value": "Classic novel"},
                    "cover": {"medium": "https://covers.example/mock.jpg"},
                    "publish_date": "1960"
                  }
                }
                """.trimIndent(),
            ),
        )

        mockMvc.perform(get("/api/catalog/search").param("query", "mockingbird"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value("mockingbird"))
            .andExpect(jsonPath("$.results", hasSize<Any>(1)))
            .andExpect(jsonPath("$.results[0].title").value("To Kill a Mockingbird"))
            .andExpect(jsonPath("$.results[0].coverUrl").value("https://covers.openlibrary.org/b/id/123-M.jpg"))

        mockMvc.perform(get("/api/catalog/isbn/{isbn}", existingBookIsbn))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(existingBookIsbn))
            .andExpect(jsonPath("$.alreadyInLibrary").value(true))
            .andExpect(jsonPath("$.book.title").value("To Kill a Mockingbird"))
            .andExpect(jsonPath("$.book.description").value("Classic novel"))

        mockMvc.perform(get("/api/catalog/search").param("query", "   "))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value(""))
            .andExpect(jsonPath("$.results", hasSize<Any>(0)))

        mockMvc.perform(get("/api/catalog/isbn/{isbn}", "isbn: ???"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(""))
            .andExpect(jsonPath("$.alreadyInLibrary").value(false))
            .andExpect(jsonPath("$.book").doesNotExist())

        verify(restTemplate, never()).getForObject(eq("https://openlibrary.org/search.json?q=&limit=12"), eq(JsonNode::class.java))
        verify(restTemplate, never()).getForObject(eq("https://openlibrary.org/api/books?bibkeys=ISBN:&jscmd=data&format=json"), eq(JsonNode::class.java))
    }

    private fun createShelf(name: String, columns: Int, rows: Int): Long =
        objectMapper.readTree(
            mockMvc.perform(
                post("/api/shelves")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"$name","columns":$columns,"rows":$rows}
                        """.trimIndent(),
                    ),
            )
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString,
        ).path("id").asLong()

    private fun createBook(
        title: String,
        isbn: String,
        status: String = "WANT_TO_READ",
        rating: Int = 0,
        dateRead: String? = null,
        shelfId: Long? = null,
        authors: List<String> = listOf("Unknown Author"),
    ): Long =
        objectMapper.readTree(
            mockMvc.perform(
                post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookJson(title, isbn, status, rating, dateRead, shelfId, authors)),
            )
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString,
        ).path("id").asLong()

    private fun bookJson(
        title: String,
        isbn: String,
        status: String = "WANT_TO_READ",
        rating: Int = 0,
        dateRead: String? = null,
        shelfId: Long? = null,
        authors: List<String> = listOf("Unknown Author"),
    ): String =
        objectMapper.writeValueAsString(
            mapOf(
                "isbn" to isbn,
                "title" to title,
                "authors" to authors,
                "status" to status,
                "rating" to rating,
                "dateRead" to dateRead,
                "shelfId" to shelfId,
            ),
        )

    private fun json(raw: String): JsonNode = objectMapper.readTree(raw)

    companion object {
        private val dbPath = Files.createTempFile("boontory-api-test-", ".db").toAbsolutePath().toString()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("BOONTORY_DB_PATH") { dbPath }
            registry.add("FRONTEND_ORIGIN") { "http://localhost:5173" }
        }
    }
}
