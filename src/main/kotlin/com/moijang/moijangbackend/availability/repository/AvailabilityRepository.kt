package com.moijang.moijangbackend.availability.repository

import com.moijang.moijangbackend.availability.entity.Availability
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AvailabilityRepository : JpaRepository<Availability, Long> {
    fun findAllByTeam_Id(teamId: Long): List<Availability>

    fun findAllByTeam_IdAndUser_Id(teamId: Long, userId: Long): List<Availability>

    fun deleteAllByTeam_Id(teamId: Long)

    fun deleteAllByTeam_IdAndUser_Id(teamId: Long, userId: Long)

    @Query(
        """
        SELECT availability
        FROM Availability availability
        JOIN FETCH availability.user
        WHERE availability.team.id = :teamId
        """,
    )
    fun findAllWithUserByTeamId(@Param("teamId") teamId: Long): List<Availability>
}
