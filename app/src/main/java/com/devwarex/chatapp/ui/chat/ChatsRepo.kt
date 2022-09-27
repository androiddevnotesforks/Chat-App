package com.devwarex.chatapp.ui.chat

import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Chat
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsRepo @Inject constructor(
    private val database: AppDao,
    private val userByIdRepo: UserByIdRepo
    ) {

    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> get() = _uiState
    private val job = CoroutineScope(Dispatchers.Unconfined)
    private var chatListener: ListenerRegistration? = null

    init {
        job.launch {
            launch { database.getChats().collect { _uiState.value = _uiState.value.copy(isLoading = false, chats = it) } }
            launch { userByIdRepo.user.receiveAsFlow().collect { saveUser(it) } }
        }
    }


    fun sync(){
        val uid = Firebase.auth.uid ?: return
        chatListener = db.collection(Paths.CHATS)
            .whereArrayContains("ids",uid)
            .addSnapshotListener { value, error ->
                if (value != null){
                    if (!value.isEmpty){
                        getChats(uid = uid)
                    }
                }
            }
    }


    private fun getChats(uid: String){
        db.collection(Paths.CHATS)
            .whereArrayContains("ids",uid)
            .get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    for (document in task.result.documents){
                        val chat = document.toObject(ChatModel::class.java)
                        if (chat != null){
                           saveChatToDb(uid = uid, chat = chat)
                        }
                    }
                }
            }
    }


    private fun saveChatToDb(uid: String,chat: ChatModel){
        job.launch {
            chat.ids.forEach {
                if (it != uid && chat.id.isNotEmpty()) {
                    database.insertChat(
                        Chat(
                            id = chat.id,
                            lastEditAt = chat.lastEdit?.time ?: 0L,
                            lastMessage = chat.lastMessage ?: "",
                            createdAt = chat.timestamp?.time ?: 0L,
                            receiverUid = it
                        )
                    )
                }
            }
        }
    }

    private fun saveUser(user: UserModel){
        job.launch {
            database.insertUser(
                User(
                    uid = user.uid,
                    name = user.name,
                    img = user.img,
                    email = user.email,
                    phone = user.phone,
                    joinedAt = user.timestamp?.time ?: 0L
                )
            )
        }

    }

    fun removeListener(){
        if (chatListener != null) {
            chatListener?.remove()
        }
    }
}