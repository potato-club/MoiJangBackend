package com.moijang.moijangbackend.schedule.repository

import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PersonalScheduleRepository : JpaRepository<PersonalSchedule, Long> {
    fun findAllByUser_IdAndDateBetween(
        userId: Long,
        start: LocalDate,
        end: LocalDate,
    ): List<PersonalSchedule>

    fun findAllByUser_IdAndIsRepeatingTrue(userId: Long): List<PersonalSchedule>

    fun findByIdAndUser_Id(id: Long, userId: Long): PersonalSchedule?
}
