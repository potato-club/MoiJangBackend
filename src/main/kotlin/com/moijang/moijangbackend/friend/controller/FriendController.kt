package com.moijang.moijangbackend.friend.controller

import com.moijang.moijangbackend.friend.dto.Friend
import com.moijang.moijangbackend.global.auth.CurrentUser
import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.util.Validator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * [소유: 유저/친구/알림 담당]
 * stub. Service·Entity 연동은 담당자가 구현한다.
 */
@Tag(name = "Friend API")
@RestController
@RequestMapping("/api/v1/friends")
class FriendController {

    private val stubFriends = listOf(
        Friend(2, "김철수", "chulsoo@gmail.com"),
        Friend(3, "이영희", "younghee@gmail.com"),
    )

    @Operation(summary = "친구 요청")
    @PostMapping
    fun requestFriend(
        @RequestParam(name = "email") email: String,
    ): ApiResponse.Ok {
        CurrentUser.id()

        if (!Validator.isEmail(email)) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "요청이 잘못되었습니다.")
        }

        return ApiResponse.Ok("친구 추가 완료")
    }

    @Operation(summary = "친구 목록 조회")
    @GetMapping
    fun getFriends(): ApiResponse.Success<List<Friend>> {
        CurrentUser.id()
        return ApiResponse.Success(data = stubFriends)
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    fun deleteFriend(
        @PathVariable friendId: Long,
    ): ApiResponse.Ok {
        CurrentUser.id()

        if (stubFriends.none { it.friendId == friendId }) {
            throw BusinessException(ErrorCode.FRIEND_NOT_FOUND)
        }

        return ApiResponse.Ok("친구 삭제 완료")
    }
}
