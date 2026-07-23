package com.moijang.moijangbackend.schedule.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.moijang.moijangbackend.team.entity.RoomType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class PostScheduleRequest(
    @field:NotBlank(message = "제목이 누락되었습니다.")
    @Schema(title = "일정 제목", defaultValue = "감자전 부치는 날")
    val title: String,

    @field:NotBlank(message = "카테고리 색깔이 누락되었습니다")
    @Schema(title = "일정 카테고리 색깔", defaultValue = "#FF00FF")
    val categoryColor: String,

    @Schema(
        title = "일정 유형",
        description = "반복 일정일 때 true, 단일 일정일 때 false",
        defaultValue = "false",
    )
    @get:JsonProperty("isRepeating")
    @param:JsonProperty("isRepeating")
    val isRepeating: Boolean,

    @Schema(
        title = "날짜",
        description = "단일 일정에만 필요합니다",
        defaultValue = "2026-07-01",
    )
    val date: String? = null,

    @Schema(
        title = "요일",
        description = "반복 일정에만 필요합니다",
    )
    val dayOfWeek: String? = null,

    @field:NotBlank(message = "시작 시간이 누락되었습니다")
    @Schema(title = "시작 시간", defaultValue = "13:00")
    val startTime: String,

    @field:NotBlank(message = "종료 시간이 누락되었습니다")
    @Schema(title = "종료 시간", defaultValue = "14:00")
    val endTime: String,
)

data class PostScheduleResponse(
    @Schema(title = "생성된 일정 ID")
    val scheduleId: Long,
)

data class ScheduleResponse(
    @Schema(title = "일정 ID")
    val scheduleId: Long,

    @Schema(title = "일정 제목")
    val title: String,

    @Schema(title = "일정 카테고리 색깔")
    val categoryColor: String,

    @Schema(title = "일정 유형", description = "반복 일정일 때 true, 단일 일정일 때 false")
    @get:JsonProperty("isRepeating")
    val isRepeating: Boolean,

    @Schema(title = "날짜", description = "단일 일정에만 필요합니다")
    val date: String?,

    @Schema(title = "요일", description = "반복 일정에만 필요합니다")
    val dayOfWeek: String?,

    @Schema(title = "시작 시간")
    val startTime: String,

    @Schema(title = "종료 시간")
    val endTime: String,

    @Schema(title = "팀 확정 일정 원본 팀 ID")
    val sourceTeamId: Long?,
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
    @field:NotBlank(message = "확정 날짜가 누락되었습니다.")
    val confirmedDate: String,

    @field:NotBlank(message = "시작 시간이 누락되었습니다.")
    val startTime: String,

    @field:NotBlank(message = "종료 시간이 누락되었습니다.")
    val endTime: String,

    @field:NotBlank(message = "일정 제목이 누락되었습니다.")
    val eventTitle: String,
)
