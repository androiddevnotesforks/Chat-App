package com.devwarex.chatapp.ui.chat

import com.devwarex.chatapp.db.ChatRelations

data class ChatUiState(
    val chats: List<ChatRelations> = listOf(),
    val isLoading: Boolean = true,
    val shouldShowDialog: Boolean = false
)
