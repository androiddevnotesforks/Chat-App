package com.devwarex.chatapp.models

import com.devwarex.chatapp.utility.MessageState
import com.devwarex.chatapp.utility.MessageType
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class MessageModel(){
    var id: String =""
    var name: String = ""
    var senderId: String = ""
    var body: String = ""
    var type: MessageType = MessageType.TEXT
    var chatId: String = ""
    var state: MessageState = MessageState.SENDING
    @ServerTimestamp
    val timestamp: Date? = null

    constructor(
        type: MessageType,
        senderId: String,
        name: String,
        body: String,
        chatId: String,
        state: MessageState
    ): this() {
        this.type = type
        this.senderId = senderId
        this.body = body
        this.name = name
        this.chatId = chatId
        this.state = state
    }


}