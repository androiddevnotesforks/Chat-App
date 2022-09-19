package com.devwarex.chatapp.ui.verify

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.models.CountryModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val repo: VerifyRepo
): ViewModel() {

    private val _code = MutableStateFlow("")
    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> get() = _uiState
    val code: StateFlow<String> get() = _code
    val isVerified: Flow<Boolean> get() = repo.isVerified
    private val _codeNumber = MutableLiveData<String>()
    val codeNumber: LiveData<String> get() = _codeNumber
    val countries: StateFlow<List<CountryModel>> get() = repo.countries

    fun getCountries() = repo.getCountries()

    fun dropDown(){
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(drop = !_uiState.value.drop))
        }
    }
    fun setPhone(s: String){
        if (s.isNotBlank()){
            if (s[0] == '0'){
                return
            }
        }
        if (s.isDigitsOnly()){
            viewModelScope.launch {
                _uiState.emit(_uiState.value.copy(phone = s))
            }
        }
    }

    fun onCountrySelect(country: CountryModel){
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(drop = false, selectedCountry = country))
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
        if (_uiState.value.phone.length > 7) {
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
        val c = _uiState.value.selectedCountry
        val p = _uiState.value.phone
        if (c != null)
        repo.verifyAccount("${c.idd.root}${c.idd.suffixes[0]}$p")
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

    override fun onCleared() {
        super.onCleared()
        repo.cancelJob()
    }

}