package com.devwarex.chatapp.repos

import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserByIdRepo @Inject constructor(
    private val database: AppDao
) {

    private val db = Firebase.firestore
    val user = Channel<UserModel>(Channel.UNLIMITED)
    val token = Channel<String>(Channel.UNLIMITED)
    val isVerified = Channel<Boolean>()
    val isFound = Channel<Boolean>(Channel.UNLIMITED)
    fun getUserById(uid: String){
        db.collection(Paths.USERS)
            .document(uid)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()){
                    val userModel: UserModel? = task.result.toObject(UserModel::class.java)
                    if (userModel != null){
                        CoroutineScope(Dispatchers.Unconfined)
                            .launch {
                                user.send(userModel)
                                database.insertUser(
                                    user = User(
                                        uid = userModel.uid,
                                        img = userModel.img,
                                        name = userModel.name,
                                        joinedAt = userModel.timestamp?.time ?: 0,
                                        email = userModel.email,
                                        phone = userModel.phone
                                    )
                                )
                            }
                    }
                }
                CoroutineScope(Dispatchers.Default).launch {
                    isFound.send(task.isSuccessful && task.result.exists())
                }
            }
    }

    fun getTokenByUserId(uid: String){
        if (uid.isEmpty()) return
        db.collection(Paths.TOKENS)
            .document(uid)
            .get(Source.SERVER)
            .addOnCompleteListener { CoroutineScope(Dispatchers.Unconfined).launch {
                if (it.isSuccessful && it.result != null) {
                    val t: String = it.result.get("token").toString()
                    if (t != "null") {
                        token.send(t)
                    }
                }
            } }
    }

    fun verifyAccount(phone: String){
        val user = Firebase.auth.currentUser
        if (user !=  null){
            db.collection(Paths.USERS)
                .document(user.uid)
                .update("verified",true,
                "phone",phone)
                .addOnCompleteListener { CoroutineScope(Dispatchers.Default).launch { isVerified.send(it.isSuccessful) } }
        }
    }
}