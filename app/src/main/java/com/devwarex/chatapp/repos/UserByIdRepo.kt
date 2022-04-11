package com.devwarex.chatapp.repos

import android.util.Log
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserByIdRepo @Inject constructor() {

    private val db = Firebase.firestore
    val user = Channel<UserModel>(Channel.UNLIMITED)
    val token = Channel<String>(Channel.UNLIMITED)
    fun getUser(uid: String){
        db.collection(Paths.USERS)
            .document(uid)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()){
                    val userModel: UserModel? = task.result.toObject(UserModel::class.java)
                    if (userModel != null){
                        CoroutineScope(Dispatchers.Unconfined)
                            .launch { user.send(userModel) }
                    }
                }
            }
    }


    fun getTokenByUserId(uid: String){
        if (uid.isEmpty()) return
        db.collection(Paths.TOKENS)
            .document(uid)
            .get(Source.SERVER)
            .addOnCompleteListener { CoroutineScope(Dispatchers.Unconfined).launch {
                val t: String = it.result.get("token").toString()
                if (t != "null"){
                    token.send(t)
                }
            } }
    }
}