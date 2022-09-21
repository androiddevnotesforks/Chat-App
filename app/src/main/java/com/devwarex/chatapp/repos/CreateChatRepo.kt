package com.devwarex.chatapp.repos

import android.util.Log
import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateChatRepo @Inject constructor() {

    private val firestore = Firebase.firestore
    private var user: UserModel? = null
    val chatId = Channel<String?>(Channel.UNLIMITED)
    private val currentUser = FirebaseAuth.getInstance().currentUser
    fun create(user: UserModel){
        this.user = user
        checkUserAddedAlready()
    }

    private fun checkUserAddedAlready(){
        if (user == null) return
        firestore.collection(Paths.CHATS)
            .whereArrayContains("ids",user?.uid!!)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.isEmpty) {
                        //add user
                        createChat()
                    } else {
                        //chat exist
                        Log.e("chats","size: ${task.result.size()}")
                        var isFound = false
                        for (document in task.result.documents){
                            val chatModel: ChatModel? = document.toObject<ChatModel>()
                            if (chatModel != null && currentUser != null) {
                                chatModel.ids.forEach {
                                    if (it == currentUser.uid){
                                        CoroutineScope(Dispatchers.Unconfined).launch { chatId.send(chatModel.id) }
                                        isFound = true
                                    }
                                }

                            }
                        }

                        if (!isFound){
                            createChat()
                        }
                    }
                }
            }
    }

    private fun createChat(){
        if (currentUser != null && user != null){
            firestore.collection(Paths.CHATS)
                .add(
                    ChatModel(
                        ids = listOf(currentUser.uid,user?.uid ?: "")
                    )
                ).addOnCompleteListener { ref ->
                    ref.result.update("id",ref.result.id)
                        .addOnCompleteListener { CoroutineScope(Dispatchers.Unconfined).launch { chatId.send(ref.result.id) } }
                }
        }
    }
}