package com.moijang.moijangbackend.availability.validation

import com.moijang.moijangbackend.global.validation.DateTimeSlotValidator
import com.moijang.moijangbackend.team.entity.RoomType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

object AvailabilityValidator {
    fun validate(
        roomType: RoomType,
        date: LocalDate?,
        dayOfWeek: DayOfWeek?,
        startTime: LocalTime,
        endTime: LocalTime,
    ) {
        DateTimeSlotValidator.validateTimeRange(startTime, endTime)
        DateTimeSlotValidator.validateDateOrDayOfWeek(
            requiresDate = roomType == RoomType.SHORT_TERM,
            date = date,
            dayOfWeek = dayOfWeek,
            dateRequiredMessage = "단기 팀방 희망 시간은 date가 필요하고 dayOfWeek는 비어 있어야 합니다",
            dayOfWeekRequiredMessage = "정기 팀방 희망 시간은 dayOfWeek가 필요하고 date는 비어 있어야 합니다",
        )
    }
}
