package com.moijang.moijangbackend.team.repository

import com.moijang.moijangbackend.team.entity.TeamUser
import org.springframework.data.jpa.repository.JpaRepository

interface TeamUserRepository : JpaRepository<TeamUser, Long> {
    fun findAllByTeam_Id(teamId: Long): List<TeamUser>

    fun existsByTeam_IdAndUser_Id(teamId: Long, userId: Long): Boolean

    fun countByTeam_Id(teamId: Long): Long

    fun deleteByTeam_IdAndUser_Id(teamId: Long, userId: Long)

    fun deleteAllByTeam_Id(teamId: Long)
}
