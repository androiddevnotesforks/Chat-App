package com.devwarex.chatapp.ui.signUp

data class SignUpUiState(
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var isLoading: Boolean = false,
    var isSucceedToSignUp: Boolean = false,
    var errors: SignUpErrors = SignUpErrors()
){



}
