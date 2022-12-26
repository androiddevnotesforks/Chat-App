package com.devwarex.chatapp.models.notification

data class NotificationResponse(
    val multicast_id: Long,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int
)
