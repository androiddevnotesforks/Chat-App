package com.devwarex.chatapp.ui.conversation

import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.MessageModel
import com.devwarex.chatapp.util.Paths
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessagesRepo @Inject constructor(
    private val database: AppDao
) {
    private lateinit var messagesListener: ListenerRegistration
    private val coroutine = CoroutineScope(Dispatchers.Unconfined)

    fun sync(chatId: String){
        messagesListener = Firebase.firestore.collection(Paths.MESSAGES)
            .whereEqualTo("chatId",chatId)
            .addSnapshotListener { task, error ->
                if (task != null) {
                    coroutine.launch {
                        for (document in task.documents) {
                            val message = document.toObject(MessageModel::class.java)
                            if (message != null) {
                                message.id = document.id
                                if (message.timestamp != null) {
                                    database.insertMessage(
                                        Message(
                                            id = message.id,
                                            senderId = message.senderId,
                                            body = message.body,
                                            timestamp = message.timestamp.time,
                                            name = message.name,
                                            type = message.type,
                                            chatId = message.chatId,
                                            state = message.state,
                                            pin_lat = message.locationPin?.lat ?: 0.0,
                                            pin_lng = message.locationPin?.lng ?: 0.0
                                        )
                                    )
                                }
                            }

                        }
                    }
                }
            }
    }


    fun removeListener(){
        if (this::messagesListener.isInitialized){
            messagesListener.remove()
            coroutine.cancel()
        }
    }
}