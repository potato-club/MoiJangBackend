package com.moijang.moijangbackend.controller

import com.moijang.moijangbackend.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(name = "Schedule API")
@RestController
@RequestMapping("/api/v1/schedules")
class NotificationController {

    @Operation(summary = "새로운 일정 등록")
    @PostMapping
    fun postSchedule(
        @Valid @RequestBody body: PostScheduleRequest
    ): PostScheduleResponse {
        return PostScheduleResponse(
            scheduleId = 0,
            message = "일정이 등록되었습니다"
        )
    }

    @Operation(summary = "일정 목록 조회")
    @GetMapping
    fun getSchedule(
        @RequestParam(name = "year") year: Int,
        @RequestParam(name = "month") month: Int,
    ): GetScheduleResponse {
        println("$year-$month")
        return GetScheduleResponse(
            schedules = List(1, {
                Schedule(
                    scheduleId = 0,
                    title = "감자볶음밥 회의",
                    categoryColor = "#FF0000",
                    isRepeating = true,
                    date = null,
                    dayOfWeek = "MONDAY",
                    startTime = "13:00",
                    endTime = "14:00"
                )
            })
        )
    }

    @Operation(summary = "일정 수정")
    @PutMapping("/{id}")
    fun updateSchedule(
        @PathVariable id: Long,
        @RequestBody body: PostScheduleRequest,
    ): ApiResponse<Unit> {
        println("$body.title")
        return ApiResponse.success("일정이 수정되었습니다.")
    }
}