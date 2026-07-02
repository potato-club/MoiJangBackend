package com.moijang.moijangbackend.availability.repository

import com.moijang.moijangbackend.availability.entity.Availability
import org.springframework.data.jpa.repository.JpaRepository

interface AvailabilityRepository : JpaRepository<Availability, Long> {
    fun findAllByTeam_Id(teamId: Long): List<Availability>

    fun findAllByTeam_IdAndUser_Id(teamId: Long, userId: Long): List<Availability>
}
