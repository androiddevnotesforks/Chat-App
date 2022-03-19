package com.devwarex.chatapp.ui.signIn

data class SignInUiState(
    var email: String = "",
    var password: String = "",
    var isLoading: Boolean = false,
    var isSucceed: Boolean = false,
    var errors: SignInErrorState = SignInErrorState()
)
