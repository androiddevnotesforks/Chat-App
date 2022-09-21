package com.devwarex.chatapp.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.MessageType

@Entity(tableName = "chat_messages_table")
data class Message(
    @NonNull @ColumnInfo(name = "message_id") @PrimaryKey val id: String,
    @NonNull @ColumnInfo(name = "chat_id") val chatId: String,
    @NonNull @ColumnInfo(name = "body") val body: String,
    @NonNull @ColumnInfo(name = "sender_name") val name: String,
    @NonNull @ColumnInfo(name = "sender_id") val senderId: String,
    @NonNull @ColumnInfo(name = "timestamp") val timestamp: Long,
    @NonNull @ColumnInfo(name = "message_type") val type: MessageType,
    @NonNull @ColumnInfo(name = "message_state") var state: MessageState
)
