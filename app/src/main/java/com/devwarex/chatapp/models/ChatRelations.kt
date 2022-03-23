package com.devwarex.chatapp.models

import androidx.room.Embedded
import androidx.room.Relation
import com.devwarex.chatapp.db.Chat
import com.devwarex.chatapp.db.User

data class ChatRelations(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "receiverUid",
        entityColumn = "uid"
    )
    val user: User?
)
