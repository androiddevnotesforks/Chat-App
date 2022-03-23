package com.devwarex.chatapp.ui.conversation

import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.MessageModel
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MessagesRepo @Inject constructor(
    private val database: AppDao
) {
    private var messagesListener: ListenerRegistration? = null

    fun sync(chatId: String){
        messagesListener = Firebase.firestore.collection(Paths.MESSAGES)
            .whereEqualTo("chatId",chatId)
            .addSnapshotListener { task, error ->
                if (task != null){
                    for (document in task.documents){
                        val message = document.toObject(MessageModel::class.java)
                        if (message != null){
                            message.id = document.id
                            if (message.timestamp != null){
                                database.insertMessage(Message(
                                    id = message.id,
                                    senderId = message.senderId,
                                    body = message.body,
                                    timestamp = message.timestamp.time,
                                    name = message.name,
                                    type = message.type,
                                    chatId = message.chatId
                                )).subscribeOn(Schedulers.computation())
                                    .subscribe()
                            }
                        }

                    }
                }
            }
    }


    fun removeListener(){
        if (messagesListener !=  null){
            messagesListener?.remove()
        }
    }
}