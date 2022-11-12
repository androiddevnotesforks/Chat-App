package com.devwarex.chatapp.models

import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.MessageType
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
    var locationPin: LocationPin? = null
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

    constructor(
        type: MessageType = MessageType.PIN_LOCATION,
        senderId: String,
        name: String,
        locationPin: LocationPin,
        chatId: String,
        state: MessageState
    ): this() {
        this.type = type
        this.senderId = senderId
        this.name = name
        this.chatId = chatId
        this.state = state
        this.locationPin = locationPin
    }


}