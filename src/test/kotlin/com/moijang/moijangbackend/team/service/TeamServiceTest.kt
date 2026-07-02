package com.moijang.moijangbackend.team.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.team.dto.CreateTeamRequest
import com.moijang.moijangbackend.team.dto.JoinTeamRequest
import com.moijang.moijangbackend.team.entity.RoomType
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.entity.UserRole
import com.moijang.moijangbackend.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class TeamServiceTest {

    @Autowired
    private lateinit var teamService: TeamService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var teamUserRepository: TeamUserRepository

    private lateinit var leader: User
    private lateinit var member: User

    @BeforeEach
    fun setUp() {
        leader = userRepository.save(
            User(
                email = "leader@gmail.com",
                nickname = "방장",
                provider = OAuthProvider.GOOGLE,
                providerId = "leader",
                role = UserRole.USER,
            ),
        )
        member = userRepository.save(
            User(
                email = "member@gmail.com",
                nickname = "멤버",
                provider = OAuthProvider.GOOGLE,
                providerId = "member",
                role = UserRole.USER,
            ),
        )
    }

    @Test
    fun `팀을 생성하면 방장이 참여자로 등록된다`() {
        val response = teamService.createTeam(leader.id, createTeamRequest())

        val team = teamRepository.findById(response.teamId).orElseThrow()
        assertEquals(4, team.inviteCode.length)
        assertTrue(teamUserRepository.existsByTeam_IdAndUser_Id(team.id, leader.id))
    }

    @Test
    fun `올바른 비밀번호로 팀에 가입할 수 있다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest(maxParticipants = 3))

        val joined = teamService.joinTeam(
            member.id,
            JoinTeamRequest(
                inviteCode = created.inviteCode,
                password = "potato123",
            ),
        )

        assertEquals(created.teamId, joined.teamId)
        assertTrue(teamUserRepository.existsByTeam_IdAndUser_Id(created.teamId, member.id))
    }

    @Test
    fun `비밀번호가 틀리면 가입할 수 없다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest())

        val exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException::class.java) {
            teamService.joinTeam(
                member.id,
                JoinTeamRequest(
                    inviteCode = created.inviteCode,
                    password = "wrong-password",
                ),
            )
        }

        assertEquals(ErrorCode.TEAM_PASSWORD_MISMATCH, exception.errorCode)
    }

    @Test
    fun `정원이 가득 찬 팀에는 가입할 수 없다`() {
        val created = teamService.createTeam(
            leader.id,
            createTeamRequest(maxParticipants = 2),
        )
        teamService.joinTeam(
            member.id,
            JoinTeamRequest(
                inviteCode = created.inviteCode,
                password = "potato123",
            ),
        )

        val extraUser = userRepository.save(
            User(
                email = "extra@gmail.com",
                nickname = "추가",
                provider = OAuthProvider.GOOGLE,
                providerId = "extra",
                role = UserRole.USER,
            ),
        )

        val exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException::class.java) {
            teamService.joinTeam(
                extraUser.id,
                JoinTeamRequest(
                    inviteCode = created.inviteCode,
                    password = "potato123",
                ),
            )
        }

        assertEquals(ErrorCode.TEAM_FULL, exception.errorCode)
    }

    @Test
    fun `방장만 팀을 삭제할 수 있다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest())

        teamService.deleteTeam(leader.id, created.teamId)

        assertFalse(teamRepository.existsById(created.teamId))
    }

    @Test
    fun `방장이 아니면 팀을 삭제할 수 없다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest())

        val exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException::class.java) {
            teamService.deleteTeam(member.id, created.teamId)
        }

        assertEquals(ErrorCode.TEAM_FORBIDDEN, exception.errorCode)
    }

    @Test
    fun `방장은 멤버를 강퇴할 수 있다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest(maxParticipants = 3))
        teamService.joinTeam(
            member.id,
            JoinTeamRequest(
                inviteCode = created.inviteCode,
                password = "potato123",
            ),
        )

        teamService.kickMember(leader.id, created.teamId, member.id)

        assertFalse(teamUserRepository.existsByTeam_IdAndUser_Id(created.teamId, member.id))
    }

    @Test
    fun `방장은 강퇴할 수 없다`() {
        val created = teamService.createTeam(leader.id, createTeamRequest())

        val exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException::class.java) {
            teamService.kickMember(leader.id, created.teamId, leader.id)
        }

        assertEquals(ErrorCode.CANNOT_KICK_LEADER, exception.errorCode)
    }

    @Test
    fun `종료일이 시작일보다 빠르면 팀을 생성할 수 없다`() {
        val exception = org.junit.jupiter.api.Assertions.assertThrows(BusinessException::class.java) {
            teamService.createTeam(
                leader.id,
                createTeamRequest(
                    startDate = "2026-07-31",
                    endDate = "2026-07-01",
                ),
            )
        }

        assertEquals(ErrorCode.INVALID_TEAM_PERIOD, exception.errorCode)
    }

    private fun createTeamRequest(
        maxParticipants: Int = 10,
        startDate: String = "2026-07-01",
        endDate: String = "2026-07-31",
    ): CreateTeamRequest {
        return CreateTeamRequest(
            title = "모이장 회의",
            roomType = RoomType.SHORT_TERM,
            maxParticipants = maxParticipants,
            isPublic = false,
            password = "potato123",
            startDate = startDate,
            endDate = endDate,
        )
    }
}
