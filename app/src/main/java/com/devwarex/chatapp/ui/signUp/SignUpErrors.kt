package com.devwarex.chatapp.ui.signUp

data class SignUpErrors(
    var email: ErrorsState = ErrorsState.NONE,
    var password: ErrorsState = ErrorsState.NONE,
    var name: ErrorsState = ErrorsState.NONE,
    var confirmPassword: ErrorsState = ErrorsState.NONE
)
