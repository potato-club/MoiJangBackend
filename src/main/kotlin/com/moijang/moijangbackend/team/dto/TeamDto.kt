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
    val isPublic: Boolean = false,

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
    val inviteLink: String,
)

data class TeamParticipantResponse(
    val userId: Long,
    val name: String,
)

data class TeamsResponse(
    val teamId: Long,
    val title: String,
    val roomType: RoomType,
    val maxParticipants: Int,
    val isPublic: Boolean,
    val startDate: String,
    val endDate: String,
    val leaderId: Long,
    val inviteCode: String,
    val inviteLink: String,
    val participants: List<TeamParticipantResponse>,
)

data class JoinTeamResponse(
    val teamId: Long,
)

data class InviteCodeResponse(
    val inviteCode: String,
    val inviteLink: String,
)
