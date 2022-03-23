package com.devwarex.chatapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_table")
data class Chat(
    @PrimaryKey val id: String,
    val lastMessage: String,
    val receiverUid: String,
    val lastEditAt: Long,
    val createdAt: Long
)
