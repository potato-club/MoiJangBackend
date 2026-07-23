package com.moijang.moijangbackend.global.error

import com.moijang.moijangbackend.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse.Failure> {
        val message = exception.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: ErrorCode.INVALID_REQUEST.message

        return ResponseEntity
            .badRequest()
            .body(
                ApiResponse.Failure(
                    errorCode = ErrorCode.INVALID_REQUEST.name,
                    errorMessage = message,
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
