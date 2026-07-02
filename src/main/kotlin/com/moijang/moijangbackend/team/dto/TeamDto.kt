package com.moijang.moijangbackend.team.dto

import com.moijang.moijangbackend.team.entity.RoomType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateTeamRequest(
    @field:NotBlank(message = "제목이 누락되었습니다.")
    @Schema(title = "팀 이름", defaultValue = "감자 요리 대회 일정 정하기")
    val title: String,

    @field:NotNull(message = "팀방 유형이 누락되었습니다.")
    @Schema(title = "팀방 유형", defaultValue = "SHORT_TERM")
    val roomType: RoomType,

    @field:Min(2, message = "최대 참여 인원은 2명 이상이어야 합니다.")
    @Schema(title = "최대 참여 인원", defaultValue = "10")
    val maxParticipants: Int,

    @field:NotNull(message = "공개 여부가 누락되었습니다.")
    @Schema(title = "공개 여부", description = "true: 공개, false: 비공개", defaultValue = "false")
    val isPublic: Boolean,

    @field:NotBlank(message = "비밀번호가 누락되었습니다.")
    @Schema(title = "팀방 비밀번호", defaultValue = "potato123")
    val password: String,

    @field:NotBlank(message = "시작일이 누락되었습니다.")
    @Schema(title = "시작일", defaultValue = "2026-07-01")
    val startDate: String,

    @field:NotBlank(message = "종료일이 누락되었습니다.")
    @Schema(title = "종료일", defaultValue = "2026-07-31")
    val endDate: String,
)

data class CreateTeamResponse(
    val teamId: Long,
    val inviteCode: String,
    val message: String,
)

data class TeamsResponse(
    val teamId: Long,
    val title: String,
    val roomType: RoomType,
    val isPublic: Boolean,
    val startDate: String,
    val endDate: String,
    val leaderId: Long,
    val participantIds: List<Long>,
)

data class JoinTeamRequest(
    @field:NotBlank(message = "초대 코드가 누락되었습니다.")
    val inviteCode: String,

    @field:NotBlank(message = "비밀번호가 누락되었습니다.")
    val password: String,
)

data class JoinTeamResponse(
    val teamId: Long,
    val message: String,
)

data class BusyTime(
    val startTime: String,
    val endTime: String,
    val busyUserCount: Int,
)

data class MergedSchedule(
    val date: String?,
    val dayOfWeek: String?,
    val busyTimes: List<BusyTime>,
)

data class MergedScheduleResponse(
    val teamId: Long,
    val roomType: RoomType,
    val mergedSchedules: List<MergedSchedule>,
)

data class ConfirmScheduleRequest(
    val confirmDate: String,
    val startTime: String,
    val endTime: String,
    val eventTitle: String,
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
