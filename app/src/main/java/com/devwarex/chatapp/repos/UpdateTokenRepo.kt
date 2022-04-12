package com.devwarex.chatapp.repos

import android.util.Log
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class UpdateTokenRepo {

    companion object {
        private val user get() = Firebase.auth.currentUser
        fun updateToken(){
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful && user != null){
                    setDeviceToken(user?.uid ?: "",it.result)
                }
            }
        }
        fun setDeviceToken(uid: String,token: String){
            if (uid.isNotEmpty()){
                val map:Map<String,String> = mapOf("token" to token)
                Firebase.firestore.collection(Paths.TOKENS)
                    .document(uid)
                    .set(map)
                    .addOnCompleteListener { Log.d("token","updated") }
                    .addOnFailureListener { Log.e("token","error: ${it.message}") }
            }
        }

    }


}