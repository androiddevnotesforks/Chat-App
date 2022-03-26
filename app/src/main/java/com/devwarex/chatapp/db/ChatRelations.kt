package com.devwarex.chatapp.db

import androidx.room.Embedded
import androidx.room.Relation


data class ChatRelations(
    @Embedded val chat: Chat,
    @Relation(
        entity = User::class,
        parentColumn = "receiverUid",
        entityColumn = "uid"
    )
    val user: User?
)
