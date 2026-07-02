package com.moijang.moijangbackend.global.common

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun success(message: String): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                message = message,
                data = null,
            )
        }

        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data,
            )
        }

        fun failed(message: String): ApiResponse<Unit> {
            return ApiResponse(
                success = false,
                message = message,
                data = null,
            )
        }

        fun <T> failed(message: String, data: T): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = data,
            )
        }
    }
}
