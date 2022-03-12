package com.devwarex.chatapp.models

class UserModel(){
    var id: String = ""
    var name: String = ""
    var email: String = ""
    var uid: String = ""
    var deviceToken: String = ""
    var img: String = ""
    constructor(
        name: String,
        email: String,
        uid: String
    ): this(){
        this.name = name
        this.email = email
        this.uid = uid
    }
}
