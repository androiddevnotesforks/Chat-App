package com.devwarex.chatapp.ui.verify

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devwarex.chatapp.repos.UserByIdRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
    private val _phoneNumber = MutableLiveData<String>()
    private val _codeNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get() = _phoneNumber
    val codeNumber: LiveData<String> get() = _codeNumber

    fun setPhone(s: String){
        if (s.isNotBlank() && s.isDigitsOnly()){
            _phone.value = s
        }
    }


    fun setCode(s: String){
        if (s.length in 0..6){
            _code.value = s
        }
    }

    fun onCodeSent(){
        _uiState.value = _uiState.value.copy(sent = true, requestingCode = false, verifying = false)
    }

    fun onRequestCode(){
        if (_phone.value.length == 11) {
            _phoneNumber.value = _phone.value
            _uiState.value = _uiState.value.copy(requestingCode = true)
        }
    }

    fun onVerify(){
        Log.e("codeVm",_code.value)
        if (_code.value.length == 6) {
            _codeNumber.value = _code.value
            _uiState.value = _uiState.value.copy(verifying = true, sent = false)
        }
    }

    fun onSuccess(){
        _uiState.value = _uiState.value.copy(success = true, verifying = false, sent = false, requestingCode = false)
    }

    fun verifyAccount(){
        repo.verifyAccount()
    }

    fun onPhoneIsWrong(){
        _uiState.value = VerifyUiState()
        _code.value = ""
        _codeNumber.value = ""
    }

    fun onWrongCode(){
        _uiState.value = _uiState.value.copy(sent = true, requestingCode = false, verifying = false, success = false)
        _code.value = ""
        _codeNumber.value = ""
    }

}