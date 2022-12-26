package com.devwarex.chatapp.models.notification

data class MessageNotificationModel(
    val to: String,
    val direct_boot_ok: Boolean = true,
    val android: NotifyAndroidModel = NotifyAndroidModel(),
    val data: MessageNotifyDataModel
    )
