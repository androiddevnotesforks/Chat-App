package com.devwarex.chatapp.ui.chat

import com.devwarex.chatapp.models.ChatModel
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.ui.signUp.ErrorsState
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddUserRepo @Inject constructor() {
    private val firestore = Firebase.firestore
    private var user: UserModel? = null
    val isAdded = Channel<Boolean>(Channel.UNLIMITED)
    val error = Channel<ErrorsState>(Channel.UNLIMITED)
    fun addUser(email: String){
        checkIfUserExist(email = email)
    }


    private fun checkIfUserExist(email: String){
        firestore.collection(Paths.USERS)
            .whereEqualTo("email",email)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (task.result.isEmpty){
                        //not found
                        CoroutineScope(Dispatchers.Unconfined).launch { error.send(ErrorsState.EMAIL_NOT_FOUND) }
                    }else{
                        //found
                        for (document in task.result.documents){
                            user = document.toObject(UserModel::class.java)
                            checkUserAddedAlready(user?.uid ?: "")
                        }
                    }
                }else{
                    //error
                }
            }
    }

    private fun checkUserAddedAlready(uid: String){
        if (uid.isEmpty()) return
        firestore.collection(Paths.CHATS)
            .whereArrayContains("ids",uid)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.isEmpty) {
                    //add user
                        createChat()
                    } else {
                    //chat exist
                        CoroutineScope(Dispatchers.Unconfined).launch { isAdded.send(false) }
                    }
                }
            }
    }

    private fun createChat(){
        val currentUid: String = Firebase.auth.uid ?: ""
        if (currentUid.isNotEmpty() && user != null){
            firestore.collection(Paths.CHATS)
                .add(
                    ChatModel(
                        ids = listOf(currentUid,user?.uid ?: "")
                    )
                ).addOnCompleteListener { ref ->
                    ref.result.update("id",ref.result.id)
                    .addOnCompleteListener { CoroutineScope(Dispatchers.Unconfined).launch { isAdded.send(true) } }
                }
        }
    }
}