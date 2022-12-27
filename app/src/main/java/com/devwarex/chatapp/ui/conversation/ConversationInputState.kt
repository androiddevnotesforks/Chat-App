package com.devwarex.chatapp.ui.conversation

import android.graphics.Bitmap

data class ConversationInputState(
    val isLoading: Boolean = false,
    val bitmap: Bitmap?= null,
    val text: String = ""
)
