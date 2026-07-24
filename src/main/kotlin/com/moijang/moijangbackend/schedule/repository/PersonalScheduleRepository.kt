package com.moijang.moijangbackend.schedule.repository

import com.moijang.moijangbackend.schedule.entity.PersonalSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface PersonalScheduleRepository : JpaRepository<PersonalSchedule, Long> {
    fun findAllByUser_IdAndDateBetween(
        userId: Long,
        start: LocalDate,
        end: LocalDate,
    ): List<PersonalSchedule>

    fun findAllByUser_IdAndIsRepeatingTrue(userId: Long): List<PersonalSchedule>

    @Query(
        """
        SELECT schedule
        FROM PersonalSchedule schedule
        JOIN FETCH schedule.user
        WHERE schedule.user.id IN :userIds
          AND schedule.isRepeating = false
          AND schedule.date BETWEEN :start AND :end
        """,
    )
    fun findAllDatedByUserIdsAndDateBetween(
        @Param("userIds") userIds: Collection<Long>,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate,
    ): List<PersonalSchedule>

    @Query(
        """
        SELECT schedule
        FROM PersonalSchedule schedule
        JOIN FETCH schedule.user
        WHERE schedule.user.id IN :userIds
          AND schedule.isRepeating = true
        """,
    )
    fun findAllRepeatingByUserIds(
        @Param("userIds") userIds: Collection<Long>,
    ): List<PersonalSchedule>

    fun deleteAllBySourceTeam_Id(teamId: Long)

    fun deleteAllBySourceTeam_IdAndUser_Id(teamId: Long, userId: Long)

    fun findByIdAndUser_Id(id: Long, userId: Long): PersonalSchedule?
}
