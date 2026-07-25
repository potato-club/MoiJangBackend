package com.moijang.moijangbackend.notification.repository

import com.moijang.moijangbackend.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByReceiverId(id: Long): List<Notification>
}