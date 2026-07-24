package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.schedule.dto.ConfirmScheduleRequest
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.team.entity.RoomType
import com.moijang.moijangbackend.team.entity.Team
import com.moijang.moijangbackend.team.entity.TeamUser
import com.moijang.moijangbackend.team.repository.TeamRepository
import com.moijang.moijangbackend.team.repository.TeamUserRepository
import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.entity.UserRole
import com.moijang.moijangbackend.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@Transactional
class ScheduleConfirmServiceTest {

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var teamUserRepository: TeamUserRepository

    @Autowired
    private lateinit var personalScheduleRepository: PersonalScheduleRepository

    @Test
    fun `방장이 확정하면 반복 팀도 모든 멤버에게 단발 일정이 생성된다`() {
        val leader = createUser("confirm-leader")
        val member = createUser("confirm-member")
        val team = createTeam(leader, RoomType.RECURRING)
        addMember(team, leader)
        addMember(team, member)

        scheduleService.confirmTeamSchedule(
            leader.id,
            team.id,
            confirmRequest(eventTitle = "주간 회의"),
        )

        val schedules = confirmedSchedules(team.id)
        assertEquals(2, schedules.size)
        assertEquals(setOf(leader.id, member.id), schedules.map { it.user.id }.toSet())
        schedules.forEach { schedule ->
            assertFalse(schedule.isRepeating)
            assertEquals(LocalDate.of(2026, 7, 10), schedule.date)
            assertEquals(null, schedule.dayOfWeek)
            assertEquals("#4A90E2", schedule.categoryColor)
            assertEquals(team.id, schedule.sourceTeam?.id)
        }
    }

    @Test
    fun `동일한 팀 일정 재확정은 기존 일정을 교체한다`() {
        val leader = createUser("replace-leader")
        val member = createUser("replace-member")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)
        addMember(team, member)

        scheduleService.confirmTeamSchedule(leader.id, team.id, confirmRequest(eventTitle = "기존 약속"))
        scheduleService.confirmTeamSchedule(leader.id, team.id, confirmRequest(eventTitle = "변경된 약속"))

        val schedules = confirmedSchedules(team.id)
        assertEquals(2, schedules.size)
        assertEquals(setOf("변경된 약속"), schedules.map { it.title }.toSet())
    }

    @Test
    fun `방장이 아니면 팀 일정을 확정할 수 없다`() {
        val leader = createUser("permission-leader")
        val member = createUser("permission-member")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)
        addMember(team, member)

        val exception = assertThrows(BusinessException::class.java) {
            scheduleService.confirmTeamSchedule(member.id, team.id, confirmRequest())
        }

        assertEquals(ErrorCode.TEAM_FORBIDDEN, exception.errorCode)
        assertEquals(0, confirmedSchedules(team.id).size)
    }

    @Test
    fun `존재하지 않는 팀 일정은 확정할 수 없다`() {
        val user = createUser("confirm-missing")

        val exception = assertThrows(BusinessException::class.java) {
            scheduleService.confirmTeamSchedule(user.id, Long.MAX_VALUE, confirmRequest())
        }

        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.errorCode)
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

    private fun createTeam(leader: User, roomType: RoomType): Team {
        return teamRepository.save(
            Team(
                leader = leader,
                title = "확정 테스트",
                roomType = roomType,
                maxParticipants = 5,
                isPublic = false,
                passwordHash = "encoded",
                inviteCode = leader.providerId.takeLast(4),
                startDate = LocalDate.of(2026, 7, 1),
                endDate = LocalDate.of(2026, 7, 31),
            ),
        )
    }

    private fun addMember(team: Team, user: User) {
        teamUserRepository.save(TeamUser(team = team, user = user))
    }

    private fun confirmRequest(eventTitle: String = "확정 약속"): ConfirmScheduleRequest {
        return ConfirmScheduleRequest(
            confirmedDate = "2026-07-10",
            startTime = "13:00",
            endTime = "14:00",
            eventTitle = eventTitle,
        )
    }

    private fun confirmedSchedules(teamId: Long) = personalScheduleRepository.findAll()
        .filter { it.sourceTeam?.id == teamId }
}
