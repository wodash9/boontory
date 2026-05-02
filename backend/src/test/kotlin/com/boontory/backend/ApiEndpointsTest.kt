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
import org.springframework.security.test.context.support.WithMockUser
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
@WithMockUser(username = "ventura", roles = ["USER"])
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
            .andExpect(status().isNoContent)

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
            .andExpect(status().isNoContent)

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
        verify(restTemplate, never()).getForObject(
            eq("https://www.googleapis.com/books/v1/volumes?q=mockingbird&maxResults=12"),
            eq(JsonNode::class.java),
        )

        whenever(restTemplate.getForObject(eq("https://openlibrary.org/search.json?q=googleonly&limit=12"), eq(JsonNode::class.java)))
            .thenReturn(json("""{"docs": []}"""))
        whenever(restTemplate.getForObject(eq("https://www.googleapis.com/books/v1/volumes?q=googleonly&maxResults=12"), eq(JsonNode::class.java)))
            .thenReturn(
                json(
                    """
                    {
                      "items": [
                        {
                          "volumeInfo": {
                            "title": "Google Search Result",
                            "authors": ["Search Author"],
                            "publishedDate": "2019",
                            "industryIdentifiers": [
                              {"type": "ISBN_13", "identifier": "9780000000003"}
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                ),
            )

        mockMvc.perform(get("/api/catalog/search").param("query", "googleonly"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value("googleonly"))
            .andExpect(jsonPath("$.results", hasSize<Any>(1)))
            .andExpect(jsonPath("$.results[0].title").value("Google Search Result"))
            .andExpect(jsonPath("$.results[0].authors[0]").value("Search Author"))

        whenever(restTemplate.getForObject(eq("https://openlibrary.org/search.json?q=bneonly&limit=12"), eq(JsonNode::class.java)))
            .thenReturn(json("""{"docs": []}"""))
        whenever(
            restTemplate.getForObject(
                eq("https://catalogo.bne.es/view/sru/34BNE_INST?operation=searchRetrieve&version=1.2&query=alma.title%3D%22bneonly%22&recordSchema=marcxml&maximumRecords=12"),
                eq(String::class.java),
            ),
        ).thenReturn(
            """
            <srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/" xmlns:marc="http://www.loc.gov/MARC21/slim">
              <srw:records>
                <srw:record>
                  <srw:recordData>
                    <marc:record>
                      <marc:datafield tag="020"><marc:subfield code="a">9788400000001</marc:subfield></marc:datafield>
                      <marc:datafield tag="100"><marc:subfield code="a">Autora BNE</marc:subfield></marc:datafield>
                      <marc:datafield tag="245">
                        <marc:subfield code="a">Libro desde BNE :</marc:subfield>
                        <marc:subfield code="b">subtítulo SRU</marc:subfield>
                      </marc:datafield>
                      <marc:datafield tag="264"><marc:subfield code="c">2020</marc:subfield></marc:datafield>
                    </marc:record>
                  </srw:recordData>
                </srw:record>
              </srw:records>
            </srw:searchRetrieveResponse>
            """.trimIndent(),
        )

        mockMvc.perform(get("/api/catalog/search").param("query", "bneonly"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value("bneonly"))
            .andExpect(jsonPath("$.results", hasSize<Any>(1)))
            .andExpect(jsonPath("$.results[0].isbn").value("9788400000001"))
            .andExpect(jsonPath("$.results[0].title").value("Libro desde BNE: subtítulo SRU"))
            .andExpect(jsonPath("$.results[0].authors[0]").value("Autora BNE"))
            .andExpect(jsonPath("$.results[0].publishedYear").value(2020))
        verify(restTemplate, never()).getForObject(
            eq("https://www.googleapis.com/books/v1/volumes?q=bneonly&maxResults=12"),
            eq(JsonNode::class.java),
        )

        whenever(restTemplate.getForObject(eq("https://openlibrary.org/search.json?q=harry%20potter&limit=12"), eq(JsonNode::class.java)))
            .thenReturn(json("""{"docs": []}"""))
        whenever(restTemplate.getForObject(eq("https://www.googleapis.com/books/v1/volumes?q=harry%20potter&maxResults=12"), eq(JsonNode::class.java)))
            .thenReturn(
                json(
                    """
                    {
                      "items": [
                        {
                          "volumeInfo": {
                            "title": "Harry Potter and the Philosopher's Stone",
                            "authors": ["J. K. Rowling"]
                          }
                        }
                      ]
                    }
                    """.trimIndent(),
                ),
            )

        mockMvc.perform(get("/api/catalog/search").param("query", "harry potter"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.query").value("harry potter"))
            .andExpect(jsonPath("$.results[0].title").value("Harry Potter and the Philosopher's Stone"))

        mockMvc.perform(get("/api/catalog/isbn/{isbn}", existingBookIsbn))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(existingBookIsbn))
            .andExpect(jsonPath("$.alreadyInLibrary").value(true))
            .andExpect(jsonPath("$.book.title").value("To Kill a Mockingbird"))
            .andExpect(jsonPath("$.book.description").value("Classic novel"))

        val googleOnlyIsbn = "9780000000002"
        whenever(
            restTemplate.getForObject(
                eq("https://openlibrary.org/api/books?bibkeys=ISBN:9780000000002&jscmd=data&format=json"),
                eq(JsonNode::class.java),
            ),
        ).thenReturn(json("{}"))
        whenever(
            restTemplate.getForObject(
                eq("https://www.googleapis.com/books/v1/volumes?q=isbn:9780000000002&maxResults=1"),
                eq(JsonNode::class.java),
            ),
        ).thenReturn(
            json(
                """
                {
                  "items": [
                    {
                      "volumeInfo": {
                        "title": "Google Books Only",
                        "authors": ["Fallback Author"],
                        "description": "Found after Open Library missed it",
                        "publishedDate": "2021-04-15",
                        "imageLinks": {"thumbnail": "http://books.google.com/cover.jpg"},
                        "industryIdentifiers": [
                          {"type": "ISBN_13", "identifier": "9780000000002"}
                        ]
                      }
                    }
                  ]
                }
                """.trimIndent(),
            ),
        )

        mockMvc.perform(get("/api/catalog/isbn/{isbn}", googleOnlyIsbn))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(googleOnlyIsbn))
            .andExpect(jsonPath("$.alreadyInLibrary").value(false))
            .andExpect(jsonPath("$.book.title").value("Google Books Only"))
            .andExpect(jsonPath("$.book.authors[0]").value("Fallback Author"))
            .andExpect(jsonPath("$.book.coverUrl").value("https://books.google.com/cover.jpg"))
            .andExpect(jsonPath("$.book.publishedYear").value(2021))

        val bneOnlyIsbn = "9788400000002"
        whenever(
            restTemplate.getForObject(
                eq("https://openlibrary.org/api/books?bibkeys=ISBN:9788400000002&jscmd=data&format=json"),
                eq(JsonNode::class.java),
            ),
        ).thenReturn(json("{}"))
        whenever(
            restTemplate.getForObject(
                eq("https://catalogo.bne.es/view/sru/34BNE_INST?operation=searchRetrieve&version=1.2&query=alma.isbn%3D%229788400000002%22&recordSchema=marcxml&maximumRecords=1"),
                eq(String::class.java),
            ),
        ).thenReturn(
            """
            <srw:searchRetrieveResponse xmlns:srw="http://www.loc.gov/zing/srw/" xmlns:marc="http://www.loc.gov/MARC21/slim">
              <srw:records>
                <srw:record>
                  <srw:recordData>
                    <marc:record>
                      <marc:datafield tag="020"><marc:subfield code="a">978-84-000-0002-0</marc:subfield></marc:datafield>
                      <marc:datafield tag="100"><marc:subfield code="a">Autor ISBN BNE</marc:subfield></marc:datafield>
                      <marc:datafield tag="245"><marc:subfield code="a">ISBN desde BNE</marc:subfield></marc:datafield>
                      <marc:datafield tag="260"><marc:subfield code="c">2018</marc:subfield></marc:datafield>
                    </marc:record>
                  </srw:recordData>
                </srw:record>
              </srw:records>
            </srw:searchRetrieveResponse>
            """.trimIndent(),
        )

        mockMvc.perform(get("/api/catalog/isbn/{isbn}", bneOnlyIsbn))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(bneOnlyIsbn))
            .andExpect(jsonPath("$.alreadyInLibrary").value(false))
            .andExpect(jsonPath("$.book.title").value("ISBN desde BNE"))
            .andExpect(jsonPath("$.book.authors[0]").value("Autor ISBN BNE"))
            .andExpect(jsonPath("$.book.publishedYear").value(2018))
        verify(restTemplate, never()).getForObject(
            eq("https://www.googleapis.com/books/v1/volumes?q=isbn:9788400000002&maxResults=1"),
            eq(JsonNode::class.java),
        )

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
