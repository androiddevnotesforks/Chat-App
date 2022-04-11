package com.devwarex.chatapp.ui.conversation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repo: ConversationRepo
): ViewModel() {

    private val _text = MutableStateFlow<String>("")
    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState())
    val uiState: StateFlow<MessageUiState> get() = _uiState
    val text: StateFlow<String> get() = _text
    val shouldFetchChat: Flow<Boolean> get() = repo.shouldFetchChat.receiveAsFlow()
    private var isSent = false
    init {
        viewModelScope.launch {
            launch { repo.uiState.collect {
                _uiState.value = it
                if (!it.isLoading && isSent){
                    _text.value = ""
                    isSent = false
                }
            } }

            launch { text.collect { repo.setTypingState(it.isNotEmpty()) } }
        }
    }
    fun sync(chatId: String){
        if (chatId.isBlank()) return
        repo.sync(chatId)
    }
    fun setText(s: String){
        if (!_uiState.value.isLoading) {
            _text.value = s
            repo.setTypingState(s.isNotEmpty())
        }
    }

    fun send(){
        if (_text.value.isNotBlank()) {
            repo.send(MessageUtility.filterText(_text.value))
            isSent = true
        }
    }

    fun removeListener(){
        repo.removeListener()
    }

    fun onStop(){
        repo.setTypingState(false)
        repo.setAvailability(false)
    }
}