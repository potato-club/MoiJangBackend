package com.moijang.moijangbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * 팀방 비밀번호 해시 등 공통 인코더.
 * 인증(SecurityFilterChain)과 분리해, 팀/일정 담당자가 SecurityConfig를 건드리지 않도록 한다.
 */
@Configuration
class PasswordEncoderConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
