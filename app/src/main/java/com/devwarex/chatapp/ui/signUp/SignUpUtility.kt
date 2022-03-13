package com.devwarex.chatapp.ui.signUp

class SignUpUtility {

    companion object {
        fun nameError(name: String): ErrorsState = when {
            name.isBlank() -> ErrorsState.EMPTY
            name.length < 4 -> ErrorsState.INVALID_NAME
            else -> ErrorsState.NONE
        }

        fun emailError(email: String): ErrorsState = when {
            email.isBlank() -> ErrorsState.EMPTY
            email.isNotBlank() && !email.contains('@') && !email.contains('.') -> ErrorsState.INVALID_EMAIL
            else -> ErrorsState.NONE
        }

        fun passwordError(password: String, name: String, email: String): ErrorsState = when {
            password.isBlank() -> ErrorsState.EMPTY
            password.length < 8 -> ErrorsState.SHORT_PASSWORD
            password.isNotBlank() && password.length >= 8 && password.contains(email) && password.contains(
                name
            ) -> ErrorsState.WEAK_PASSWORD
            else -> ErrorsState.NONE
        }

        fun confirmPasswordError(password: String, confirmPassword: String): ErrorsState = when {
            password.isNotBlank() && password.length >= 8 && confirmPassword.isBlank() -> ErrorsState.EMPTY
            password.isNotBlank() && password.length >= 8 && confirmPassword.isNotBlank() && password != confirmPassword -> ErrorsState.NOT_MATCH_PASSWORD
            else -> ErrorsState.NONE
        }

        fun filterEmail(email: String): String {
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

        /*private fun isPasswordValid(
       password: String,
       email: String,
       confirmPassword: String,
       name: String
   ): Boolean = password.isNotBlank() && confirmPassword.isNotBlank() &&
           password == confirmPassword && password.length >= 8 && name.isNotBlank() && !password.contains(name) &&
           email.isNotBlank() && !password.contains(email)

      fun isDataValid(elements: SignUpUiState): Boolean = elements.name.isNotBlank() && elements.name.length > 3 && elements.email.isNotBlank() &&
               elements.email.contains('@') && elements.email.contains('.') && isPasswordValid(
           password = elements.password,
           email = elements.email,
           confirmPassword = elements.confirmPassword,
           name = elements.name
       )*/

        fun isDataValid(errors: SignUpErrors): Boolean = errors.name == ErrorsState.NONE && errors.password == ErrorsState.NONE &&
                errors.email == ErrorsState.NONE && errors.confirmPassword == ErrorsState.NONE

    }

}
