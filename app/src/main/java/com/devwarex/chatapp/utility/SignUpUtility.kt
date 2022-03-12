package com.devwarex.chatapp.utility

import com.devwarex.chatapp.R
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SignUpUtility @Inject constructor() {
    var name: String = ""
    var email: String = ""
    var password: String = ""
    var confirmPassword: String = ""

    fun nameMessage(): Int {
        return when {
            name.isBlank() -> R.string.empty_message
            name.length < 4 -> R.string.invalid_name_message
            else -> R.string.empty
        }
    }



    fun emailMessage(): Int =  when{
        email.isBlank() -> R.string.empty_message
        email.isNotBlank() && !email.contains('@') && !email.contains('.') -> R.string.invalid_email_message
        else -> R.string.empty
    }

    fun passwordMessage(): Int {
        return when{
            password.isBlank() -> R.string.empty_message
            password.length < 8 -> R.string.password_short_message
            password.isNotBlank() && password.length >= 8 && password.contains(email) && password.contains(name) -> R.string.password_weak_message
            else -> R.string.empty
        }
    }

    fun confirmPasswordMessage(): Int = when{
        password.isNotBlank() && password.length >= 8 && confirmPassword.isBlank() -> R.string.empty_message
        password.isNotBlank() && password.length >= 8 && confirmPassword.isNotBlank() && password != confirmPassword -> R.string.password_not_same_message
        else -> R.string.empty
    }
    fun filterEmail(){
        var newEmail = ""
        if (email.isNotBlank()){
            email.forEach{ c ->
                if (!c.isWhitespace()){
                    newEmail += c
                }
            }
        }
        email = newEmail
    }

    private fun isPasswordValid(): Boolean = password.isNotBlank() && confirmPassword.isNotBlank() &&
            password == confirmPassword && password.length >= 8 && name.isNotBlank() && !password.contains(name) &&
            email.isNotBlank() && !password.contains(email)

    fun isDataValid(): Boolean = name.isNotBlank() && name.length > 3 && email.isNotBlank() &&
            email.contains('@') && email.contains('.') && isPasswordValid()
}