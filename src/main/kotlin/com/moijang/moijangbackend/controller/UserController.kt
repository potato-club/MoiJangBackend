package com.moijang.moijangbackend.controller

import com.moijang.moijangbackend.dto.ApiResponse
import com.moijang.moijangbackend.dto.GetMyDataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User API")
@RestController
@RequestMapping("/api/v1/users")
class UserController {

    @Operation(summary = "내 프로필 정보 조회")
    @GetMapping("/me")
    fun getUserProfile(): ApiResponse<GetMyDataResponse> {
        val isLoggedIn = true
        if (isLoggedIn) {
            val id: Long = 0
            return ApiResponse.Success(
                GetMyDataResponse(
                    id,
                    "test-user@gmail.com",
                    "하영"
                )
            )
        } else {
            return ApiResponse.Failure(
                "NOT_AUTHENTICATED",
                "로그인이 필요한 서비스입니다."
            )
        }
    }
}