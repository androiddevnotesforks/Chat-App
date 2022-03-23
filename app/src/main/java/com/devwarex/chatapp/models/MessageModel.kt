package com.devwarex.chatapp.models

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
    @ServerTimestamp
    val timestamp: Date? = null

    constructor(type: MessageType,senderId: String,name: String,body: String,chatId: String): this() {
        this.type = type
        this.senderId = senderId
        this.body = body
        this.name = name
        this.chatId = chatId

    }


}