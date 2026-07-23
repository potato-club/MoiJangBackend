package com.moijang.moijangbackend.config

import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.entity.UserRole
import com.moijang.moijangbackend.user.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * 로컬 개발용 시드 데이터.
 * 인증/유저 담당과 협의해 유지한다. 팀·일정 담당자는 테스트용으로만 의존한다.
 */
@Configuration
class DevDataInitializer {

    @Bean
    @Profile("default")
    fun seedDevUser(userRepository: UserRepository): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.findByEmail("test-user@gmail.com") == null) {
                userRepository.save(
                    User(
                        email = "test-user@gmail.com",
                        nickname = "하영",
                        provider = OAuthProvider.GOOGLE,
                        providerId = "dev-test-user",
                        role = UserRole.USER,
                    ),
                )
            }
        }
    }
}
