package com.devwarex.chatapp.ui.profile

import android.graphics.Bitmap
import com.devwarex.chatapp.datastore.DatastoreImpl
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.User
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.UploadProfilePicRepo
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject


class EditProfileRepo @Inject constructor(
    private val db: AppDao,
    private val uploadImageRepo: UploadProfilePicRepo,
    private val datastoreImpl: DatastoreImpl
) {

    val uiState = MutableStateFlow(ProfileUiState())
    private val auth get() = Firebase.auth
    private val coroutine = CoroutineScope(Dispatchers.Default)
    private val firestore = Firebase.firestore
    init {
        coroutine.launch {
            launch { db.getUserById(auth.uid ?: "").collect { uiState.emit(uiState.value.copy(user = it, isLoading = it == null, name = it?.name ?: "", isNameUpdated = false))} }
            launch { uploadImageRepo.img.receiveAsFlow().collect{
                if (it.isNotEmpty()){
                    updateUserImage(it)
                }
            }}
        }
    }


    private fun updateUserImage(img: String){
        firestore.collection(Paths.USERS)
            .document(auth.uid ?: "")
            .update("img",img).addOnCompleteListener {
                if (it.isSuccessful){
                    getUserById(uid = auth.uid ?: "")
                }
            }
    }


    fun updateUserName(s: String){
        firestore.collection(Paths.USERS)
            .document(auth.uid ?: "")
            .update("name",s).addOnCompleteListener {
                if (it.isSuccessful){
                    getUserById(uid = auth.uid ?: "")
                    coroutine.launch { uiState.emit(uiState.value.copy(isNameUpdated = true)) }
                }
            }
    }

    fun uploadProfilePic(bit: Bitmap){
        uploadImageRepo.upload(bit)
    }

    fun cancelJob(){
        coroutine.cancel()
    }
    private fun getUserById(uid: String){
        firestore.collection(Paths.USERS)
            .document(uid)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()){
                    val userModel: UserModel? = task.result.toObject(UserModel::class.java)
                    if (userModel != null){
                        coroutine.launch {
                                db.insertUser(
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
            }
    }


    suspend fun deleteUserData(){
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {  }
        datastoreImpl.clear()
        db.dropMessages()
        db.dropChats()
    }
}