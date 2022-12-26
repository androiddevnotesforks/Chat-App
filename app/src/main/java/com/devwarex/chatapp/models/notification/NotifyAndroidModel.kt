package com.devwarex.chatapp.models.notification

data class NotifyAndroidModel(
    val priority: Int = 10,
    val ttl: String ="86400s"
)