package com.devwarex.chatapp.ui.conversation

import android.graphics.Bitmap
import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.LocationPin
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.SendMessageRepo
import com.devwarex.chatapp.repos.UploadImageRepo
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class ConversationRepo @Inject constructor(
    private val userRepo: UserByIdRepo,
    private val database: AppDao,
    private val sendMessageRepo: SendMessageRepo,
    private val repo: MessagesRepo,
    private val uploadImageRepo: UploadImageRepo
) {

    private var currentUser: UserModel? = null
    val uiState = MutableStateFlow(MessageUiState())
    private var token: String = ""
    private var chatId: String = ""
    private val dbRef = Firebase.database(Paths.DATABASE_URL).reference
    private var currentUid: String = ""
    val shouldFetchChat = Channel<Boolean>()
    private val coroutine = CoroutineScope(Dispatchers.Unconfined)
    private var isRemoteDbLaunched = false
    val uploadProgress = uploadImageRepo.uploadProgress
    val isSendingMessage: StateFlow<Boolean> = sendMessageRepo.isLoading

    fun sync(id: String) {
        currentUid = Firebase.auth.uid ?: ""
        chatId = id
        repo.sync(chatId = chatId)
        userRepo.getUserById(currentUid)
        coroutine.launch {
            launch {
                database.getMessages(chatId = chatId).collect {
                    uiState.emit(
                        value = uiState.value.copy(
                            uid = currentUid,
                            messages = it
                        )
                    )
                    checkForUnDeliveredMessages(it)
                }
            }
        }
        coroutine.launch {
            database.getChatByChatId(chatId).collect {
                if (it?.user == null) {
                    launch {
                        shouldFetchChat.send(true)
                    }
                } else {
                    uiState.emit(
                        value = uiState.value.copy(
                            chat = it.chat,
                            receiverUser = it.user
                        )
                    )
                    if (!isRemoteDbLaunched) {
                        userRepo.getTokenByUserId(it.user.uid)
                        launchRemoteDatabase(it.user.uid)
                    }
                }
            }
        }
    }

    init {
        coroutine.launch {
            launch { userRepo.token.receiveAsFlow().collect { token = it } }
            launch { userRepo.user.receiveAsFlow().collect { currentUser = it } }
            launch {
                uploadImageRepo.img.receiveAsFlow().collect {
                    if (it.isNotEmpty()) {
                        sendMessageRepo.sendImageMessage(
                            uid = currentUser?.uid ?: "",
                            name = currentUser?.name ?: "",
                            url = it,
                            chatId = chatId,
                            token = token,
                            availability = uiState.value.availability
                        )
                        launch { uploadImageRepo.img.send("") }
                    }
                }
            }

            launch { sendMessageRepo.isDeleted.collect{
                if (it){
                    val messageId = uiState.value.deleteMessageId
                    database.deleteMessage(messageId)
                    delay(50)
                    uiState.emit(
                        value = uiState.value.copy(deleteMessageId = "")
                    )
                }
            } }
        }
    }

    fun sendImage(bitmap: Bitmap) {
        uploadImageRepo.upload(bitmap = bitmap)
    }

    fun shareLocationPin(pin: LocationPin) {
        sendMessageRepo.shareLocationPin(
            uid = currentUser?.uid ?: "",
            name = currentUser?.name ?: "",
            pin = pin,
            chatId = chatId,
            token = token,
            availability = uiState.value.availability
        )
    }

    fun zeroProgress() {
        uploadImageRepo.uploadProgress.value = 0
    }

    fun send(text: String) {
        if (currentUser != null) {
            sendMessageRepo.sendTextMessage(
                uid = currentUser?.uid ?: "",
                name = currentUser?.name ?: "",
                text = text,
                chatId = chatId,
                token = token,
                availability = uiState.value.availability
            )
        }
    }

    private fun checkForUnDeliveredMessages(msgs: List<Message>) {
        coroutine.launch {
            msgs.forEach {
                if (it.state == MessageState.SENT && it.senderId != currentUid) {
                    sendMessageRepo.updateMessageState(it.id)
                }
            }
        }
    }

    fun deleteMessage() = coroutine.launch {
        val messageId = uiState.value.deleteMessageId
        sendMessageRepo.deleteMessage(messageId)
    }

    private val availabilityListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.e("cor_${coroutine.isActive}",snapshot.value.toString())
            coroutine.launch {
                uiState.emit(
                    value = uiState.value.copy(
                        availability = snapshot.getValue<Boolean>() ?: false
                    )
                )
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    private val typingListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            coroutine.launch {
                uiState.emit(
                    value = uiState.value.copy(
                        typing = snapshot.getValue<Boolean>() ?: false
                    )
                )
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    private fun launchRemoteDatabase(receiverUid: String) {
        if (receiverUid.isEmpty()) return
        dbRef.child(Paths.DM)
            .child(chatId)
            .child(receiverUid)
            .child(Paths.AVAILABILITY).addValueEventListener(availabilityListener)
        dbRef.child(Paths.DM)
            .child(chatId)
            .child(receiverUid)
            .child(Paths.TYPING).addValueEventListener(typingListener)
        isRemoteDbLaunched = true
        setAvailability(true)
    }

    fun removeListener() {
        repo.removeListener()
        dbRef.removeEventListener(availabilityListener)
        dbRef.removeEventListener(typingListener)
        coroutine.cancel()
        sendMessageRepo.cancelJob()
    }

    fun setAvailability(b: Boolean) {
        if (currentUid.isNotEmpty() && chatId.isNotEmpty()) {
            dbRef.child(Paths.DM)
                .child(chatId)
                .child(currentUid)
                .child(Paths.AVAILABILITY).setValue(b)
        }
    }

    fun setTypingState(b: Boolean) {
        dbRef.child("${Paths.DM}/$chatId/$currentUid/${Paths.TYPING}").setValue(b)
    }
}