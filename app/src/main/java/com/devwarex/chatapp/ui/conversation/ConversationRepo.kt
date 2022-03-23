package com.devwarex.chatapp.ui.conversation

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.SendMessageRepo
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
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
    private var chatId: String = ""
    private val dbRef = Firebase.database(Paths.DATABASE_URL).reference
    private var currentUid: String = ""

    fun sync(id: String){
        currentUid = Firebase.auth.uid ?: ""
        chatId = id
        repo.sync(chatId = chatId)
        userRepo.getUser(currentUid)
        CoroutineScope(Dispatchers.Unconfined).launch {
            launch { database.getMessages(chatId = chatId).collect { _uiState.value = _uiState.value.copy(uid = currentUid, messages = it) } }
            launch { database.getChatByChatId(chatId).collect {
                _uiState.value = _uiState.value.copy(chat = it.chat, receiverUser = it.user)
                userRepo.getTokenByUserId(it.user?.uid ?: "")
                initDataBase(it.user?.uid ?: "")
            } }
        }
    }
    init {
        CoroutineScope(Dispatchers.Unconfined).launch {
            launch { sendMessageRepo.isLoading.receiveAsFlow().collect { _uiState.value = _uiState.value.copy(isLoading = it, enable = !it) } }
            launch { userRepo.token.receiveAsFlow().collect { token = it } }
            launch { userRepo.user.receiveAsFlow().collect { currentUser = it } }
        }
    }

    fun send(text: String){
        if (currentUser != null) {
            sendMessageRepo.sendTextMessage(
                uid = currentUser?.uid ?: "",
                name = currentUser?.name ?: "",
                text = text,
                chatId = chatId,
                token = token
            )
        }
    }

    private val availabilityListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _uiState.value = _uiState.value.copy(availability = snapshot.getValue<Boolean>() ?: false)
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    private val typingListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            _uiState.value = _uiState.value.copy(typing = snapshot.getValue<Boolean>() ?: false)
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    private fun initDataBase(receiverUid: String){
        if (receiverUid.isEmpty()) return
        dbRef.child("${Paths.DM}$chatId/$receiverUid/availability").addListenerForSingleValueEvent(availabilityListener)
        dbRef.child("${Paths.DM}$chatId/$receiverUid/typing").addListenerForSingleValueEvent(typingListener)
        setAvailability(true)
    }

    fun removeListener(){
        repo.removeListener()
        dbRef.removeEventListener(availabilityListener)
        dbRef.removeEventListener(typingListener)
    }

    fun setAvailability(b: Boolean){
        dbRef.child("${Paths.DM}/$chatId/$currentUid/availability").setValue(b)
    }

    fun setTypingState(b: Boolean){
        dbRef.child("${Paths.DM}/$chatId/$currentUid/typing").setValue(b)
    }
}