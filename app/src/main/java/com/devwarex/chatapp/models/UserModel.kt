package com.devwarex.chatapp.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

class UserModel{
    var name: String = ""
    var email: String = ""
    var uid: String = ""
    var img: String = ""
    var phone: String = ""
    var verified: Boolean = false
    @ServerTimestamp
    val timestamp: Date? = null

    constructor()
    constructor(
        name: String,
        email: String,
        uid: String,
        verified: Boolean = false
    ): this(){
        this.name = name
        this.email = email
        this.uid = uid
    }
    constructor(
        name: String,
        email: String,
        uid: String,
        img: String,
        phone: String,
        verified: Boolean = false
    ): this(){
        this.name = name
        this.email = email
        this.uid = uid
        this.img = img
        this.verified = verified
        this.phone = phone
    }
}
