package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@Transactional
class MergedScheduleServiceTest {

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
    fun `단기 팀은 단발 일정과 해당 요일 반복 일정을 날짜별로 병합한다`() {
        val leader = createUser("merge-leader")
        val member = createUser("merge-member")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)
        addMember(team, member)

        createSchedule(leader, date = team.startDate, startTime = "09:00", endTime = "10:00")
        createSchedule(member, date = team.startDate, startTime = "09:30", endTime = "10:30")
        createSchedule(
            member,
            dayOfWeek = team.startDate.dayOfWeek,
            startTime = "10:00",
            endTime = "11:00",
        )

        val result = scheduleService.getMergedTeamSchedules(leader.id, team.id)

        assertEquals(RoomType.SHORT_TERM, result.roomType)
        assertEquals(2, result.mergedSchedules.size)
        val firstDate = result.mergedSchedules.first()
        assertEquals(team.startDate.toString(), firstDate.date)
        assertEquals(listOf(1, 2, 1, 1), firstDate.busyTimes.map { it.busyUserCount })
        assertEquals("09:00", firstDate.busyTimes.first().startTime)
        assertEquals("11:00", firstDate.busyTimes.last().endTime)
        assertEquals("00:00", firstDate.freeTimes.first().startTime)
        assertEquals("09:00", firstDate.freeTimes.first().endTime)
        assertEquals(emptyList<Any>(), result.mergedSchedules.last().busyTimes)
        assertEquals("00:00", result.mergedSchedules.last().freeTimes.single().startTime)
        assertEquals("24:00", result.mergedSchedules.last().freeTimes.single().endTime)
    }

    @Test
    fun `반복 팀은 요일별 busy와 free를 모두 반환한다`() {
        val leader = createUser("recurring-leader")
        val member = createUser("recurring-member")
        val team = createTeam(leader, RoomType.RECURRING)
        addMember(team, leader)
        addMember(team, member)

        createSchedule(leader, dayOfWeek = DayOfWeek.TUESDAY, startTime = "13:00", endTime = "14:00")
        createSchedule(member, dayOfWeek = DayOfWeek.TUESDAY, startTime = "13:30", endTime = "14:30")
        createSchedule(leader, date = team.startDate, startTime = "08:00", endTime = "09:00")

        val result = scheduleService.getMergedTeamSchedules(member.id, team.id)

        assertEquals(7, result.mergedSchedules.size)
        val tuesday = result.mergedSchedules.single { it.dayOfWeek == "TUESDAY" }
        assertEquals(null, tuesday.date)
        assertEquals(listOf(1, 2, 1), tuesday.busyTimes.map { it.busyUserCount })
        assertEquals("00:00", tuesday.freeTimes.first().startTime)
        assertEquals("13:00", tuesday.freeTimes.first().endTime)

        val monday = result.mergedSchedules.single { it.dayOfWeek == "MONDAY" }
        assertEquals(emptyList<Any>(), monday.busyTimes)
        assertEquals("00:00", monday.freeTimes.single().startTime)
        assertEquals("24:00", monday.freeTimes.single().endTime)
    }

    @Test
    fun `팀원이 아니면 병합 일정을 조회할 수 없다`() {
        val leader = createUser("forbidden-leader")
        val outsider = createUser("forbidden-outsider")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)

        val exception = assertThrows(BusinessException::class.java) {
            scheduleService.getMergedTeamSchedules(outsider.id, team.id)
        }

        assertEquals(ErrorCode.TEAM_FORBIDDEN, exception.errorCode)
    }

    @Test
    fun `존재하지 않는 팀의 병합 일정은 조회할 수 없다`() {
        val user = createUser("missing-team")

        val exception = assertThrows(BusinessException::class.java) {
            scheduleService.getMergedTeamSchedules(user.id, Long.MAX_VALUE)
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
                title = "병합 테스트",
                roomType = roomType,
                maxParticipants = 5,
                isPublic = false,
                passwordHash = "encoded",
                inviteCode = leader.providerId.takeLast(4),
                startDate = LocalDate.of(2026, 7, 6),
                endDate = LocalDate.of(2026, 7, 7),
            ),
        )
    }

    private fun addMember(team: Team, user: User) {
        teamUserRepository.save(TeamUser(team = team, user = user))
    }

    private fun createSchedule(
        user: User,
        date: LocalDate? = null,
        dayOfWeek: DayOfWeek? = null,
        startTime: String,
        endTime: String,
    ) {
        personalScheduleRepository.save(
            PersonalSchedule(
                user = user,
                title = "일정",
                categoryColor = "#FF0000",
                isRepeating = dayOfWeek != null,
                date = date,
                dayOfWeek = dayOfWeek,
                startTime = LocalTime.parse(startTime),
                endTime = LocalTime.parse(endTime),
            ),
        )
    }
}
