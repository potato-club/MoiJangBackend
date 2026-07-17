package com.moijang.moijangbackend.user.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.user.dto.GetMyDataResponse
import com.moijang.moijangbackend.user.service.UserService
import com.moijang.moijangbackend.user.validation.GoogleUserSchema
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.ObjectMapper

@Tag(name = "User API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService, private val objectMapper: ObjectMapper) {

    @Operation(summary = "내 프로필 정보 조회")
    @GetMapping("/me")
    fun getUserProfile(@AuthenticationPrincipal oauth2User: OAuth2User?): ApiResponse<GetMyDataResponse> {
        if (oauth2User == null) {
            return ApiResponse.Failure("AUTH_ERROR", "사용자 정보가 없습니다")
        }
        return try {
            val validatedUser = objectMapper.convertValue(oauth2User.attributes, GoogleUserSchema::class.java)
            val res = GoogleUserSchema.from(validatedUser)

            // 2. 검증이 완료된 안전한 데이터를 반환
            ApiResponse.Success(res)
        } catch (e: IllegalArgumentException) {
            println(e.toString())
            ApiResponse.Failure("AUTH_ERROR", e.localizedMessage)
        }
    }

    @PostMapping("/rename")
    fun updateNickname(@RequestBody id: Long, @RequestBody newNickname: String): ApiResponse<Unit> {
        return userService.updateNickname(id, newNickname)
    }
}