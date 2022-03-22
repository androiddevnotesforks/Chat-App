package com.devwarex.chatapp.models

data class MessageNotifyDataModel(
    val title: String,
    val id: String,
    val img: String = "",
    val body: String
    )
