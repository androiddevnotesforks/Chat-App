package com.devwarex.chatapp.db

import androidx.room.Embedded
import androidx.room.Relation

data class MessagesRelations(
    @Embedded val messages: List<Message>,
    @Relation(
        entity = Chat::class,
        parentColumn = "chat_id",
        entityColumn = "id"
    )
    val chat: Chat
)
