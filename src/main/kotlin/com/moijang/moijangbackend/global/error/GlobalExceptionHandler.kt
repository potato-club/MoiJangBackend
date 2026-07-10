package com.moijang.moijangbackend.global.error

import com.moijang.moijangbackend.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(exception: BusinessException): ResponseEntity<ApiResponse.Failure> {
        return ResponseEntity
            .status(exception.errorCode.status)
            .body(
                ApiResponse.Failure(
                    errorCode = exception.errorCode.name,
                    errorMessage = exception.message,
                ),
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ApiResponse.Failure> {
        return ResponseEntity
            .badRequest()
            .body(
                ApiResponse.Failure(
                    errorCode = ErrorCode.INVALID_REQUEST.name,
                    errorMessage = exception.message ?: ErrorCode.INVALID_REQUEST.message,
                ),
            )
    }
}
