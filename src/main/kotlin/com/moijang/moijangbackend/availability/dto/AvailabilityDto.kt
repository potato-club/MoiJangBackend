package com.moijang.moijangbackend.availability.dto

import com.moijang.moijangbackend.team.entity.RoomType
import jakarta.validation.constraints.NotBlank

data class AvailabilitySlotRequest(
    val date: String? = null,
    val dayOfWeek: String? = null,

    @field:NotBlank(message = "시작 시간이 누락되었습니다.")
    val startTime: String,

    @field:NotBlank(message = "종료 시간이 누락되었습니다.")
    val endTime: String,
)

data class AvailabilityUserSummary(
    val userId: Long,
    val nickname: String,
)

data class AvailabilitySlotSummary(
    val date: String?,
    val dayOfWeek: String?,
    val startTime: String,
    val endTime: String,
    val selectedUsers: List<AvailabilityUserSummary>,
)

data class AvailabilitySummaryResponse(
    val teamId: Long,
    val roomType: RoomType,
    val slots: List<AvailabilitySlotSummary>,
)
