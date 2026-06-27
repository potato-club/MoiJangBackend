package com.moijang.moijangbackend.controller

import com.moijang.moijangbackend.dto.ApiResponse
import com.moijang.moijangbackend.dto.CreateTeamRequest
import com.moijang.moijangbackend.dto.CreateTeamResponse
import com.moijang.moijangbackend.dto.JoinTeamRequest
import com.moijang.moijangbackend.dto.JoinTeamResponse
import com.moijang.moijangbackend.dto.TeamsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
class TeamController {

    @Operation(summary = "팀 생성")
    @PostMapping
    fun createTeam(@Valid @RequestBody req: CreateTeamRequest): CreateTeamResponse {
        // 테스트 DB 중복 조회 (랜덤으로 중복됐다고 함)
        val db = object {
            fun has(code: String): Boolean {
                return if (Math.random() < 0.3) {
                    true
                } else {
                    false
                }
            }
        }
        var inviteCode: String
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ" // I, O, 1, 0, L은 헷갈려서 제외
        do {
            inviteCode = (1..4).map { chars.random() }.joinToString("")
        } while (db.has(inviteCode))
        return CreateTeamResponse(
            teamId = 0,
            inviteCode,
            message = "방이 생성되었습니다",
        )
    }

    @Operation(summary = "팀 목록 조회")
    @GetMapping("/{id}")
    fun getTeam(@PathVariable id: Long): ResponseEntity<TeamsResponse> {
        val res = TeamsResponse(
            teamId = id,
            title = "모이장 1차 회의",
            startDate = "2026-06-01",
            endDate = "2026-06-30",
            leaderId = 1,
            participantIds = List(0, { i -> i.toLong() })
        )
        return ResponseEntity.ok(res)
    }

    @Operation(summary = "팀 삭제")
    @DeleteMapping("/{id}")
    fun deleteTeam(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        if (true) {
            val res = ApiResponse.success("방 삭제 완료")
            return ResponseEntity.ok(res)
        } else {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failed("방 삭제 권한 부족"))
        }
    }

    @Operation(summary = "팀 가입")
    @PostMapping("/join")
    fun joinTeam(@Valid @RequestBody body: JoinTeamRequest): JoinTeamResponse {
        return JoinTeamResponse(
            teamId = 0,
            message = "가입되었습니다"
        )
    }

    @DeleteMapping("/{teamId}/kick/{id}")
    fun kickUserFromTeam(
        @PathVariable teamId: Long,
        @PathVariable id: Long
    ): ApiResponse<Unit> {
        return ApiResponse.success("해당 사용자를 강퇴했습니다")
    }
}