package com.devwarex.chatapp.ui.conversation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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

    fun sync(chatId: String){
        if (chatId.isEmpty()) return
        repo.sync(chatId)
    }
    fun setText(s: String){
        _text.value = s
    }

    fun send(){
        if (_text.value.isNotBlank()) {
            repo.send(MessageUtility.filterText(_text.value))
        }
    }

    init {
        viewModelScope.launch {
            launch { repo.uiState.collect {
                _uiState.value = it
                if (!it.isLoading){
                    _text.value = ""
                }
            } }
        }
    }

    fun removeListener(){
        repo.removeListener()
    }
}