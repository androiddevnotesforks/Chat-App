package com.devwarex.chatapp.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.db.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Queue
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repo: ContactsRepo
):ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading())
    private val _requestContactPermission = MutableStateFlow(false)
    private val _shouldRetrieveContacts = MutableStateFlow(false)
    val uiState: StateFlow<ContactsUiState> get() = _uiState
    val requestContactPermission: StateFlow<Boolean> get() = _requestContactPermission
    val shouldRetrieveContacts: StateFlow<Boolean> get() = _shouldRetrieveContacts


    init {
        viewModelScope.launch {
            repo.getContacts().collect{
                if (it.isEmpty()){
                    viewModelScope.launch { _shouldRetrieveContacts.emit(true) }
                }else{
                    _uiState.emit(ContactsUiState.Success(isLoading = false, contacts = it))
                }
            }
        }
    }

    fun showPermissionMessage(){
        viewModelScope.launch {
            _uiState.emit(ContactsUiState.PermissionState(showPermissionMessage = true))
            _requestContactPermission.emit(false)
        }
    }

    fun requestContactPermission(){
        viewModelScope.launch { _requestContactPermission.emit(true) }
    }


    fun loading(){
        viewModelScope.launch {
            _uiState.emit(ContactsUiState.Loading())
        }
    }

    fun removeRequestObserving(){
        viewModelScope.launch {
            _requestContactPermission.emit(false)
        }
    }
    fun updateContacts(list: Queue<Contact>) =  repo.updateContacts(contacts = list)

}