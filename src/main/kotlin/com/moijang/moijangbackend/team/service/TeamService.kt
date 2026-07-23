package com.moijang.moijangbackend.team.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.team.dto.CreateTeamRequest
import com.moijang.moijangbackend.team.dto.CreateTeamResponse
import com.moijang.moijangbackend.team.dto.JoinTeamResponse
import com.moijang.moijangbackend.team.dto.TeamParticipantResponse
import com.moijang.moijangbackend.team.dto.TeamsResponse
import com.moijang.moijangbackend.team.entity.Team
import com.moijang.moijangbackend.team.entity.TeamUser
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamUserRepository: TeamUserRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun createTeam(userId: Long, request: CreateTeamRequest): CreateTeamResponse {
        val leader = findUser(userId)
        val startDate = LocalDate.parse(request.startDate)
        val endDate = LocalDate.parse(request.endDate)
        validateTeamPeriod(startDate, endDate)

        val inviteCode = generateUniqueInviteCode()
        val team = teamRepository.save(
            Team(
                leader = leader,
                title = request.title,
                roomType = request.roomType,
                maxParticipants = request.maxParticipants,
                isPublic = request.isPublic,
                passwordHash = requireNotNull(passwordEncoder.encode(request.password)),
                inviteCode = inviteCode,
                startDate = startDate,
                endDate = endDate,
            ),
        )

        teamUserRepository.save(
            TeamUser(
                team = team,
                user = leader,
            ),
        )

        return CreateTeamResponse(
            teamId = team.id,
            inviteCode = team.inviteCode,
        )
    }

    @Transactional(readOnly = true)
    fun getTeam(teamId: Long): TeamsResponse {
        val team = findTeam(teamId)
        val participants = teamUserRepository.findAllByTeam_Id(teamId)
            .map {
                TeamParticipantResponse(
                    userId = it.user.id,
                    name = it.user.nickname,
                )
            }

        return TeamsResponse(
            teamId = team.id,
            title = team.title,
            roomType = team.roomType,
            maxParticipants = team.maxParticipants,
            isPublic = team.isPublic,
            startDate = team.startDate.toString(),
            endDate = team.endDate.toString(),
            leaderId = team.leader.id,
            participants = participants,
        )
    }

    @Transactional
    fun deleteTeam(userId: Long, teamId: Long) {
        val team = findTeam(teamId)
        assertLeader(userId, team)

        teamUserRepository.deleteAllByTeam_Id(teamId)
        teamRepository.delete(team)
    }

    @Transactional
    fun joinTeam(userId: Long, inviteCode: String): JoinTeamResponse {
        val user = findUser(userId)
        val team = teamRepository.findByInviteCode(inviteCode)
            ?: throw BusinessException(ErrorCode.TEAM_NOT_FOUND)

        if (teamUserRepository.existsByTeam_IdAndUser_Id(team.id, userId)) {
            throw BusinessException(ErrorCode.ALREADY_TEAM_MEMBER)
        }

        if (teamUserRepository.countByTeam_Id(team.id) >= team.maxParticipants) {
            throw BusinessException(ErrorCode.TEAM_FULL)
        }

        teamUserRepository.save(
            TeamUser(
                team = team,
                user = user,
            ),
        )

        return JoinTeamResponse(teamId = team.id)
    }

    @Transactional
    fun kickMember(leaderId: Long, teamId: Long, targetUserId: Long) {
        val team = findTeam(teamId)
        assertLeader(leaderId, team)

        if (targetUserId == team.leader.id) {
            throw BusinessException(ErrorCode.CANNOT_KICK_LEADER)
        }

        if (!teamUserRepository.existsByTeam_IdAndUser_Id(teamId, targetUserId)) {
            throw BusinessException(ErrorCode.TEAM_NOT_FOUND, "팀 멤버를 찾을 수 없습니다")
        }

        teamUserRepository.deleteByTeam_IdAndUser_Id(teamId, targetUserId)
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            BusinessException(ErrorCode.USER_NOT_FOUND)
        }
    }

    private fun findTeam(teamId: Long): Team {
        return teamRepository.findById(teamId).orElseThrow {
            BusinessException(ErrorCode.TEAM_NOT_FOUND)
        }
    }

    private fun assertLeader(userId: Long, team: Team) {
        if (team.leader.id != userId) {
            throw BusinessException(ErrorCode.TEAM_FORBIDDEN)
        }
    }

    private fun validateTeamPeriod(startDate: LocalDate, endDate: LocalDate) {
        if (endDate.isBefore(startDate)) {
            throw BusinessException(ErrorCode.INVALID_TEAM_PERIOD)
        }
    }

    private fun generateUniqueInviteCode(): String {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        var inviteCode: String
        do {
            inviteCode = (1..4).map { chars.random() }.joinToString("")
        } while (teamRepository.existsByInviteCode(inviteCode))
        return inviteCode
    }
}
