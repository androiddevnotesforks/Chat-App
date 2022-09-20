package com.devwarex.chatapp.ui.contacts

import com.devwarex.chatapp.db.Contact

sealed class ContactsUiState{
    data class Loading(val isLoading: Boolean = true): ContactsUiState()
    data class PermissionState(val showPermissionMessage: Boolean = false): ContactsUiState()
    data class Success(val isLoading: Boolean = false,val contacts: List<Contact> = listOf()): ContactsUiState()
}