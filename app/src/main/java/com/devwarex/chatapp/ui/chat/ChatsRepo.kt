package com.devwarex.chatapp.ui.chat

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Chat
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.ui.signUp.ErrorsState
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsRepo @Inject constructor(
    private val database: AppDao,
    private val userByIdRepo: UserByIdRepo,
    private val addUserRepo: AddUserRepo
) {

    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> get() = _uiState
    private val job = CoroutineScope(Dispatchers.Unconfined)
    private var chatListener: ListenerRegistration? = null
    val error: Flow<ErrorsState> get() = addUserRepo.error.receiveAsFlow()
    val isAdded: Flow<Boolean> get() = addUserRepo.isAdded.receiveAsFlow()
    init {
        job.launch {
            launch { database.getChats().collect { _uiState.value = _uiState.value.copy(isLoading = false, chats = it) } }
            launch { userByIdRepo.user.receiveAsFlow().collect { saveUser(it) } }
        }
    }

    fun addUser(email: String){
        addUserRepo.addUser(email = email)
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
                    ).subscribeOn(Schedulers.computation())
                        .subscribe({userByIdRepo.getUser(it)},{ Log.e("save_chat","error: ${it.message}")})
                }
            }
        }
    }

    private fun saveUser(user: UserModel){
        database.insertUser(
            User(
                uid = user.uid,
                name = user.name,
                img = user.img,
                email = user.email,
                joinedAt = user.timestamp?.time ?: 0L
            )
        ).subscribeOn(Schedulers.computation())
            .subscribe({Log.e("save_user","saved")},{Log.e("save_user","error: ${it.message}")})
    }

    fun removeListener(){
        if (chatListener != null) {
            chatListener?.remove()
        }
    }


    fun clearEmail(email: String): String {
        var newEmail = ""
        if (email.isNotBlank()) {
            email.forEach { c ->
                if (!c.isWhitespace()) {
                    newEmail += c
                }
            }
        }
        return newEmail
    }
}