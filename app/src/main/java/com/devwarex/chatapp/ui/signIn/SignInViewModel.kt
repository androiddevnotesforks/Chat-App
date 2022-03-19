package com.devwarex.chatapp.ui.signIn

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.ui.signUp.ErrorsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repo: SignInRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> get() = _uiState
    val isSucceed: LiveData<Boolean> get() = repo.isSucceed

    init {
        viewModelScope.launch {
            repo.uiState.collect {
                if (_uiState.value != it) {
                    _uiState.value = it
                }
            }
        }
    }
    fun signIn(){
        repo.attemptToSignIn(_uiState.value)
    }

    fun updatePassword(s: String){
        if (_uiState.value.password == s) return
        _uiState.value = _uiState.value.copy(
            password = s,
            errors = _uiState.value.copy().errors.copy(password = ErrorsState.NONE)
        )
    }

    fun updateEmail(s: String){
        if (_uiState.value.email == s) return
        if (s.length < 65) {
            _uiState.value = _uiState.value.copy(
                email = repo.clearEmail(s),
                errors = _uiState.value.copy().errors.copy(email = ErrorsState.NONE)
            )
        }
    }
}