package com.moijang.moijangbackend.schedule.service

import com.moijang.moijangbackend.global.error.BusinessException
import com.moijang.moijangbackend.global.error.ErrorCode
import com.moijang.moijangbackend.schedule.dto.PostScheduleRequest
import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
import com.moijang.moijangbackend.schedule.repository.PersonalScheduleRepository
import com.moijang.moijangbackend.user.entity.OAuthProvider
import com.moijang.moijangbackend.user.entity.User
import com.moijang.moijangbackend.user.entity.UserRole
import com.moijang.moijangbackend.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private lateinit var scheduleService: ScheduleService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var personalScheduleRepository: PersonalScheduleRepository

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = userRepository.save(
            User(
                email = "schedule-test@gmail.com",
                nickname = "테스트",
                provider = OAuthProvider.GOOGLE,
                providerId = "schedule-test",
                role = UserRole.USER,
            ),
        )
    }

    @Test
    fun `일정을 생성하고 삭제할 수 있다`() {
        val response = scheduleService.createSchedule(
            user.id,
            PostScheduleRequest(
                title = "회의",
                categoryColor = "#FF0000",
                isRepeating = false,
                date = "2026-07-01",
                startTime = "13:00",
                endTime = "14:00",
            ),
        )

        scheduleService.deleteSchedule(user.id, response.scheduleId)

        assertThrows(BusinessException::class.java) {
            scheduleService.deleteSchedule(user.id, response.scheduleId)
        }
    }

    @Test
    fun `월별 조회 시 단발 일정과 반복 일정을 함께 반환한다`() {
        personalScheduleRepository.save(
            PersonalSchedule(
                user = user,
                title = "단발",
                categoryColor = "#FF0000",
                isRepeating = false,
                date = LocalDate.of(2026, 7, 10),
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(11, 0),
            ),
        )
        personalScheduleRepository.save(
            PersonalSchedule(
                user = user,
                title = "반복",
                categoryColor = "#00FF00",
                isRepeating = true,
                dayOfWeek = java.time.DayOfWeek.MONDAY,
                startTime = LocalTime.of(13, 0),
                endTime = LocalTime.of(14, 0),
            ),
        )

        val result = scheduleService.getSchedules(user.id, 2026, 7)

        assertEquals(2, result.size)
    }

    @Test
    fun `다른 사용자의 일정은 삭제할 수 없다`() {
        val otherUser = userRepository.save(
            User(
                email = "other@gmail.com",
                nickname = "다른사람",
                provider = OAuthProvider.GOOGLE,
                providerId = "other-user",
                role = UserRole.USER,
            ),
        )
        val schedule = personalScheduleRepository.save(
            PersonalSchedule(
                user = otherUser,
                title = "남의 일정",
                categoryColor = "#FF0000",
                isRepeating = false,
                date = LocalDate.of(2026, 7, 1),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 0),
            ),
        )

        val exception = assertThrows(BusinessException::class.java) {
            scheduleService.deleteSchedule(user.id, schedule.id)
        }

        assertEquals(ErrorCode.SCHEDULE_NOT_FOUND, exception.errorCode)
    }
}
