package com.moijang.moijangbackend.global.common

/**
 * API 공통 응답 형식 (팀 합의 기준)
 *
 * - [Success]: 데이터가 있는 응답 (GET, POST create/join)
 * - [Ok]: 데이터 없이 성공만 알리는 응답 (PUT, DELETE)
 * - [Failure]: 실패 응답 ([com.moijang.moijangbackend.global.error.GlobalExceptionHandler]에서 사용)
 */
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