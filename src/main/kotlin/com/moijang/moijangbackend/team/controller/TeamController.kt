package com.moijang.moijangbackend.team.controller

import com.moijang.moijangbackend.global.common.ApiResponse
import com.moijang.moijangbackend.team.dto.CreateTeamRequest
import com.moijang.moijangbackend.team.dto.CreateTeamResponse
import com.moijang.moijangbackend.team.dto.JoinTeamRequest
import com.moijang.moijangbackend.team.dto.JoinTeamResponse
import com.moijang.moijangbackend.team.dto.TeamsResponse
import com.moijang.moijangbackend.team.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Team API")
@RestController
@RequestMapping("/api/v1/teams")
class TeamController(
    private val teamService: TeamService,
) {

    // TODO: Backend A OAuth/JWT 완료 후 @AuthenticationPrincipal AuthUser로 교체
    private fun currentUserId(): Long = TEMP_USER_ID

    @Operation(summary = "팀 생성")
    @PostMapping
    fun createTeam(@Valid @RequestBody req: CreateTeamRequest): CreateTeamResponse {
        return teamService.createTeam(currentUserId(), req)
    }

    @Operation(summary = "팀 조회")
    @GetMapping("/{teamId}")
    fun getTeam(@PathVariable teamId: Long): ResponseEntity<TeamsResponse> {
        return ResponseEntity.ok(teamService.getTeam(teamId))
    }

    @Operation(summary = "팀 삭제")
    @DeleteMapping("/{teamId}")
    fun deleteTeam(@PathVariable teamId: Long): ResponseEntity<ApiResponse<Unit>> {
        teamService.deleteTeam(currentUserId(), teamId)
        return ResponseEntity.ok(ApiResponse.Ok("방 삭제 완료"))
    }

    @Operation(summary = "팀 가입")
    @PostMapping("/join")
    fun joinTeam(@Valid @RequestBody body: JoinTeamRequest): JoinTeamResponse {
        return teamService.joinTeam(currentUserId(), body)
    }

    @Operation(summary = "팀 멤버 강퇴")
    @DeleteMapping("/{teamId}/members/{userId}")
    fun kickUserFromTeam(
        @PathVariable teamId: Long,
        @PathVariable userId: Long,
    ): ApiResponse<Unit> {
        teamService.kickMember(currentUserId(), teamId, userId)
        return ApiResponse.Ok("사용자 $userId 강퇴했습니다")
    }

    companion object {
        private const val TEMP_USER_ID = 1L
    }
}
