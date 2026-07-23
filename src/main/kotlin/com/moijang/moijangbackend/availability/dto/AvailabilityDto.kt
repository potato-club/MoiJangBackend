package com.moijang.moijangbackend.availability.dto

import com.moijang.moijangbackend.team.entity.RoomType

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
