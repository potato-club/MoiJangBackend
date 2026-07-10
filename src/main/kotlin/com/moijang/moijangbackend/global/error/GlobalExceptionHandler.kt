package com.moijang.moijangbackend.global.error

import com.moijang.moijangbackend.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity
            .status(exception.errorCode.status)
            .body(ApiResponse.Failure(exception.errorCode.name, exception.message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.Failure("ILLEGAL_ARGUMENT", exception.message ?: ErrorCode.INVALID_REQUEST.message))
    }
}
