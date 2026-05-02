package com.boontory.backend

import com.boontory.backend.config.KeycloakJwtAuthoritiesConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtAuthoritiesConverterTest {
    private val converter = KeycloakJwtAuthoritiesConverter("boontory-frontend")

    @Test
    fun `maps realm and target client roles to spring authorities`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("realm_access", mapOf("roles" to listOf("user")))
            .claim(
                "resource_access",
                mapOf(
                    "boontory-frontend" to mapOf("roles" to listOf("admin")),
                ),
            )
            .build()

        val authorities = converter.convert(jwt).map { it.authority }

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
    }

    @Test
    fun `ignores unrelated client roles`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim(
                "resource_access",
                mapOf(
                    "other-client" to mapOf("roles" to listOf("admin")),
                ),
            )
            .build()

        val authorities = converter.convert(jwt).map { it.authority }

        assertThat(authorities).isEmpty()
    }

    @Test
    fun `ignores malformed claims`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("realm_access", "bad-shape")
            .claim("resource_access", listOf("bad-shape"))
            .build()

        val authorities = converter.convert(jwt).map { it.authority }

        assertThat(authorities).isEmpty()
    }

    @Test
    fun `dedupes duplicate roles across claims`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("realm_access", mapOf("roles" to listOf("user", "user")))
            .claim(
                "resource_access",
                mapOf(
                    "boontory-frontend" to mapOf("roles" to listOf("user", "USER")),
                ),
            )
            .build()

        val authorities = converter.convert(jwt).map { it.authority }

        assertThat(authorities).containsExactly("ROLE_USER")
    }
}
