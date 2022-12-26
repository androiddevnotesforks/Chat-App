package com.devwarex.chatapp.repos

import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.models.LocationPin
import com.devwarex.chatapp.models.MessageModel
import com.devwarex.chatapp.models.notification.MessageNotificationModel
import com.devwarex.chatapp.models.notification.MessageNotifyDataModel
import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.MessageType
import com.devwarex.chatapp.util.Paths
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SendMessageRepo @Inject constructor(
    private val database: AppDao
) {
    private val db = Firebase.firestore
    val isLoading = Channel<Boolean>()
    private val job = CoroutineScope(Dispatchers.Unconfined)
    fun sendTextMessage(
        chatId: String,
        uid: String,
        text: String,
        name: String,
        token: String,
        availability: Boolean
    ){
        job.launch { isLoading.send(true) }
        db.collection(Paths.MESSAGES)
            .add(
                MessageModel(
                    senderId = uid,
                    type = MessageType.TEXT,
                    body = text,
                    name = name,
                    chatId = chatId,
                    state = MessageState.SENT
                )
            ).addOnCompleteListener { job.launch {
                isLoading.send(!it.isSuccessful) }
                updateLastMessage(chatId = chatId, lastMessage = text)
                if (token.isNotEmpty() && !availability) {
                    PushNotificationRepo.push(
                        fcm = MessageNotificationModel(
                            to = token,
                            data = MessageNotifyDataModel(
                                title = name,
                                id = chatId,
                                body = if (text.length > 28) text.take(28) + "..." else text
                            )
                        )
                    )
                }
            }
            .addOnFailureListener { job.launch { isLoading.send(false) } }
    }


    fun shareLocationPin(
        chatId: String,
        uid: String,
        pin: LocationPin,
        name: String,
        token: String,
        availability: Boolean
    ){
        job.launch { isLoading.send(true) }
        db.collection(Paths.MESSAGES)
            .add(
                MessageModel(
                    senderId = uid,
                    type = MessageType.PIN_LOCATION,
                    locationPin = pin,
                    name = name,
                    chatId = chatId,
                    state = MessageState.SENT
                )
            ).addOnCompleteListener { job.launch {
                isLoading.send(!it.isSuccessful) }
                updateLastMessage(chatId = chatId, lastMessage = "location_pin")
                if (token.isNotEmpty() && !availability) {
                    PushNotificationRepo.push(
                        fcm = MessageNotificationModel(
                            to = token,
                            data = MessageNotifyDataModel(
                                title = name,
                                id = chatId,
                                body = "Shared Location"
                            )
                        )
                    )
                }
            }
            .addOnFailureListener { job.launch { isLoading.send(false) } }
    }
    fun sendImageMessage(
        chatId: String,
        uid: String,
        url: String,
        name: String,
        token: String,
        availability: Boolean
    ){
        job.launch { isLoading.send(true) }
        db.collection(Paths.MESSAGES)
            .add(
                MessageModel(
                    senderId = uid,
                    type = MessageType.IMAGE,
                    body = url,
                    name = name,
                    chatId = chatId,
                    state = MessageState.SENT
                )
            ).addOnCompleteListener { job.launch {
                isLoading.send(!it.isSuccessful) }
                updateLastMessage(chatId = chatId, lastMessage = "IMAGE")
                if (token.isNotEmpty() && !availability) {
                    PushNotificationRepo.push(
                        fcm = MessageNotificationModel(
                            to = token,
                            data = MessageNotifyDataModel(
                                title = name,
                                id = chatId,
                                body = "sent photo"
                            )
                        )
                    )
                }
            }
            .addOnFailureListener { job.launch { isLoading.send(false) } }
    }

    fun updateMessageState(id: String){
        db.collection(Paths.MESSAGES)
            .document(id)
            .update("state",MessageState.DELIVERED)
            .addOnCompleteListener {  }
    }

    private fun updateLastMessage(chatId: String,lastMessage: String){
        db.collection(Paths.CHATS)
            .document(chatId).update(
                "lastMessage",lastMessage,
                "lastEdit",FieldValue.serverTimestamp()
            ).addOnCompleteListener {  }
    }
}