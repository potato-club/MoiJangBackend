package com.moijang.moijangbackend.schedule.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.schedule.dto.GetScheduleResponse
import com.moijang.moijangbackend.schedule.dto.PostScheduleRequest
import com.moijang.moijangbackend.schedule.dto.PostScheduleResponse
import com.moijang.moijangbackend.schedule.service.ScheduleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Schedule API")
@RestController
@RequestMapping("/api/v1/schedules")
class ScheduleController(
    private val scheduleService: ScheduleService,
) {

    // TODO: Google OAuth 완료 후 @AuthenticationPrincipal AuthUser로 교체
    private fun currentUserId(): Long = TEMP_USER_ID

    @Operation(summary = "새로운 일정 등록")
    @PostMapping
    fun postSchedule(
        @Valid @RequestBody body: PostScheduleRequest,
    ): ApiResponse.Success<PostScheduleResponse> {
        val response = scheduleService.createSchedule(currentUserId(), body)
        return ApiResponse.Success(
            data = response,
            message = response.message,
        )
    }

    @Operation(summary = "일정 목록 조회")
    @GetMapping
    fun getSchedule(
        @RequestParam(name = "year") year: Int,
        @RequestParam(name = "month") month: Int,
    ): ApiResponse.Success<GetScheduleResponse> {
        return ApiResponse.Success(data = scheduleService.getSchedules(currentUserId(), year, month))
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{scheduleId}")
    fun updateSchedule(
        @PathVariable scheduleId: Long,
        @Valid @RequestBody body: PostScheduleRequest,
    ): ApiResponse.Ok {
        scheduleService.updateSchedule(currentUserId(), scheduleId, body)
        return ApiResponse.Ok("일정이 수정되었습니다.")
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{scheduleId}")
    fun deleteSchedule(
        @PathVariable scheduleId: Long,
    ): ApiResponse.Ok {
        scheduleService.deleteSchedule(currentUserId(), scheduleId)
        return ApiResponse.Ok("일정이 삭제되었습니다.")
    }

    companion object {
        private const val TEMP_USER_ID = 1L
    }
}
