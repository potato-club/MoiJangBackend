package com.moijang.moijangbackend.config

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * [소유: 인증 담당]
 * OAuth2 / JWT / 인가 규칙을 이 클래스에서 구현한다.
 * 팀·일정 담당자는 수정하지 말고, 컨트롤러에서는 [com.moijang.moijangbackend.global.auth.CurrentUser]만 호출한다.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    // TODO(인증 담당): JWT 필터, OAuth 성공 핸들러, /api/** authenticated 전환
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            // 프론트 연동용. CorsConfig의 CorsConfigurationSource를 사용한다.
            .cors { }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/**",
                        "/h2-console/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/scalar/**",
                        "/favicon.ico",
                    ).permitAll()
                    .requestMatchers(PathRequest.toH2Console()).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { }

        return http.build()
    }
}
