package com.moijang.moijangbackend.availability.validation

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
        require(startTime.isBefore(endTime)) {
            "시작 시간은 종료 시간보다 빨라야 합니다"
        }

        when (roomType) {
            RoomType.SHORT_TERM -> {
                require(date != null && dayOfWeek == null) {
                    "단기 팀방 희망 시간은 date가 필요하고 dayOfWeek는 비어 있어야 합니다"
                }
            }
            RoomType.RECURRING -> {
                require(date == null && dayOfWeek != null) {
                    "정기 팀방 희망 시간은 dayOfWeek가 필요하고 date는 비어 있어야 합니다"
                }
            }
        }
    }
}
