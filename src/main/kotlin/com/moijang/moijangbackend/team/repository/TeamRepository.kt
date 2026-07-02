package com.moijang.moijangbackend.team.repository

import com.moijang.moijangbackend.team.entity.Team
import org.springframework.data.jpa.repository.JpaRepository

interface TeamRepository : JpaRepository<Team, Long> {
    fun existsByInviteCode(inviteCode: String): Boolean

    fun findByInviteCode(inviteCode: String): Team?
}
