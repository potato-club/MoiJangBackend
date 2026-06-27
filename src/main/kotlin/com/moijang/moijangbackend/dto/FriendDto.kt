package com.moijang.moijangbackend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RequestFriendRequest(
    @field:NotBlank(message = "이메일이 누락되었습니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:Schema(description = "친구 이메일", example = "moi-jang-example@gmail.com")
    val friendEmail: String,
)

data class FriendsRequest(
    @field:NotBlank(message = "커서가 누락되었습니다")
    val cursor: Int,
)

data class Friend(
    val friendId: Long,
    val name: String,
    val email: String,
)

data class FriendsResponse(
    val friends: List<Friend>
)