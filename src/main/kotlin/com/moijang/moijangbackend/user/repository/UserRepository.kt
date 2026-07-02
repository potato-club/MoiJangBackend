package com.moijang.moijangbackend.user.repository

import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): User?
}
