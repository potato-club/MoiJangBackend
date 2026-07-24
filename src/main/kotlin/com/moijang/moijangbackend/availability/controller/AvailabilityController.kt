package com.moijang.moijangbackend.availability.controller

import com.moijang.moijangbackend.availability.dto.AvailabilitySlotRequest
import com.moijang.moijangbackend.availability.dto.AvailabilitySummaryResponse
import com.moijang.moijangbackend.availability.service.AvailabilityService
import com.moijang.moijangbackend.global.auth.CurrentUser
import com.moijang.moijangbackend.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Availability API")
@RestController
@RequestMapping("/api/v1/teams/{teamId}/availabilities")
class AvailabilityController(
    private val availabilityService: AvailabilityService,
) {

    @Operation(summary = "내 희망 시간 전체 교체")
    @PutMapping
    fun replaceMyAvailabilities(
        @PathVariable teamId: Long,
        @Valid @RequestBody body: List<@Valid AvailabilitySlotRequest>,
    ): ApiResponse.Ok {
        availabilityService.replaceMyAvailabilities(CurrentUser.id(), teamId, body)
        return ApiResponse.Ok("희망 시간이 성공적으로 저장되었습니다.")
    }

    @Operation(summary = "팀 희망 시간 요약 조회")
    @GetMapping
    fun getAvailabilitySummary(
        @PathVariable teamId: Long,
    ): ApiResponse.Success<AvailabilitySummaryResponse> {
        return ApiResponse.Success(
            data = availabilityService.getAvailabilitySummary(CurrentUser.id(), teamId),
        )
    }
}
