package com.devwarex.chatapp.ui.conversation


import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.models.LocationPin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repo: ConversationRepo
) : ViewModel() {

    private val _locationUiState = MutableStateFlow(LocationUiState())
    private val _inputState = MutableStateFlow(ConversationInputState())
    val inputState: StateFlow<ConversationInputState> = _inputState
    val uiState: StateFlow<MessageUiState> get() = repo.uiState
    val locationUiState: StateFlow<LocationUiState> = _locationUiState
    val shouldFetchChat: Flow<Boolean> get() = repo.shouldFetchChat.receiveAsFlow()
    private val _insert = MutableLiveData<Boolean>()
    val insert: LiveData<Boolean> get() = _insert
    val uploadProgress: StateFlow<Int> get() = repo.uploadProgress

    init {
        viewModelScope.launch {
            repo.isSendingMessage.collect {
                _inputState.emit(
                    value = _inputState.value.copy(
                        isLoading = it,
                        text = if (it) _inputState.value.text else "",
                        bitmap = if (it) _inputState.value.bitmap else null
                    )
                )
                repo.uiState.emit(
                    value = repo.uiState.value.copy(
                        previewBeforeSending = _inputState.value.bitmap != null
                    )
                )
            }
        }
    }

    fun sync(chatId: String) {
        if (chatId.isBlank()) return
        repo.sync(chatId)
    }

    fun setText(s: String) = viewModelScope.launch {
        repo.setTypingState(s.isNotEmpty())
        if (!_inputState.value.isLoading) {
            _inputState.emit(value = _inputState.value.copy(text = s))
        }
    }

    fun send() {
        if (_inputState.value.text.isNotBlank()) {
            repo.send(MessageUtility.filterText(_inputState.value.text))
        }
    }

    fun removeListener() {
        repo.removeListener()
    }

    fun available() = viewModelScope.launch { repo.setAvailability(true) }

    fun onStop() {
        repo.setTypingState(false)
        repo.setAvailability(false)
    }

    fun setBitmap(
        bitmap: Bitmap
    ) = viewModelScope.launch {
        _inputState.emit(
            value = _inputState.value.copy(bitmap = bitmap)
        )
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewBeforeSending = _inputState.value.bitmap != null)
        )
    }

    fun insertPhoto() = viewModelScope.launch {
        _insert.value = true
    }

    fun removeInsertPhoto() = viewModelScope.launch {
        _insert.value = false
    }

    fun onPreviewImage(img: String) = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewImage = img, isPreviewImage = true)
        )
    }

    fun closePreviewImageForSending() = viewModelScope.launch {
        _inputState.emit(
            value = _inputState.value.copy(bitmap = null)
        )
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewBeforeSending = _inputState.value.bitmap != null)
        )
        repo.zeroProgress()
    }

    fun closePreviewImage() = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewImage = "", isPreviewImage = false)
        )
    }

    fun sendImage() {
        if (_inputState.value.bitmap != null) {
            repo.sendImage(_inputState.value.bitmap!!)
        }
    }

    fun isLocationEnabled(b: Boolean) = viewModelScope.launch {
        _locationUiState.emit(
            value = _locationUiState.value.copy(isLocationEnabled = b)
        )
    }

    fun isLocationPermissionGranted(b: Boolean) = viewModelScope.launch {
        _locationUiState.emit(
            value = _locationUiState.value.copy(isLocationPermissionGranted = b)
        )
    }

    fun locationPermissionDenied() = viewModelScope.launch {
        _locationUiState.emit(
            value = _locationUiState.value.copy(
                requestLastKnownLocation = false
            )
        )

        repo.uiState.emit(
            value = repo.uiState.value.copy(
                requestLocation = false
            )
        )
    }

    fun pickLocation() = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(
                requestLocation = true,
                locationPermissionGranted = _locationUiState.value.isLocationPermissionGranted
                        && _locationUiState.value.isLocationEnabled
            )
        )
        _locationUiState.emit(
            value = _locationUiState.value.copy(
                requestLastKnownLocation = true
            )
        )
    }

    fun updateLocationPin(lat: Double, lng: Double) = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(locationPin = LocationPin(lat = lat, lng = lng))
        )
    }

    fun shareCurrentLocation() = viewModelScope.launch {
        val pin = repo.uiState.value.locationPin
        repo.shareLocationPin(pin)
        delay(100)
        dismissMapDialog()
    }

    fun dismissMapDialog() = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(requestLocation = false, locationPin = LocationPin())
        )
        _locationUiState.emit(
            value = _locationUiState.value.copy(requestLastKnownLocation = false)
        )
    }

    fun onDeleteMessage(
        messageId: String
    ) = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(
                deleteMessageId = messageId
            )
        )
    }

    fun deleteMessage() = repo.deleteMessage()

    fun onDismissDeleteMessage() = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(
                deleteMessageId = ""
            )
        )
    }

}