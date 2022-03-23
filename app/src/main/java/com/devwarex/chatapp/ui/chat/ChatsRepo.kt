package com.devwarex.chatapp.ui.chat

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Chat
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
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

    init {
        sync(Firebase.auth.uid)
        job.launch {
            launch { database.getChats().collect { _uiState.value = _uiState.value.copy(isLoading = it.isEmpty(), chats = it) } }
            launch { userByIdRepo.user.receiveAsFlow().collect { database.insertUser(
                User(
                    uid = it.uid,
                    name = it.name,
                    img = it.img,
                    email = it.email,
                    joinedAt = it.timestamp?.time ?: 0L
                )
            ).subscribeOn(Schedulers.computation())
                .subscribe()
            } }
        }
    }


    fun sync(uid: String?){
        if (uid == null) return
        db.collection(Paths.CHATS)
            .whereArrayContains("ids",uid)
            .get().addOnCompleteListener { task ->
                Log.e("task","${task.isSuccessful} , ${task.result.size()}")
               if (task.isSuccessful){
                   for (document in task.result.documents){
                       val chat = document.toObject(ChatModel::class.java)
                       Log.e("chat",Gson().toJson(chat))
                       if (chat != null){
                           job.launch {
                               chat.ids.forEach {
                                   if (it != uid) {
                                       database.insertChat(
                                           Chat(
                                               id = chat.id,
                                               lastEditAt = chat.lastEdit?.time ?: 0L,
                                               lastMessage = chat.lastMessage ?: "",
                                               createdAt = chat.timestamp?.time ?: 0L,
                                               receiverUid = it
                                           )
                                       ).subscribeOn(Schedulers.computation())
                                           .subscribe({userByIdRepo.getUser(it)},{Log.e("save_chat","error: ${it.message}")})
                                   }
                               }
                           }
                       }
                   }
               }
            }
    }
}