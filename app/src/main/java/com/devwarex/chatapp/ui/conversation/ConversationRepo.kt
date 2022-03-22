package com.devwarex.chatapp.ui.conversation

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.SendMessageRepo
import com.devwarex.chatapp.repos.UserByIdRepo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationRepo @Inject constructor(
    private val userRepo: UserByIdRepo,
    private val database: AppDao,
    private val sendMessageRepo: SendMessageRepo,
    private val repo: MessagesRepo
) {

    private var currentUser: UserModel? = null
    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState())
    val uiState: StateFlow<MessageUiState> get() = _uiState
    private var token: String = ""
    init {
        repo.sync("")
        userRepo.getUser(Firebase.auth.uid ?: "")
        userRepo.getTokenByUserId(Firebase.auth.uid ?: "")
        CoroutineScope(Dispatchers.Unconfined).launch {
            launch {
                userRepo.user.receiveAsFlow().collect {
                    currentUser = it
                    _uiState.value = _uiState.value.copy(uid = currentUser?.uid ?: "")
                }
            }
            launch { database.getMessages().collect { _uiState.value = _uiState.value.copy(messages = it) } }

            launch { sendMessageRepo.isLoading.receiveAsFlow().collect { _uiState.value = _uiState.value.copy(isLoading = it, enable = !it) } }

            launch { userRepo.token.receiveAsFlow().collect { token = it } }
        }
    }

    fun send(text: String){
        if (currentUser != null) {
            sendMessageRepo.sendTextMessage(
                uid = currentUser?.uid ?: "",
                name = currentUser?.name ?: "",
                text = text,
                chatId = "",
                token = token
            )
        }
    }

}