package com.moijang.moijangbackend.availability.service

import com.moijang.moijangbackend.availability.dto.AvailabilitySlotRequest
import com.moijang.moijangbackend.availability.repository.AvailabilityRepository
import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
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
import java.time.LocalDate

@SpringBootTest
@Transactional
class AvailabilityServiceTest {

    @Autowired
    private lateinit var availabilityService: AvailabilityService

    @Autowired
    private lateinit var availabilityRepository: AvailabilityRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var teamUserRepository: TeamUserRepository

    @Test
    fun `단기 팀 희망 시간을 슬롯별 선택 사용자와 함께 요약한다`() {
        val leader = createUser("availability-leader")
        val member = createUser("availability-member")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)
        addMember(team, member)

        availabilityService.replaceMyAvailabilities(
            leader.id,
            team.id,
            listOf(shortTermSlot("13:00", "14:00")),
        )
        availabilityService.replaceMyAvailabilities(
            member.id,
            team.id,
            listOf(
                shortTermSlot("13:00", "14:00"),
                shortTermSlot("14:00", "15:00"),
            ),
        )

        val result = availabilityService.getAvailabilitySummary(leader.id, team.id)

        assertEquals(RoomType.SHORT_TERM, result.roomType)
        assertEquals(2, result.slots.size)
        assertEquals(2, result.slots[0].selectedUsers.size)
        assertEquals(setOf(leader.id, member.id), result.slots[0].selectedUsers.map { it.userId }.toSet())
        assertEquals(listOf(member.id), result.slots[1].selectedUsers.map { it.userId })
    }

    @Test
    fun `희망 시간 저장은 사용자의 기존 슬롯 전체를 교체한다`() {
        val leader = createUser("replace-availability")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)

        availabilityService.replaceMyAvailabilities(
            leader.id,
            team.id,
            listOf(shortTermSlot("09:00", "10:00"), shortTermSlot("10:00", "11:00")),
        )
        availabilityService.replaceMyAvailabilities(
            leader.id,
            team.id,
            listOf(shortTermSlot("16:00", "17:00")),
        )

        val saved = availabilityRepository.findAllByTeam_IdAndUser_Id(team.id, leader.id)
        assertEquals(1, saved.size)
        assertEquals("16:00", saved.single().startTime.toString())
    }

    @Test
    fun `정기 팀은 요일 기준 희망 시간을 저장하고 요약한다`() {
        val leader = createUser("recurring-availability")
        val team = createTeam(leader, RoomType.RECURRING)
        addMember(team, leader)

        availabilityService.replaceMyAvailabilities(
            leader.id,
            team.id,
            listOf(
                AvailabilitySlotRequest(
                    dayOfWeek = "monday",
                    startTime = "18:00",
                    endTime = "19:00",
                ),
            ),
        )

        val result = availabilityService.getAvailabilitySummary(leader.id, team.id)

        assertEquals(1, result.slots.size)
        assertEquals(null, result.slots.single().date)
        assertEquals("MONDAY", result.slots.single().dayOfWeek)
    }

    @Test
    fun `유효하지 않은 교체 요청은 기존 희망 시간을 삭제하지 않는다`() {
        val leader = createUser("invalid-availability")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)
        availabilityService.replaceMyAvailabilities(
            leader.id,
            team.id,
            listOf(shortTermSlot("13:00", "14:00")),
        )

        assertThrows(IllegalArgumentException::class.java) {
            availabilityService.replaceMyAvailabilities(
                leader.id,
                team.id,
                listOf(
                    AvailabilitySlotRequest(
                        dayOfWeek = "MONDAY",
                        startTime = "15:00",
                        endTime = "16:00",
                    ),
                ),
            )
        }

        val saved = availabilityRepository.findAllByTeam_IdAndUser_Id(team.id, leader.id)
        assertEquals(1, saved.size)
        assertEquals("13:00", saved.single().startTime.toString())
    }

    @Test
    fun `팀원이 아니면 희망 시간을 읽거나 저장할 수 없다`() {
        val leader = createUser("availability-owner")
        val outsider = createUser("availability-outsider")
        val team = createTeam(leader, RoomType.SHORT_TERM)
        addMember(team, leader)

        val writeException = assertThrows(BusinessException::class.java) {
            availabilityService.replaceMyAvailabilities(
                outsider.id,
                team.id,
                listOf(shortTermSlot("13:00", "14:00")),
            )
        }
        val readException = assertThrows(BusinessException::class.java) {
            availabilityService.getAvailabilitySummary(outsider.id, team.id)
        }

        assertEquals(ErrorCode.TEAM_FORBIDDEN, writeException.errorCode)
        assertEquals(ErrorCode.TEAM_FORBIDDEN, readException.errorCode)
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
                title = "희망 시간 테스트",
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

    private fun shortTermSlot(startTime: String, endTime: String): AvailabilitySlotRequest {
        return AvailabilitySlotRequest(
            date = "2026-07-10",
            startTime = startTime,
            endTime = endTime,
        )
    }
}
