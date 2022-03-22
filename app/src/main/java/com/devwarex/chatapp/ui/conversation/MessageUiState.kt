package com.devwarex.chatapp.ui.conversation

import com.devwarex.chatapp.db.Message

data class MessageUiState(
    val messages: List<Message> = listOf(),
    val enable: Boolean = true,
    val uid: String = "",
    val isLoading: Boolean = true
)
