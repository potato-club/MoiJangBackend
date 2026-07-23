package com.moijang.moijangbackend.global.validation

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * 일정/희망시간 공통 슬롯 검증 (시작 < 종료, date XOR dayOfWeek).
 */
object DateTimeSlotValidator {
    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime) {
        require(startTime.isBefore(endTime)) {
            "시작 시간은 종료 시간보다 빨라야 합니다"
        }
    }

    fun validateDateOrDayOfWeek(
        requiresDate: Boolean,
        date: LocalDate?,
        dayOfWeek: DayOfWeek?,
        dateRequiredMessage: String,
        dayOfWeekRequiredMessage: String,
    ) {
        if (requiresDate) {
            require(date != null && dayOfWeek == null) { dateRequiredMessage }
        } else {
            require(date == null && dayOfWeek != null) { dayOfWeekRequiredMessage }
        }
    }
}
