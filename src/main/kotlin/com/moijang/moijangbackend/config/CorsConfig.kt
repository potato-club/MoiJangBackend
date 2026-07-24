package com.moijang.moijangbackend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * 프론트(로컬/배포) origin에서 API 호출을 허용한다.
 * SecurityFilterChain에서 http.cors {} 가 켜져 있어야 적용된다.
 */
@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(
        @Value("\${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
        allowedOrigins: String,
    ): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            this.allowedOrigins = allowedOrigins
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
