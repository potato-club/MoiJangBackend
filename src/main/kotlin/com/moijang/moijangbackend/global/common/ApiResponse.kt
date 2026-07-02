package com.moijang.moijangbackend.global.common

sealed class ApiResponse<out T> {
    data class Success<out T>(
        val data: T,
        val message: String = "Success",
    ) : ApiResponse<T>()

    data class Failure(
        val errorCode: String,
        val errorMessage: String,
    ) : ApiResponse<Nothing>()

    data class Ok(
        val message: String,
    ) : ApiResponse<Nothing>()
}