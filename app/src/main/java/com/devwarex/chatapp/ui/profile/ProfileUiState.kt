package com.devwarex.chatapp.ui.profile

import com.devwarex.chatapp.db.User

data class ProfileUiState(
    val user: User? = null,
    val isUploadingImage: Boolean = false,
    val isLoading: Boolean = true,
    val name: String = "",
    val isNameUpdated: Boolean = false
)