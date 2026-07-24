package com.moijang.moijangbackend.team.repository

import com.moijang.moijangbackend.team.entity.TeamUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TeamUserRepository : JpaRepository<TeamUser, Long> {
    fun findAllByTeam_Id(teamId: Long): List<TeamUser>

    fun findByTeam_IdAndUser_Id(teamId: Long, userId: Long): TeamUser?

    @Query("SELECT teamUser.user.id FROM TeamUser teamUser WHERE teamUser.team.id = :teamId")
    fun findUserIdsByTeamId(@Param("teamId") teamId: Long): List<Long>

    fun existsByTeam_IdAndUser_Id(teamId: Long, userId: Long): Boolean

    fun countByTeam_Id(teamId: Long): Long

    fun deleteByTeam_IdAndUser_Id(teamId: Long, userId: Long)

    fun deleteAllByTeam_Id(teamId: Long)
}
