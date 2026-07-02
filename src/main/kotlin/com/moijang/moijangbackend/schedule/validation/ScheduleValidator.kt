package com.moijang.moijangbackend.schedule.validation

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object ScheduleValidator {
    fun validate(
        isRepeating: Boolean,
        date: LocalDate?,
        dayOfWeek: DayOfWeek?,
        startTime: LocalTime,
        endTime: LocalTime,
    ) {
        require(startTime.isBefore(endTime)) {
            "시작 시간은 종료 시간보다 빨라야 합니다"
        }

        if (isRepeating) {
            require(date == null && dayOfWeek != null) {
                "반복 일정은 dayOfWeek가 필요하고 date는 비어 있어야 합니다"
            }
        } else {
            require(date != null && dayOfWeek == null) {
                "단발 일정은 date가 필요하고 dayOfWeek는 비어 있어야 합니다"
            }
        }
    }
}
