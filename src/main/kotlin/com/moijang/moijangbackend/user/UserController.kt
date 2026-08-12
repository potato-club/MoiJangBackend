package com.moijang.moijangbackend.user

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.user.dto.UserDto
import com.moijang.moijangbackend.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.ObjectMapper

@Tag(name = "User API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) {

    @Operation(summary = "내 프로필 정보 조회")
    @GetMapping("/me")
    fun getUserProfile(@AuthenticationPrincipal oauth2User: OAuth2User): ApiResponse<UserDto> {
        val userId = oauth2User.attributes["sub"] as String
        val email = oauth2User.attributes["email"] as String
        val name = oauth2User.attributes["name"] as String
        val picture = oauth2User.attributes["picture"] as String
        val userData = UserDto(userId, email, name, picture)

        return ApiResponse.Success(userData)
    }

    @Operation(summary = "이름 변경")
    @PostMapping("/rename")
    fun updateNickname(@RequestBody id: Long, @RequestBody newNickname: String): ApiResponse<Unit> {
        return userService.updateNickname(id, newNickname)
    }

    @Operation(summary = "사용자 프로필 조회 (키: 이메일)")
    @GetMapping("/{email}")
    fun getUserProfileByEmail(@PathVariable email: String): ApiResponse<UserDto> {
        val user = userRepository.findByEmail(email)
        return ApiResponse.Success(
            UserDto(
                user.id.toString(),
                user.email,
                user.nickname,
                user.picture,
            )
        )
    }
}