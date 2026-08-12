package com.moijang.moijangbackend.user

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun updateNickname(id: Long, newNickname: String): ApiResponse<Unit> {
        if (!UserValidation.isValidNickname(newNickname))
            throw BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 이름")
        val user =
            userRepository.findByIdOrNull(id) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND, "User not found")
        user.nickname = newNickname
        return ApiResponse.Ok("이름이 변경되었습니다")
    }
}