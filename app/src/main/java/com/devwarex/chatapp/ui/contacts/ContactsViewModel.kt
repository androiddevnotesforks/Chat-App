package com.devwarex.chatapp.ui.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.datastore.DatastoreImpl
import com.devwarex.chatapp.db.Contact
import com.devwarex.chatapp.util.Timeout.isTimeout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Queue
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repo: ContactsRepo,
    private val datastore: DatastoreImpl
):ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading())
    private val _requestContactPermission = MutableStateFlow(false)
    private val _shouldRetrieveContacts = MutableStateFlow(false)
    private val _shouldInviteContact = MutableStateFlow("")
    val uiState: StateFlow<ContactsUiState> get() = _uiState
    val requestContactPermission: StateFlow<Boolean> get() = _requestContactPermission
    val shouldRetrieveContacts: StateFlow<Boolean> get() = _shouldRetrieveContacts
    val shouldInviteContact: StateFlow<String> get() = _shouldInviteContact
    val chatId: Flow<String?> get() = repo.chatId

    init {
        viewModelScope.launch {
            repo.getContacts().collect{
                if (it.isEmpty()){
                    viewModelScope.launch { _shouldRetrieveContacts.emit(true) }
                }else{
                    _uiState.emit(ContactsUiState.Success(isLoading = false, contacts = it))
                        launch { datastore.contactsTimeout.collect{ time ->
                            if (isTimeout(
                                    currentTime = Calendar.getInstance().timeInMillis,
                                    savedTime = time
                                )
                            ){
                                repo.searchContacts(it)
                                datastore.updateContactsTimeout(Calendar.getInstance().timeInMillis)
                            }
                        }
                    }
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

    fun inviteContact(phone: String){
        viewModelScope.launch {
            _shouldInviteContact.emit(phone)
        }
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

    fun createChat(phone: String){
        repo.findIfUserExistByPhone(phone)
    }

    fun cancelJobs(){
        repo.cancelJobs()
    }
}