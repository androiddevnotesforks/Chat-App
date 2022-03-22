package com.devwarex.chatapp.repos

import com.devwarex.chatapp.models.MessageModel
import com.devwarex.chatapp.utility.MessageType
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SendMessageRepo @Inject constructor() {
    private val db = Firebase.firestore
    val isLoading = Channel<Boolean>()
    private val job = CoroutineScope(Dispatchers.Unconfined)
    fun sendTextMessage(uid: String,text: String,name: String){
        job.launch { isLoading.send(true) }
        db.collection(Paths.MESSAGES)
            .add(
                MessageModel(
                    senderId = uid,
                    type = MessageType.TEXT,
                    body = text,
                    name = name
                )
            ).addOnCompleteListener { job.launch { isLoading.send(!it.isSuccessful) } }
            .addOnFailureListener { job.launch { isLoading.send(false) } }
    }
}