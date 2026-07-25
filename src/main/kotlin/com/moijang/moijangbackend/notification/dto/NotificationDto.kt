package com.moijang.moijangbackend.notification.dto

data class NotificationDto(
    val notificationId: Int,
    val type: String,
    val senderName: String,
    val relatedId: Int,
    val message: String,
    val createdAt: String,
)