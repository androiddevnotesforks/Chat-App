package com.devwarex.chatapp.models

data class NotificationResponse(
    val multicast_id: Long,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int
)
