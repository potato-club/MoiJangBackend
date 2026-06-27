package com.moijang.moijangbackend.dto

data class GetMyDataResponse(
    val userId: Long,
    val email: String,
    val name: String,
)
