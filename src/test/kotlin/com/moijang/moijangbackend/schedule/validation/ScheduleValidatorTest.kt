package com.moijang.moijangbackend.schedule.validation

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleValidatorTest {

    @Test
    fun `단발 일정은 date가 있으면 통과한다`() {
        assertDoesNotThrow {
            ScheduleValidator.validate(
                isRepeating = false,
                date = LocalDate.of(2026, 7, 1),
                dayOfWeek = null,
                startTime = LocalTime.of(13, 0),
                endTime = LocalTime.of(14, 0),
            )
        }
    }

    @Test
    fun `반복 일정은 dayOfWeek가 있으면 통과한다`() {
        assertDoesNotThrow {
            ScheduleValidator.validate(
                isRepeating = true,
                date = null,
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(13, 0),
                endTime = LocalTime.of(14, 0),
            )
        }
    }

    @Test
    fun `시작 시간이 종료 시간보다 늦으면 실패한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleValidator.validate(
                isRepeating = false,
                date = LocalDate.of(2026, 7, 1),
                dayOfWeek = null,
                startTime = LocalTime.of(15, 0),
                endTime = LocalTime.of(14, 0),
            )
        }
    }

    @Test
    fun `단발 일정에 dayOfWeek가 있으면 실패한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScheduleValidator.validate(
                isRepeating = false,
                date = LocalDate.of(2026, 7, 1),
                dayOfWeek = DayOfWeek.MONDAY,
                startTime = LocalTime.of(13, 0),
                endTime = LocalTime.of(14, 0),
            )
        }
    }
}
