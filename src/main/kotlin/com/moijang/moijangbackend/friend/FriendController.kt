package com.moijang.moijangbackend.friend

import com.moijang.moijangbackend.friend.dto.FriendDto
import com.moijang.moijangbackend.friend.repository.FriendRepository
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
class FriendController(private val friendRepository: FriendRepository) {

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
    fun getFriends(): ApiResponse.Success<List<FriendDto>> {
        val id = CurrentUser.id()
        val friends = friendRepository.findByUserId(id)
        val result = friends.map {
            FriendDto(
                it.friend.id,
                it.friend.nickname,
                it.createdAt
            )
        }
        return ApiResponse.Success(data = result)
    }

    @Operation(summary = "친구 삭제")
    @DeleteMapping("/{friendId}")
    fun deleteFriend(
        @PathVariable friendId: Long,
    ): ApiResponse.Ok {
        val userId = CurrentUser.id()
        if (!friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw BusinessException(ErrorCode.FRIEND_NOT_FOUND);
        }
        friendRepository.deleteByUserIdAndFriendId(CurrentUser.id(), friendId)

        return ApiResponse.Ok("친구 삭제함")
    }
}