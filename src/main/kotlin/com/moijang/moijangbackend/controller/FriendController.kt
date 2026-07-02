package com.moijang.moijangbackend.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.dto.Friend
import com.moijang.moijangbackend.dto.FriendsRequest
import com.moijang.moijangbackend.dto.FriendsResponse
import com.moijang.moijangbackend.util.Validator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Friend API")
@RestController
@RequestMapping("/api/v1/friends")
class FriendController {

    // 친구 요청
    @Operation(summary = "친구 요청")
    @PostMapping
    fun requestFriend(
        @RequestParam(name = "email") email: String,
    ): ApiResponse<Unit> {
        if (Validator.isEmail(email)) {
            val isExistEmail = true
            if (isExistEmail) {
                return ApiResponse.Ok("친구 요청됨")
            } else {
                return ApiResponse.Failure(
                    "NotFoundError",
                    "사용자를 찾을 수 없습니다."
                )
            }
        } else {
            return ApiResponse.Failure(
                "RequestError",
                "요청이 잘못되었습니다."
            )
        }
    }

    // 친구 목록 조회
    @Operation(summary = "친구 목록 조회")
    @GetMapping
    fun getFriends(
        @Valid @RequestBody request: FriendsRequest
    ): ApiResponse<FriendsResponse> {
        val friends: List<Friend> = listOf(
            Friend(0, "하영", "hayoung@gmail.com"),
            Friend(1, "이솔", "sol@gmail.com"),
            Friend(2, "예림", "yerim@gmail.com"),
        )
        return ApiResponse.Success(FriendsResponse(friends))
    }

    // 친구 삭제
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    fun deleteFriend(
        @PathVariable friendId: Long
    ): ApiResponse<Unit> {
        val isExistId = friendId in 0L..<3L;
        if (isExistId) {
            return ApiResponse.Ok("친구 삭제 완료")
        } else {
            return ApiResponse.Failure(
                "NOT_EXIST_FRIEND",
                "해당 친구가 없습니다."
            )
        }
    }
}