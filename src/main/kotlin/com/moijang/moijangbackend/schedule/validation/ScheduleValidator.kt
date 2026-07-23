package com.moijang.moijangbackend.schedule.validation

import com.moijang.moijangbackend.global.validation.DateTimeSlotValidator
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
        DateTimeSlotValidator.validateTimeRange(startTime, endTime)
        DateTimeSlotValidator.validateDateOrDayOfWeek(
            requiresDate = !isRepeating,
            date = date,
            dayOfWeek = dayOfWeek,
            dateRequiredMessage = "단발 일정은 date가 필요하고 dayOfWeek는 비어 있어야 합니다",
            dayOfWeekRequiredMessage = "반복 일정은 dayOfWeek가 필요하고 date는 비어 있어야 합니다",
        )
    }
}
