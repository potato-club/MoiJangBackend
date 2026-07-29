package com.moijang.moijangbackend.notification.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.notification.dto.NotificationDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController {

    @GetMapping
    fun getNotifications(): ApiResponse<List<NotificationDto>> {
        val res: List<NotificationDto> = emptyList()
        return ApiResponse.Success(res)
    }
}