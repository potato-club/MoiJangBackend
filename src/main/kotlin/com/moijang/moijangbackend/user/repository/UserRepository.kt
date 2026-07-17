package com.moijang.moijangbackend.user.repository

import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    override fun findById(id: Long): Optional<User>

    fun findByEmail(email: String): User?

    fun findByProviderAndProviderId(provider: OAuthProvider, providerId: String): User?
}
