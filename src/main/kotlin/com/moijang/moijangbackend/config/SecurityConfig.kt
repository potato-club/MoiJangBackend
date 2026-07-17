package com.moijang.moijangbackend.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig() {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Order(1)
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**", "/error")
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .headers { it.frameOptions { it.sameOrigin() } }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized"
                    )
                }
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/scalar/**",
                        "/favicon.ico",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login {
                it
                    .defaultSuccessUrl("http://localhost:5173/oauth/success", true)
                    .failureUrl("http://localhost:5173/oauth/failure")
            }
            .logout {
                it.logoutUrl("/api/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_OK
                        response.writer.write("{\"message\":\"Sign Out Success\"}")
                    }
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            }

        return http.build()
    }

    @Order(2)
    @Bean
    fun webFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .oauth2Login {
                it
                    .defaultSuccessUrl("http://localhost:5173/oauth/success")
                    .failureUrl("http://localhost:5173/oauth/failure")
            }

        return http.build()
    }


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}