package com.moijang.moijangbackend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CreateTeamRequest(
    @field:NotBlank(message = "제목이 누락되었습니다.")
    @Schema(title = "팀 이름", defaultValue = "감자 요리 대회 일정 정하기")
    val title: String,
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
    val startDate: String,
    val endDate: String,
    val leaderId: Long,
    val participantIds: List<Long>,
)

data class JoinTeamRequest(
    @field:NotBlank(message = "초대 코드가 누락되었습니다.")
    val inviteCode: String,
)
data class JoinTeamResponse(
    val teamId: Long,
    val message: String,
)
