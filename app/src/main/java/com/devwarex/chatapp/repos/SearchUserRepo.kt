package com.devwarex.chatapp.repos

import com.devwarex.chatapp.db.Contact
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.util.Paths
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
    val isContactFound = Channel<Contact>(Channel.UNLIMITED)
    val user = Channel<UserModel?>(Channel.UNLIMITED)

    fun findUserByPhone(phone: String){
        firestore.collection(Paths.USERS)
            .whereEqualTo("phone",phone)
            .get(Source.SERVER)
            .addOnCompleteListener {
                if (it.isSuccessful && !it.result.isEmpty){
                    for (document in it.result.documents){
                        val userModel: UserModel? = document.toObject(UserModel::class.java)
                        if (userModel != null && userModel.phone == phone){
                            CoroutineScope(Dispatchers.Default).launch {
                                launch { isFound.send(true) }
                                launch { user.send(userModel) }
                            }
                        }
                    }

                }else{
                    CoroutineScope(Dispatchers.Default).launch {
                        launch { isFound.send(false) }
                        launch { user.send(null) }
                    }
                }

            }
    }


    fun findUserByContact(contact: Contact){
        firestore.collection(Paths.USERS)
            .whereEqualTo("phone",contact.phone)
            .get(Source.SERVER)
            .addOnCompleteListener {
                CoroutineScope(Dispatchers.Default).launch {
                    if (it.isSuccessful && !it.result.isEmpty) {
                        isContactFound.send(
                            Contact(
                                name = contact.name,
                                phone = contact.phone,
                                isFound = true
                            )
                        )
                    }
                }
            }
    }
}