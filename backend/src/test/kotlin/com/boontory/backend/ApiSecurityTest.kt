package com.boontory.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.file.Files

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `api endpoints reject unauthenticated requests`() {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `jwt for boontory client via azp can reach api endpoints`() {
        mockMvc.perform(
            get("/api/books").with(
                jwt().jwt {
                    it.claim("realm_access", mapOf("roles" to listOf("user")))
                    it.claim("azp", "boontory-frontend")
                },
            ),
        ).andExpect(status().isOk)
    }

    @Test
    fun `jwt for boontory client via audience can reach api endpoints`() {
        mockMvc.perform(
            get("/api/books").with(
                jwt().jwt {
                    it.claim("realm_access", mapOf("roles" to listOf("user")))
                    it.audience(listOf("boontory-frontend"))
                },
            ),
        ).andExpect(status().isOk)
    }

    @Test
    fun `jwt for another client is rejected from api endpoints`() {
        mockMvc.perform(
            get("/api/books").with(
                jwt().jwt {
                    it.claim("realm_access", mapOf("roles" to listOf("user")))
                    it.claim("azp", "other-client")
                    it.audience(listOf("other-client"))
                },
            ),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `actuator health stays public because only api routes require auth`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
    }

    companion object {
        private val dbPath = Files.createTempFile("boontory-api-security-test-", ".db").toAbsolutePath().toString()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("BOONTORY_DB_PATH") { dbPath }
        }
    }
}
