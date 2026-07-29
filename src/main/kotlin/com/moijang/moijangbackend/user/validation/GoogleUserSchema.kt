package com.moijang.moijangbackend.user.validation

import com.moijang.moijangbackend.user.dto.GetMyDataResponse

data class GoogleUserSchema(
    val sub: String,
    val email: String,
    val name: String,
    val picture: String? = null
) {
    init {
        require(name.isNotBlank()) { "Name must not be blank" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(name.isNotBlank()) { "Name must not be blank" }
    }

    companion object {
        fun from(googleUser: GoogleUserSchema) = GetMyDataResponse(
            userId = googleUser.sub,
            email = googleUser.email,
            name = googleUser.name,
            picture = googleUser.picture
        )
    }
}