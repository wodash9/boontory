package com.boontory.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component("jwtClientAccessEvaluator")
class JwtClientAccessEvaluator(
    @Value("\${boontory.security.keycloak-client-id:boontory-frontend}")
    private val keycloakClientId: String,
) {
    fun hasBoontoryClient(authentication: Authentication?): Boolean {
        if (authentication == null) {
            return false
        }

        val jwt = (authentication as? JwtAuthenticationToken)?.token
        if (jwt != null) {
            val azp = jwt.getClaimAsString("azp")
            if (azp == keycloakClientId) {
                return true
            }
            return jwt.audience.contains(keycloakClientId)
        }

        return authentication.isAuthenticated
    }
}
