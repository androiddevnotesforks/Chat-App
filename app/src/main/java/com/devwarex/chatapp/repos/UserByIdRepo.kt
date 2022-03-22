package com.devwarex.chatapp.repos

import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserByIdRepo @Inject constructor() {


    val user = Channel<UserModel>(Channel.UNLIMITED)
    fun getUser(uid: String){
        Firebase.firestore.collection(Paths.USERS)
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
}