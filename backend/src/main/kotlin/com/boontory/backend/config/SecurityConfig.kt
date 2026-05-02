package com.boontory.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationConverter: JwtAuthenticationConverter,
    ): SecurityFilterChain {
        http
            .csrf().disable()
            .cors(Customizer.withDefaults())
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/**").access("isAuthenticated() and @jwtClientAccessEvaluator.hasBoontoryClient(authentication)")
            .anyRequest().permitAll()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter)

        return http.build()
    }

    @Bean
    fun keycloakJwtAuthoritiesConverter(
        @Value("\${boontory.security.keycloak-client-id:boontory-frontend}") keycloakClientId: String,
    ): Converter<Jwt, Collection<GrantedAuthority>> = KeycloakJwtAuthoritiesConverter(keycloakClientId)

    @Bean
    fun jwtAuthenticationConverter(
        keycloakJwtAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>>,
    ): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(keycloakJwtAuthoritiesConverter)
        return converter
    }
}
