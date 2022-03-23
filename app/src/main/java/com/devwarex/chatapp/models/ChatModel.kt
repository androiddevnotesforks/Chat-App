package com.devwarex.chatapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class ChatModel{
    var id: String = ""
    var ids: List<String> = listOf()
    var lastMessage: String? = null

    var lastEdit: Date? = null

    @ServerTimestamp
    var timestamp: Date? = null


    constructor(){}
    constructor(ids: List<String>) {
        this.ids = ids
    }

}
