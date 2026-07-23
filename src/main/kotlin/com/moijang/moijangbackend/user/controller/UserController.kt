package com.moijang.moijangbackend.user.controller

import com.moijang.moijangbackend.global.auth.CurrentUser
import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.user.dto.GetMyDataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * [소유: 유저/친구/알림 담당]
 * stub. UserService 연동은 담당자가 구현한다.
 */
@Tag(name = "User API")
@RestController
@RequestMapping("/api/v1/users")
class UserController {

    @Operation(summary = "내 프로필 정보 조회")
    @GetMapping("/me")
    fun getUserProfile(): ApiResponse.Success<GetMyDataResponse> {
        return ApiResponse.Success(
            data = GetMyDataResponse(
                userId = CurrentUser.id(),
                email = "test-user@gmail.com",
                name = "홍길동",
            ),
        )
    }
}
