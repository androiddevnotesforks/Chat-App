package com.devwarex.chatapp.ui.signUp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val repo: SignUpRepo
): ViewModel() {

    val uiState = MutableStateFlow(SignUpUiState())
    val isSucceed: LiveData<Boolean> get() = repo.isSucceed

    init {
        viewModelScope.launch {
            repo.uiState.collect {
                if (uiState.value != it){
                    uiState.value = it
                    Log.e("state",Gson().toJson(it))
                }
            }
        }
    }
    fun signUp(){
        repo.signUp(uiState.value)
    }

    fun updatePassword(s: String){
        if (uiState.value.password == s) return
        uiState.value = uiState.value.copy(
            password = s,
            errors = uiState.value.copy().errors.copy(password = ErrorsState.NONE)
        )
    }
    fun updateConfirmPassword(s: String){
        if (uiState.value.confirmPassword == s) return
        uiState.value = uiState.value.copy(
            confirmPassword = s,
            errors = uiState.value.copy().errors.copy(confirmPassword = ErrorsState.NONE)
        )
    }
    fun updateName(s: String){
        if (uiState.value.name == s) return
        if (s.length < 16) {
            uiState.value = uiState.value.copy(
                name = s,
                errors = uiState.value.copy().errors.copy(name = ErrorsState.NONE)
            )
        }
    }
    fun updateEmail(s: String){
        if (uiState.value.email == s) return
        if (s.length < 65) {
            uiState.value = uiState.value.copy(
                email = repo.filterEmail(s),
                errors = uiState.value.copy().errors.copy(email = ErrorsState.NONE)
            )
        }
    }
}