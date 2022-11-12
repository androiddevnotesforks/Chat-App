package com.devwarex.chatapp.ui.conversation


import android.graphics.Bitmap
import android.util.Log
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
): ViewModel() {

    private val _text = MutableStateFlow("")
    private val _uiState = MutableStateFlow(MessageUiState())
    private val _locationUiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<MessageUiState> get() = _uiState
    val locationUiState: StateFlow<LocationUiState> = _locationUiState
    val text: StateFlow<String> get() = _text
    val shouldFetchChat: Flow<Boolean> get() = repo.shouldFetchChat.receiveAsFlow()
    private var isSent = false
    private val _insert = MutableLiveData<Boolean>()
    val insert: LiveData<Boolean> get() = _insert
    val uploadProgress: StateFlow<Int> get() = repo.uploadProgress
    init {
        viewModelScope.launch {
            launch { repo.uiState.collect {
                _uiState.emit(it)
                if (!it.isLoading && isSent){
                    _text.value = ""
                    isSent = false
                }
            } }

            launch { text.collect { repo.setTypingState(it.isNotEmpty()) } }

            launch {
                locationUiState.collect{ location ->

                }
            }
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
    }

    fun insertPhoto(){
        _insert.value = true
    }

    fun removeInsertPhoto(){
        _insert.value = false
    }
    fun onPreviewImage(img: String){
        _uiState.value = _uiState.value.copy(previewImage = img, isPreviewImage = true)
    }

    fun closePreviewImageForSending(){
        _uiState.value = _uiState.value.copy(previewBeforeSending = false, bitmap = null)
        repo.zeroProgress()
    }

    fun closePreviewImage(){
        _uiState.value = _uiState.value.copy(previewImage = "", isPreviewImage = false)
    }

    fun sendImage(){
        if (_uiState.value.bitmap != null){
            repo.sendImage(_uiState.value.bitmap!!)
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

        _uiState.emit(
            value = _uiState.value.copy(
                requestLocation = false
            )
        )
    }

    fun pickLocation() = viewModelScope.launch {
        _uiState.emit(
            value = _uiState.value.copy(
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

    fun updateLocationPin(lat: Double,lng: Double) = viewModelScope.launch{
        _uiState.emit(
            value = _uiState.value.copy(locationPin = LocationPin(lat = lat,lng = lng))
        )
    }

    fun shareCurrentLocation() = viewModelScope.launch {
        val pin = _uiState.value.locationPin
        repo.shareLocationPin(pin)
        delay(100)
        dismissMapDialog()
    }

    fun dismissMapDialog() = viewModelScope.launch {
        _uiState.emit(
            value = _uiState.value.copy(requestLocation = false, locationPin = LocationPin())
        )
        _locationUiState.emit(
            value = _locationUiState.value.copy(requestLastKnownLocation = false)
        )
    }

}