package com.devwarex.chatapp.ui.conversation

import android.graphics.Bitmap
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.SendMessageRepo
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConversationRepo @Inject constructor(
    private val userRepo: UserByIdRepo,
    private val database: AppDao,
    private val sendMessageRepo: SendMessageRepo,
    private val repo: MessagesRepo,
    private val uploadImageRepo: UploadImageRepo
) {

    private var currentUser: UserModel? = null
    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> get() = _uiState
    private var token: String = ""
    private var chatId: String = ""
    private val dbRef = Firebase.database(Paths.DATABASE_URL).reference
    private var currentUid: String = ""
    val shouldFetchChat = Channel<Boolean>()
    private val job = CoroutineScope(Dispatchers.Unconfined)
    private val chatJob = CoroutineScope(Dispatchers.Default)
    val uploadProgress = uploadImageRepo.uploadProgress

    fun sync(id: String){
        currentUid = Firebase.auth.uid ?: ""
        chatId = id
        repo.sync(chatId = chatId)
        userRepo.getUserById(currentUid)
        job.launch {
            launch { database.getMessages(chatId = chatId).collect {
                _uiState.value = _uiState.value.copy(uid = currentUid, messages = it, isLoading = false)
                checkForUnDeliveredMessages(it)
            } }
        }
        chatJob.launch { database.getChatByChatId(chatId).collect {
            if (it?.user == null){
                launch {
                    shouldFetchChat.send(true)
                    job.cancel()
                }
            }else {
                _uiState.value = _uiState.value.copy(chat = it.chat, receiverUser = it.user)
                userRepo.getTokenByUserId(it.user.uid)
                initDataBase(it.user.uid)
                chatJob.cancel()
            }
        } }
    }
    init {
        CoroutineScope(Dispatchers.Unconfined).launch {
            launch { sendMessageRepo.isLoading.receiveAsFlow().collect { _uiState.value = _uiState.value.copy(isLoading = it, enable = !it) } }
            launch { userRepo.token.receiveAsFlow().collect { token = it } }
            launch { userRepo.user.receiveAsFlow().collect { currentUser = it } }
            launch { uploadImageRepo.img.receiveAsFlow().collect {
                if (it.isNotEmpty()){
                    sendMessageRepo.sendImageMessage(
                        uid = currentUser?.uid ?: "",
                        name = currentUser?.name ?: "",
                        url = it,
                        chatId = chatId,
                        token = token,
                        availability = _uiState.value.availability
                    )
                    launch { uploadImageRepo.img.send("") }
                }
            } }
        }
    }

    fun sendImage(bitmap: Bitmap){
        uploadImageRepo.upload(bitmap = bitmap)
    }

    fun zeroProgress(){
        uploadImageRepo.uploadProgress.value = 0
    }

    fun send(text: String){
        if (currentUser != null) {
            sendMessageRepo.sendTextMessage(
                uid = currentUser?.uid ?: "",
                name = currentUser?.name ?: "",
                text = text,
                chatId = chatId,
                token = token,
                availability = _uiState.value.availability
            )
        }
    }

    private fun checkForUnDeliveredMessages(msgs: List<Message>){
        msgs.forEach {
            if (it.state == MessageState.SENT && it.senderId != currentUid){
                sendMessageRepo.updateMessageState(it.id)
            }
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
        dbRef.child(Paths.DM)
            .child(chatId)
            .child(receiverUid)
            .child("availability").addValueEventListener(availabilityListener)
        dbRef.child(Paths.DM)
            .child(chatId)
            .child(receiverUid)
            .child("typing").addValueEventListener(typingListener)
        setAvailability(true)
    }

    fun removeListener(){
        repo.removeListener()
        dbRef.removeEventListener(availabilityListener)
        dbRef.removeEventListener(typingListener)
        job.cancel()
        chatJob.cancel()
    }

    fun setAvailability(b: Boolean){
        dbRef.child(Paths.DM)
            .child(chatId)
            .child(currentUid)
            .child("availability").setValue(b)
    }

    fun setTypingState(b: Boolean){
        dbRef.child("${Paths.DM}/$chatId/$currentUid/typing").setValue(b)
    }
}