package com.devwarex.chatapp.repos

import com.devwarex.chatapp.utility.Paths
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchUserRepo @Inject constructor() {

    private val firestore = Firebase.firestore
    val isFound = Channel<Boolean>()

    fun findUserByPhone(phone: String){
        firestore.collection(Paths.USERS)
            .whereEqualTo("phone",phone)
            .get(Source.SERVER)
            .addOnCompleteListener {
                CoroutineScope(Dispatchers.Default).launch { isFound.send(it.isSuccessful && !it.result.isEmpty) }
            }
    }
}