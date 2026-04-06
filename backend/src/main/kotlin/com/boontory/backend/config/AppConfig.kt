package com.boontory.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration

@Configuration
class AppConfig(
    @Value("\${boontory.frontend-origin-patterns}") private val frontendOriginPatterns: String,
) {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build()

    @Bean
    fun corsConfigurer(): WebMvcConfigurer =
        object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val originPatterns = frontendOriginPatterns
                    .split(',')
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .toTypedArray()

                registry.addMapping("/api/**")
                    .allowedOriginPatterns(*originPatterns)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            }
        }
}
