package com.devwarex.chatapp.ui.conversation

import android.graphics.Bitmap
import com.devwarex.chatapp.db.Chat
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.db.User

data class MessageUiState(
    val messages: List<Message> = listOf(),
    val enable: Boolean = true,
    val uid: String = "",
    val isLoading: Boolean = true,
    val chat: Chat? = null,
    val receiverUser: User? = null,
    val availability: Boolean = false,
    val typing: Boolean = false,
    val previewBeforeSending: Boolean = false,
    val bitmap: Bitmap?= null,
    val isPreviewImage: Boolean = false,
    val previewImage: String = ""
)
