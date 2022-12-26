package com.devwarex.chatapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.repos.UpdateTokenRepo
import com.devwarex.chatapp.util.Paths
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
) : ViewModel() {

    private val _signIn = MutableLiveData<Boolean>()
    val signIn: LiveData<Boolean> get() = _signIn
    private val _signUp = MutableLiveData<Boolean>()
    val signUp: LiveData<Boolean> get() = _signUp
    private val _googleSignIn = MutableLiveData<Boolean>()
    val googleSignIn: LiveData<Boolean> get() = _googleSignIn
    private val _isSucceed = MutableLiveData(false)
    val isSucceed: LiveData<Boolean> get() = _isSucceed

    fun toSignIn() {
        _signIn.value = true
    }

    fun toSignUp() {
        _signUp.value = true
    }

    fun signInWithGoogle() {
        _googleSignIn.value = true
    }

    fun clearListeners() {
        _signIn.value = false
        _signUp.value = false
        _googleSignIn.value = false
    }


    fun signingWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnSuccessListener { task ->
                if (task.user != null) {
                    viewModelScope.launch {
                        saveUser(uid = task.user!!.uid, account = account)
                    }
                }
            }.addOnFailureListener { Log.e("google", it.message!!) }
    }

    private fun saveUser(uid: String, account: GoogleSignInAccount) {
        val db = Firebase.firestore
        db.collection(Paths.USERS)
            .document(uid)
            .set(
                UserModel(
                    uid = uid,
                    email = account.email ?: "",
                    name = account.displayName ?: "",
                    img = account.photoUrl.toString(),
                    phone = Firebase.auth.currentUser?.phoneNumber ?: ""
                )
            ).addOnCompleteListener {
                UpdateTokenRepo.updateToken()
                viewModelScope.launch {
                    delay(300)
                    _isSucceed.value = it.isSuccessful
                }
            }
    }
}