package com.moijang.moijangbackend.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun success(message: String): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                message,
                data = null
            )
        }
        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message,
                data
            )
        }
        fun failed(message: String): ApiResponse<Unit> {
            return ApiResponse(
                success = false,
                message,
                data = null
            )
        }
        fun <T> failed(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message,
                data
            )
        }
    }
}