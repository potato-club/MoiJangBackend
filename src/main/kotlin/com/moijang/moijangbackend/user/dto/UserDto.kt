package com.moijang.moijangbackend.user.dto

data class GetMyDataResponse(
    val userId: Long,
    val email: String,
    val name: String,
)
