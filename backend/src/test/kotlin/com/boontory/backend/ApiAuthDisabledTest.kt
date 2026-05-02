package com.boontory.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.file.Files

@SpringBootTest(properties = ["boontory.security.disable-auth=true"])
@AutoConfigureMockMvc
class ApiAuthDisabledTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `local QA mode permits api requests without Keycloak`() {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
    }

    companion object {
        private val dbPath = Files.createTempFile("boontory-api-auth-disabled-test-", ".db").toAbsolutePath().toString()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("BOONTORY_DB_PATH") { dbPath }
        }
    }
}
