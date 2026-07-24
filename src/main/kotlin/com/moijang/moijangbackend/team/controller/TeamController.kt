package com.moijang.moijangbackend.team.controller

import com.moijang.moijangbackend.global.auth.CurrentUser
import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.team.dto.CreateTeamRequest
import com.moijang.moijangbackend.team.dto.CreateTeamResponse
import com.moijang.moijangbackend.team.dto.InviteCodeResponse
import com.moijang.moijangbackend.team.dto.JoinTeamResponse
import com.moijang.moijangbackend.team.dto.TeamsResponse
import com.moijang.moijangbackend.team.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Team API")
@RestController
@RequestMapping("/api/v1/teams")
class TeamController(
    private val teamService: TeamService,
) {

    @Operation(summary = "팀 생성")
    @PostMapping
    fun createTeam(@Valid @RequestBody req: CreateTeamRequest): ApiResponse.Success<CreateTeamResponse> {
        return ApiResponse.Success(
            data = teamService.createTeam(CurrentUser.id(), req),
            message = "방이 성공적으로 생성되었습니다.",
        )
    }

    @Operation(summary = "팀 조회")
    @GetMapping("/{teamId}")
    fun getTeam(@PathVariable teamId: Long): ApiResponse.Success<TeamsResponse> {
        return ApiResponse.Success(data = teamService.getTeam(teamId))
    }

    @Operation(summary = "팀 삭제")
    @DeleteMapping("/{teamId}")
    fun deleteTeam(@PathVariable teamId: Long): ApiResponse.Ok {
        teamService.deleteTeam(CurrentUser.id(), teamId)
        return ApiResponse.Ok("방 삭제 완료")
    }

    @Operation(summary = "초대 코드로 팀 가입")
    @PostMapping("/join")
    fun joinTeam(
        @RequestParam(name = "code") code: String,
        @RequestParam(name = "password") password: String,
    ): ApiResponse.Success<JoinTeamResponse> {
        return ApiResponse.Success(
            data = teamService.joinTeam(CurrentUser.id(), code, password),
            message = "방에 성공적으로 참여했습니다.",
        )
    }

    @Operation(summary = "초대 코드 재발급")
    @PostMapping("/{teamId}/invite-code")
    fun reissueInviteCode(
        @PathVariable teamId: Long,
    ): ApiResponse.Success<InviteCodeResponse> {
        return ApiResponse.Success(
            data = teamService.reissueInviteCode(CurrentUser.id(), teamId),
            message = "초대 코드가 재발급되었습니다.",
        )
    }

    @Operation(summary = "팀 멤버 강퇴")
    @DeleteMapping("/{teamId}/kick/{userId}")
    fun kickUserFromTeam(
        @PathVariable teamId: Long,
        @PathVariable userId: Long,
    ): ApiResponse.Ok {
        teamService.kickMember(CurrentUser.id(), teamId, userId)
        return ApiResponse.Ok("해당 유저를 강퇴 처리했습니다.")
    }
}
