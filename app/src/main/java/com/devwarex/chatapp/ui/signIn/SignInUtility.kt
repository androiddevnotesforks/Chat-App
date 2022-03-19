package com.devwarex.chatapp.ui.signIn

import com.devwarex.chatapp.ui.signUp.ErrorsState

class SignInUtility {


    companion object{

        fun passwordError(password: String): ErrorsState = when {
            password.isBlank() -> ErrorsState.EMPTY
            password.length < 8 -> ErrorsState.SHORT_PASSWORD
            else -> ErrorsState.NONE
        }
        fun emailError(email: String): ErrorsState = when {
            email.isBlank() -> ErrorsState.EMPTY
            email.isNotBlank() && !email.contains('@') && !email.contains('.') -> ErrorsState.INVALID_EMAIL
            else -> ErrorsState.NONE
        }
        fun clearEmail(email: String): String {
            var newEmail = ""
            if (email.isNotBlank()) {
                email.forEach { c ->
                    if (!c.isWhitespace()) {
                        newEmail += c
                    }
                }
            }
            return newEmail
        }

        fun isDataValid(errors: SignInErrorState): Boolean = errors.password == ErrorsState.NONE &&
                errors.email == ErrorsState.NONE
    }
}