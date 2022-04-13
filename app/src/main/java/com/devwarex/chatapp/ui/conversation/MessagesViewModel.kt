package com.devwarex.chatapp.ui.conversation


import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    private val _insert = MutableLiveData<Boolean>()
    private val _backState = MutableLiveData<BackButtonState>(BackButtonState())
    val backState: LiveData<BackButtonState> get() = _backState
    val insert: LiveData<Boolean> get() = _insert
    val uploadProgress: StateFlow<Int> get() = repo.uploadProgress
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

    fun setBitmap(bitmap: Bitmap){
        _uiState.value = _uiState.value.copy(bitmap = bitmap, previewBeforeSending = true)
        _backState.value = _backState.value?.copy(isPreviewBeforeSending = true)
    }

    fun insertPhoto(){
        _insert.value = true
    }

    fun removeInsertPhoto(){
        _insert.value = false
    }
    fun onPreviewImage(img: String){
        _uiState.value = _uiState.value.copy(previewImage = img, isPreviewImage = true)
        _backState.value = _backState.value?.copy(isImagePreview = true)
    }

    fun closePreviewImageForSending(){
        _uiState.value = _uiState.value.copy(previewBeforeSending = false, bitmap = null)
        _backState.value = _backState.value?.copy(isPreviewBeforeSending = false)
        repo.zeroProgress()
    }

    fun closePreviewImage(){
        _uiState.value = _uiState.value.copy(previewImage = "", isPreviewImage = false)
        _backState.value = _backState.value?.copy(isImagePreview = false)
    }

    fun sendImage(){
        if (_uiState.value.bitmap != null){
            repo.sendImage(_uiState.value.bitmap!!)
        }
    }
}