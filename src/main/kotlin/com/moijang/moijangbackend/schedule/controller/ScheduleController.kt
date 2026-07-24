package com.moijang.moijangbackend.schedule.controller

import com.moijang.moijangbackend.global.auth.CurrentUser
import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.schedule.dto.ConfirmScheduleRequest
import com.moijang.moijangbackend.schedule.dto.MergedScheduleResponse
import com.moijang.moijangbackend.schedule.dto.PostScheduleRequest
import com.moijang.moijangbackend.schedule.dto.PostScheduleResponse
import com.moijang.moijangbackend.schedule.dto.ScheduleResponse
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

    @Operation(summary = "새로운 일정 등록")
    @PostMapping
    fun postSchedule(
        @Valid @RequestBody body: PostScheduleRequest,
    ): ApiResponse.Success<PostScheduleResponse> {
        return ApiResponse.Success(
            data = scheduleService.createSchedule(CurrentUser.id(), body),
            message = "일정이 성공적으로 등록되었습니다.",
        )
    }

    @Operation(summary = "일정 목록 조회")
    @GetMapping
    fun getSchedule(
        @RequestParam(name = "year") year: Int,
        @RequestParam(name = "month") month: Int,
    ): ApiResponse.Success<List<ScheduleResponse>> {
        return ApiResponse.Success(data = scheduleService.getSchedules(CurrentUser.id(), year, month))
    }

    @Operation(summary = "팀원 일정 병합 조회")
    @GetMapping("/teams/{teamId}/merged")
    fun getMergedTeamSchedules(
        @PathVariable teamId: Long,
    ): ApiResponse.Success<MergedScheduleResponse> {
        return ApiResponse.Success(
            data = scheduleService.getMergedTeamSchedules(CurrentUser.id(), teamId),
        )
    }

    @Operation(summary = "팀 약속 확정")
    @PostMapping("/teams/{teamId}/confirm")
    fun confirmTeamSchedule(
        @PathVariable teamId: Long,
        @Valid @RequestBody body: ConfirmScheduleRequest,
    ): ApiResponse.Ok {
        scheduleService.confirmTeamSchedule(CurrentUser.id(), teamId, body)
        return ApiResponse.Ok("약속이 성공적으로 확정되었습니다.")
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{id}")
    fun updateSchedule(
        @PathVariable id: Long,
        @Valid @RequestBody body: PostScheduleRequest,
    ): ApiResponse.Ok {
        scheduleService.updateSchedule(CurrentUser.id(), id, body)
        return ApiResponse.Ok("일정이 성공적으로 수정되었습니다.")
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/{id}")
    fun deleteSchedule(
        @PathVariable id: Long,
    ): ApiResponse.Ok {
        scheduleService.deleteSchedule(CurrentUser.id(), id)
        return ApiResponse.Ok("일정이 성공적으로 삭제되었습니다.")
    }
}
