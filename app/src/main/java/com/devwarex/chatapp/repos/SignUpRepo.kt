package com.devwarex.chatapp.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.utility.Paths
import com.devwarex.chatapp.utility.SignUpUtility
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SignUpRepo @Inject constructor(
    private val utility: SignUpUtility
) {

    val isLoading =  MutableStateFlow<Boolean>(false)
    private val auth = Firebase.auth
    val isSucceed = MutableLiveData(false)
    private fun setLoadingState(b: Boolean){
        isLoading.value = b
    }

    fun signUp(){
        setLoadingState(true)
        registerUser()
    }

    private fun registerUser(){
       auth.createUserWithEmailAndPassword(utility.email, utility.password)
           .addOnCompleteListener {
               if (it.isSuccessful){
                   val user = auth.currentUser
                   if (user != null){
                       saveUser(user.uid)
                   }
               }
           }.addOnFailureListener {
               setLoadingState(false)
               Log.e("signUp",it.message!!)
           }
    }

    private fun saveUser(uid: String){
        val db = Firebase.firestore
        db.collection(Paths.USERS)
            .document(uid)
            .set(
                UserModel(
                    uid = uid,
                    email = utility.email,
                    name = utility.name
                )
            ).addOnCompleteListener {
                setLoadingState(false)
                isSucceed.value = it.isSuccessful
            }
    }
}