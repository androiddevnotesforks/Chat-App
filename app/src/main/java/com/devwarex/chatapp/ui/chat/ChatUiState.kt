package com.devwarex.chatapp.ui.chat

import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.models.ChatRelations

data class ChatUiState(
    val chats: List<ChatRelations> = listOf(),
    val isLoading: Boolean = true
)
