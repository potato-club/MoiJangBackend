package com.moijang.moijangbackend.user.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.user.dto.GetMyDataResponse
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
        val id: Long = 0
        return ApiResponse.Success(
            GetMyDataResponse(
                id,
                "test-user@gmail.com",
                "하영"
            )
        )
    }
}