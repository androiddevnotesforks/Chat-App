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
    val uiState: StateFlow<MessageUiState> get() = repo.uiState
    val locationUiState: StateFlow<LocationUiState> = _locationUiState
    val shouldFetchChat: Flow<Boolean> get() = repo.shouldFetchChat.receiveAsFlow()
    private var isSent = false
    private val _insert = MutableLiveData<Boolean>()
    val insert: LiveData<Boolean> get() = _insert
    val uploadProgress: StateFlow<Int> get() = repo.uploadProgress

    fun sync(chatId: String) {
        if (chatId.isBlank()) return
        repo.sync(chatId)
    }

    fun setText(s: String) = viewModelScope.launch {
        repo.setTypingState(s.isNotEmpty())
        if (!repo.uiState.value.isLoading) {
            repo.uiState.emit(value = repo.uiState.value.copy(text = s))
        }
    }

    fun send() {
        if (uiState.value.text.isNotBlank()) {
            repo.send(MessageUtility.filterText(uiState.value.text))
            isSent = true
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
        repo.uiState.emit(repo.uiState.value.copy(bitmap = bitmap, previewBeforeSending = true))
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
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewBeforeSending = false, bitmap = null)
        )
        repo.zeroProgress()
    }

    fun closePreviewImage() = viewModelScope.launch {
        repo.uiState.emit(
            value = repo.uiState.value.copy(previewImage = "", isPreviewImage = false)
        )
    }

    fun sendImage() {
        if (repo.uiState.value.bitmap != null) {
            repo.sendImage(repo.uiState.value.bitmap!!)
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

}