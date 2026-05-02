package com.boontory.backend

import com.boontory.backend.config.JwtClientAccessEvaluator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class JwtClientAccessEvaluatorTest {
    private val evaluator = JwtClientAccessEvaluator("boontory-frontend")

    @Test
    fun `accepts token when azp matches boontory client`() {
        val auth = JwtAuthenticationToken(
            Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .claim("azp", "boontory-frontend")
                .build(),
        )

        assertThat(evaluator.hasBoontoryClient(auth)).isTrue()
    }

    @Test
    fun `accepts token when audience contains boontory client`() {
        val auth = JwtAuthenticationToken(
            Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .audience(listOf("boontory-frontend"))
                .build(),
        )

        assertThat(evaluator.hasBoontoryClient(auth)).isTrue()
    }

    @Test
    fun `rejects token when azp and audience target another client`() {
        val auth = JwtAuthenticationToken(
            Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .claim("azp", "other-client")
                .audience(listOf("other-client"))
                .build(),
        )

        assertThat(evaluator.hasBoontoryClient(auth)).isFalse()
    }
}
