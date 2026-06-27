package com.moijang.moijangbackend.controller

import com.moijang.moijangbackend.dto.ApiResponse
import com.moijang.moijangbackend.dto.Friend
import com.moijang.moijangbackend.dto.FriendsRequest
import com.moijang.moijangbackend.dto.FriendsResponse
import com.moijang.moijangbackend.dto.RequestFriendRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Friend API")
@RestController
@RequestMapping("/api/v1/friends")
class FriendController {

    // 친구 요청
    @Operation(summary = "친구 요청")
    @PostMapping
    fun requestFriend(
        @Valid @RequestBody request: RequestFriendRequest
    ): ApiResponse<Unit> {
        return ApiResponse.success("친구 요청됨")
    }

    // 친구 목록 조회
    @Operation(summary = "친구 목록 조회")
    @GetMapping
    fun getFriends(
        @Valid @RequestBody request: FriendsRequest
    ): FriendsResponse {
        val friends: List<Friend> = List(
            0,
            { Friend(0, "하영", "test-user@gmail.com") }
        )
        return FriendsResponse(friends)
    }

    // 친구 삭제
    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    fun deleteFriend(
        @PathVariable friendId: Long
    ): ApiResponse<Unit> {
        return ApiResponse.success("친구 삭제 완료")
    }
}