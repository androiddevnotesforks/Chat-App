package com.devwarex.chatapp.ui.profile

import android.graphics.Bitmap
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: EditProfileRepo
): ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    private val _insert = MutableStateFlow(false)
    private val _isSignedOut = MutableStateFlow(false)
    val uiState: StateFlow<ProfileUiState> get() = _uiState
    val insert: StateFlow<Boolean> get() = _insert
    val isSignedOut: StateFlow<Boolean> = _isSignedOut

    init {
        viewModelScope.launch {
            repo.uiState.collect{
                _uiState.emit(it)
            }
        }
    }

    fun onNameChange(s: String) = viewModelScope.launch {
        if (s.isNotBlank()) {
            _uiState.emit(_uiState.value.copy(name = s))
        }
    }

    fun updateUser(){
        val s = _uiState.value.name
        if (s.isNotBlank() && !s.isDigitsOnly()){
            repo.updateUserName(s)
        }
    }

    fun insertPhoto(){
        _insert.value = true
    }

    fun removeInsertPhoto(){
        _insert.value = false
    }

    fun setBitmap(bitmap: Bitmap){
        repo.uploadProfilePic(bitmap)
    }

    fun signOut(){
        Firebase.auth.signOut()
        viewModelScope.launch {
            if (Firebase.auth.currentUser == null) {
                _isSignedOut.emit(true)
                //repo.deleteUserData()
            }
        }
    }


}