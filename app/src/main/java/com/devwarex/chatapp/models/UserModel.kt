package com.devwarex.chatapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class UserModel(){
    var id: String = ""
    var name: String = ""
    var email: String = ""
    var uid: String = ""
    var deviceToken: String = ""
    var img: String = ""
    var verified: Boolean = false
    @ServerTimestamp
    val timestamp: Date? = null
    constructor(
        name: String,
        email: String,
        uid: String
    ): this(){
        this.name = name
        this.email = email
        this.uid = uid
    }

    constructor(
        name: String,
        email: String,
        uid: String,
        img: String
    ): this(){
        this.name = name
        this.email = email
        this.uid = uid
        this.img = img
    }
}
