package com.devwarex.chatapp.ui.signUp

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import com.devwarex.chatapp.models.UserModel
import com.devwarex.chatapp.utility.Paths
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SignUpRepo @Inject constructor() {

    companion object{
        private const val emailExistMessage = "The email address is already in use by another account."
    }
    private val _uiState =  MutableStateFlow<SignUpUiState>(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> get() = _uiState
    private val auth = Firebase.auth
    val isSucceed = MutableLiveData(false)
    fun signUp(elementState: SignUpUiState){
        _uiState.value = elementState.copy(isLoading = true, errors = SignUpErrors(
            email = SignUpUtility.emailError(email = elementState.email),
            name = SignUpUtility.nameError(name = elementState.name),
            password = SignUpUtility.passwordError(
                password = elementState.password,
                name = elementState.name,
                email = elementState.email
            ),
            confirmPassword = SignUpUtility.confirmPasswordError(
                password = elementState.password,
                confirmPassword = elementState.confirmPassword
            )
        )
        )
        if (SignUpUtility.isDataValid(_uiState.value.errors)) {
            registerUser(elementState = _uiState.value)
        }else{
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun filterEmail(s: String): String = SignUpUtility.filterEmail(s)

    private fun registerUser(elementState: SignUpUiState){
       auth.createUserWithEmailAndPassword(elementState.email,elementState.password)
           .addOnCompleteListener {
               if (it.isSuccessful){
                   val user = auth.currentUser
                   if (user != null){
                       _uiState.value = elementState.copy(isSucceedToSignUp = true, isLoading = false)
                       saveUser(user.uid,elementState)
                   }
               }
           }.addOnFailureListener {
               Log.e("signUp",it.message!!)
               if (it.message == emailExistMessage) {
                   _uiState.value =
                       elementState.copy(isLoading = false, errors = elementState.errors.copy(email = ErrorsState.EMAIL_EXIST))
               }
           }
    }

    private fun saveUser(uid: String,elementState: SignUpUiState){
        val db = Firebase.firestore
        db.collection(Paths.USERS)
            .document(uid)
            .set(
                UserModel(
                    uid = uid,
                    email = elementState.email,
                    name = elementState.name
                )
            ).addOnCompleteListener {
                isSucceed.value = it.isSuccessful
            }
    }
}