package com.devwarex.chatapp.ui.verify

data class VerifyUiState(
    val sent: Boolean = false,
    val requestingCode: Boolean = false,
    val verifying: Boolean = false,
    val success: Boolean = false
)
