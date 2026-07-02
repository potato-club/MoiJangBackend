package com.moijang.moijangbackend.availability.validation

import com.moijang.moijangbackend.team.entity.RoomType
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class AvailabilityValidatorTest {

    @Test
    fun `단기 팀방은 date가 있으면 통과한다`() {
        assertDoesNotThrow {
            AvailabilityValidator.validate(
                roomType = RoomType.SHORT_TERM,
                date = LocalDate.of(2026, 7, 15),
                dayOfWeek = null,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(16, 0),
            )
        }
    }

    @Test
    fun `정기 팀방은 dayOfWeek가 있으면 통과한다`() {
        assertDoesNotThrow {
            AvailabilityValidator.validate(
                roomType = RoomType.RECURRING,
                date = null,
                dayOfWeek = DayOfWeek.FRIDAY,
                startTime = LocalTime.of(18, 0),
                endTime = LocalTime.of(20, 0),
            )
        }
    }

    @Test
    fun `단기 팀방에 dayOfWeek가 있으면 실패한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            AvailabilityValidator.validate(
                roomType = RoomType.SHORT_TERM,
                date = LocalDate.of(2026, 7, 15),
                dayOfWeek = DayOfWeek.FRIDAY,
                startTime = LocalTime.of(14, 0),
                endTime = LocalTime.of(16, 0),
            )
        }
    }
}
