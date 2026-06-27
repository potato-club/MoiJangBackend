package com.moijang.moijangbackend.controller

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
    fun getUserProfile(): GetMyDataResponse {
        val id: Long = 0
        return GetMyDataResponse(
            userId = id, email = "test-user@gmail.com", name = "하영"
        )
    }
}