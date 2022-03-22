package com.devwarex.chatapp.ui.conversation

import android.util.Log
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.MessageModel
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MessagesRepo @Inject constructor(
    private val database: AppDao
) {

    fun sync(chatId: String){
        Firebase.firestore.collection(Paths.MESSAGES)
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
                                    chatId = chatId
                                )).subscribeOn(Schedulers.computation())
                                    .subscribe()
                            }
                        }

                    }
                }
            }

    }
}