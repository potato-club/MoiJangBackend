package com.moijang.moijangbackend.friend.repository

import com.moijang.moijangbackend.friend.entity.Friend
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<Friend, Long> {
    fun findByUserId(from: Long): List<Friend>
    fun existsByUserIdAndFriendId(userId: Long, from: Long): Boolean
    fun deleteByUserIdAndFriendId(userId: Long, friendId: Long)
}