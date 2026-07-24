package com.moijang.moijangbackend.team.service

import com.moijang.moijangbackend.availability.dto.AvailabilitySlotRequest
import com.moijang.moijangbackend.availability.repository.AvailabilityRepository
import com.moijang.moijangbackend.availability.service.AvailabilityService
import com.moijang.moijangbackend.schedule.dto.ConfirmScheduleRequest
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.schedule.service.ScheduleService
import com.moijang.moijangbackend.team.dto.CreateTeamRequest
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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class TeamRelationCleanupTest {

    @Autowired
    private lateinit var teamService: TeamService

    @Autowired
    private lateinit var availabilityService: AvailabilityService

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var teamUserRepository: TeamUserRepository

    @Autowired
    private lateinit var availabilityRepository: AvailabilityRepository

    @Autowired
    private lateinit var personalScheduleRepository: PersonalScheduleRepository

    @Test
    fun `팀을 삭제하면 희망 시간과 팀 확정 일정도 함께 제거한다`() {
        val leader = createUser("delete-cleanup-leader")
        val member = createUser("delete-cleanup-member")
        val created = teamService.createTeam(leader.id, createTeamRequest())
        teamService.joinTeam(member.id, created.inviteCode, "potato123")

        availabilityService.replaceMyAvailabilities(
            member.id,
            created.teamId,
            listOf(shortTermSlot()),
        )
        scheduleService.confirmTeamSchedule(leader.id, created.teamId, confirmRequest())

        teamService.deleteTeam(leader.id, created.teamId)

        assertFalse(teamRepository.existsById(created.teamId))
        assertTrue(availabilityRepository.findAllByTeam_Id(created.teamId).isEmpty())
        assertTrue(personalScheduleRepository.findAll().none { it.sourceTeam?.id == created.teamId })
    }

    @Test
    fun `멤버를 강퇴하면 해당 멤버의 희망 시간과 팀 확정 일정만 제거한다`() {
        val leader = createUser("kick-cleanup-leader")
        val member = createUser("kick-cleanup-member")
        val created = teamService.createTeam(leader.id, createTeamRequest())
        teamService.joinTeam(member.id, created.inviteCode, "potato123")

        availabilityService.replaceMyAvailabilities(
            member.id,
            created.teamId,
            listOf(shortTermSlot()),
        )
        scheduleService.confirmTeamSchedule(leader.id, created.teamId, confirmRequest())

        teamService.kickMember(leader.id, created.teamId, member.id)

        assertFalse(teamUserRepository.existsByTeam_IdAndUser_Id(created.teamId, member.id))
        assertTrue(availabilityRepository.findAllByTeam_IdAndUser_Id(created.teamId, member.id).isEmpty())
        val confirmed = personalScheduleRepository.findAll().filter { it.sourceTeam?.id == created.teamId }
        assertEquals(listOf(leader.id), confirmed.map { it.user.id })
    }

    private fun createUser(key: String): User {
        return userRepository.save(
            User(
                email = "$key@gmail.com",
                nickname = key,
                provider = OAuthProvider.GOOGLE,
                providerId = key,
                role = UserRole.USER,
            ),
        )
    }

    private fun createTeamRequest(): CreateTeamRequest {
        return CreateTeamRequest(
            title = "정합성 테스트",
            roomType = RoomType.SHORT_TERM,
            maxParticipants = 5,
            isPublic = false,
            password = "potato123",
            startDate = "2026-07-01",
            endDate = "2026-07-31",
        )
    }

    private fun shortTermSlot(): AvailabilitySlotRequest {
        return AvailabilitySlotRequest(
            date = "2026-07-10",
            startTime = "13:00",
            endTime = "14:00",
        )
    }

    private fun confirmRequest(): ConfirmScheduleRequest {
        return ConfirmScheduleRequest(
            confirmedDate = "2026-07-10",
            startTime = "13:00",
            endTime = "14:00",
            eventTitle = "확정 약속",
        )
    }
}
