package com.devwarex.chatapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.repos.SignUpRepo
import com.devwarex.chatapp.utility.SignUpUtility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: SignUpRepo,
    private val utility: SignUpUtility
): ViewModel() {

    val name = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
    val isLoading = repo.isLoading
    val nameMessage = MutableStateFlow<Int>(R.string.empty)
    val passwordMessage = MutableStateFlow<Int>(R.string.empty)
    val confirmPasswordMessage = MutableStateFlow<Int>(R.string.empty)
    val emailMessage = MutableStateFlow<Int>(R.string.empty)
    val isSucceed: LiveData<Boolean> get() = repo.isSucceed

    fun signUp(){
        utility.name = name.value
        utility.email = email.value
        utility.password = password.value
        utility.confirmPassword = confirmPassword.value
        nameMessage.value = utility.nameMessage()
        emailMessage.value = utility.emailMessage()
        passwordMessage.value = utility.passwordMessage()
        confirmPasswordMessage.value = utility.confirmPasswordMessage()
        if (utility.isDataValid()) {
            repo.signUp()
        }
    }

    fun updatePassword(s: String){
        password.value = s
        passwordMessage.value = R.string.empty
    }
    fun updateConfirmPassword(s: String){
        confirmPassword.value = s
        confirmPasswordMessage.value = R.string.empty
    }
    fun updateName(s: String){
        if (s.length < 16) {
            name.value = s
        }
        nameMessage.value = R.string.empty
    }
    fun updateEmail(s: String){
        utility.email = s
        utility.filterEmail()
        if (s.length < 65) {
            email.value = utility.email
        }
        emailMessage.value = R.string.empty
    }
}