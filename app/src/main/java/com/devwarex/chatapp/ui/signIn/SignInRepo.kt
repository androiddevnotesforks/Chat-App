package com.devwarex.chatapp.ui.signIn

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.devwarex.chatapp.repos.UpdateTokenRepo
import com.devwarex.chatapp.ui.signUp.ErrorsState
import com.devwarex.chatapp.util.Paths
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SignInRepo @Inject constructor() {


    companion object{
        private const val wrong_password_message: String = "The password is invalid or the user does not have a password."
        private const val email_not_found_message: String = "There is no user record corresponding to this identifier. The user may have been deleted."
    }

    fun clearEmail(email: String) = SignInUtility.clearEmail(email)
    private val _uiState =  MutableStateFlow<SignInUiState>(SignInUiState())
    val uiState: StateFlow<SignInUiState> get() = _uiState
    private val auth = Firebase.auth
    val isSucceed = MutableLiveData(false)
    private val coroutine = CoroutineScope(Dispatchers.Default)

    fun attemptToSignIn(elementsState: SignInUiState){
        _uiState.value = elementsState.copy(
            isLoading = true,
            errors = SignInErrorState(
                email = SignInUtility.emailError(email =elementsState.email),
                password = SignInUtility.passwordError(password = elementsState.password)
            )
        )

        if (SignInUtility.isDataValid(errors = _uiState.value.errors)){
            signIn()
        }else{
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }


    private fun signIn(){
        auth.signInWithEmailAndPassword(_uiState.value.email,_uiState.value.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                  val user = auth.currentUser
                  if (user  != null){
                      updateUser(user)
                      UpdateTokenRepo.updateToken()
                      coroutine.launch {
                          delay(300)
                          _uiState.emit(
                              value = _uiState.value.copy(isLoading = false, isSucceed = true)
                          )
                      }
                  }
                }
            }.addOnFailureListener { e ->
                Log.e("error",e.message!!)
                when(e.message){
                    wrong_password_message -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errors = _uiState.value.errors.copy(password = ErrorsState.WRONG_PASSWORD))
                    }
                    email_not_found_message -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errors = _uiState.value.errors.copy(email = ErrorsState.EMAIL_NOT_FOUND))
                    }
                    else -> Log.e("sign_in","error: ${e.message}")
                }
            }
    }

    private fun updateUser(
        user: FirebaseUser
    ){
        val db = Firebase.firestore
        db.collection(Paths.USERS)
            .document(user.uid)
            .update(
                "verified",false
            ).addOnSuccessListener {
                isSucceed.value = true
            }

    }

    fun cancelJobs() = coroutine.cancel( )
}