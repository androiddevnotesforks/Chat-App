package com.devwarex.chatapp.ui.verify

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.devwarex.chatapp.repos.UserByIdRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val repo: UserByIdRepo
): ViewModel() {

    private val _phone = MutableStateFlow("")
    private val _code = MutableStateFlow("")
    private val _uiState = MutableStateFlow(VerifyUiState())
    val uistate: StateFlow<VerifyUiState> get() = _uiState
    val phone: StateFlow<String> get() = _phone
    val code: StateFlow<String> get() = _code
    val isVerified: Flow<Boolean> get() = repo.isVerified.receiveAsFlow()

    fun setPhone(s: String){
        if (s.isNotBlank() && s.isDigitsOnly()){
            _phone.value = s
        }
    }


    fun setCode(s: String){
        if (s.isNotBlank() && s.isDigitsOnly() && s.length in 0..6){
            _code.value = s
        }
    }

    fun onCodeSent(){
        _uiState.value = _uiState.value.copy(sent = true, requestingCode = false)
    }

    fun onRequestCode(){
        if (phone.value.length == 11) {
            _uiState.value = _uiState.value.copy(requestingCode = true)
        }
    }

    fun onVerify(){
        if (code.value.length == 6) {
            _uiState.value = _uiState.value.copy(verifying = true, sent = false)
        }
    }

    fun onSuccess(){
        _uiState.value = _uiState.value.copy(success = true, verifying = false, sent = false, requestingCode = false)
    }

    fun verifyAccount(){
        repo.verifyAccount()
    }

}