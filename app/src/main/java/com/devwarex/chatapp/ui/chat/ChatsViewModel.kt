package com.devwarex.chatapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repo: ChatsRepo
): ViewModel()  {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> get() = _uiState
    private val _chatId = MutableLiveData<String>()
    val chatId:LiveData<String> get() = _chatId
    private val _toContacts = MutableStateFlow(false)
    private val _toProfile = MutableStateFlow(false)
    val toContacts: StateFlow<Boolean> get() = _toContacts
    val toProfile: StateFlow<Boolean> get() = _toProfile

    fun sync(){
        repo.sync()
    }

    init {
        viewModelScope.launch {
            launch { repo.uiState.collect { _uiState.value = it } }
        }
    }

    fun clearChatId(){
        _chatId.value = ""
    }

    fun onChatClick(id: String){
        _chatId.value = id
    }

    fun navigateToProfile() = viewModelScope.launch {
        _toProfile.emit(true)
    }

    fun removeListener(){
        repo.removeListener()
    }

    fun toContacts(){
        viewModelScope.launch { _toContacts.emit(true) }
    }

    fun removeToContactsObserver() = viewModelScope.launch {
        _toContacts.emit(false)
        _toProfile.emit(false)
    }


}