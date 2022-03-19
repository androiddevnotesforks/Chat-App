package com.devwarex.chatapp.ui.signIn

import com.devwarex.chatapp.ui.signUp.ErrorsState

data class SignInErrorState(
    val email: ErrorsState = ErrorsState.NONE,
    val password: ErrorsState = ErrorsState.NONE
)
