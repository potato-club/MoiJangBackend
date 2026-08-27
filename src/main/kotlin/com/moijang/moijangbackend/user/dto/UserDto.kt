package com.moijang.moijangbackend.user.dto

data class UserDto(
    val userId: String,
    val email: String,
    val name: String,
    val picture: String?,
)
