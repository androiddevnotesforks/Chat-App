package com.devwarex.chatapp.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.ui.signUp.ErrorsState
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
    //private val _email = MutableStateFlow("")
    //val email: StateFlow<String> get() = _email
    //private val _emailMessage = MutableStateFlow(ErrorsState.NONE)
    //val emailMessage: StateFlow<ErrorsState> get() = _emailMessage
    //val isAdded: Flow<Boolean> get() = repo.isAdded
    private val _addUser = MutableStateFlow(false)
    val addUser: StateFlow<Boolean> get() = _addUser

    fun sync(){
        repo.sync()
    }

    init {
        viewModelScope.launch {
            launch { repo.uiState.collect { _uiState.value = it } }
          //  launch { repo.error.collect { _emailMessage.value = it } }
        }
    }

    /*fun setEmail(s: String){
        if (s.length < 65) {
            _email.value = repo.clearEmail(s)
            _emailMessage.value = ErrorsState.NONE
        }
    }*/

    fun clearChatId(){
        _chatId.value = ""
    }

    fun onChatClick(id: String){
        _chatId.value = id
    }

    fun removeListener(){
        repo.removeListener()
    }

    fun toContacts(){
        viewModelScope.launch { _addUser.value = true }
    }

    fun removeToContactsObserver(){
        viewModelScope.launch { _addUser.value = false }
    }
    
   /* fun addUser(){
        if (_email.value.isNotBlank() &&
            _email.value.contains('@') &&
            _email.value.contains('.')){
                repo.addUser(email = _email.value)
            Log.e("error","add")
        }else{
            _emailMessage.value = ErrorsState.INVALID_EMAIL
        }
    }*/

}