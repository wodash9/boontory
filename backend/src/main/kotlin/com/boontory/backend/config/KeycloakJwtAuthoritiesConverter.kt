package com.boontory.backend.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtAuthoritiesConverter(
    private val clientId: String = "boontory-frontend",
) : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = extractRoles(jwt.getClaim<Any>("realm_access"))

        val clientRoles = (jwt.getClaim<Any>("resource_access") as? Map<*, *> ?: emptyMap<String, Any>())
            .let { it[clientId] }
            .let { extractRoles(it) }

        return (realmRoles + clientRoles)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.uppercase() }
            .distinct()
            .map { role -> SimpleGrantedAuthority("ROLE_$role") }
    }

    private fun extractRoles(claim: Any?): List<String> {
        val claimMap = claim as? Map<*, *> ?: return emptyList()
        return (claimMap["roles"] as? Collection<*>)
            .orEmpty()
            .filterIsInstance<String>()
    }
}
