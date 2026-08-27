package com.moijang.moijangbackend.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Service

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOauth2UserService: CustomOAuth2UserService
) {

    // PasswordEncoder는 PasswordEncoderConfig에서만 정의한다. (팀방 비밀번호 해시와 공유)

    @Order(1)
    @Bean
    fun oAuthFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/v1/oauth/**")
            .csrf { it.disable() }
            .cors { }
            .headers { it.frameOptions { config -> config.sameOrigin() } }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .oauth2Login {
                it.authorizationEndpoint { endpoint ->
                    endpoint.baseUri("/api/v1/oauth/authorize")
                }
                    .redirectionEndpoint { endpoint ->
                        endpoint.baseUri("/api/v1/oauth/code/*")
                    }
                    .defaultSuccessUrl("/login?success", true)
                    .failureUrl("/login?failure")
                    .userInfoEndpoint { it.userService(customOauth2UserService) }
            }

        return http.build()
    }

    @Order(2)
    @Bean
    fun defaultWebFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/**",
                    "/h2-console/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/scalar/**",
                    "/favicon.ico",
                ).permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .formLogin { it.disable() }
            .logout {
                it.logoutUrl("/api/v1/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_OK
                        response.writer.write("{\"message\":\"Sign Out Success\"}")
                    }
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .deleteCookies("SESSION")
            }

        return http.build()
    }
}

@Service
class CustomOAuth2UserService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val attributes = oAuth2User.attributes

        val email = attributes["email"] as? String
        val name = attributes["name"] as? String

        return oAuth2User
    }
}
