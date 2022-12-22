package com.devwarex.chatapp.ui.verify

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.devwarex.chatapp.R
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.util.NetworkUtil
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class VerifyActivity : ComponentActivity() {

    private var storedVerificationId: String = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCredential: PhoneAuthCredential
    private lateinit var options: PhoneAuthOptions.Builder
    private lateinit var phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val viewModel by viewModels<VerifyViewModel>()

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d("TAG", "onVerificationCompleted:$credential")
            mCredential = credential
            linkWithPhoneAuthCredential()
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.e("TAG", "onVerificationFailed", e)
            Log.e("sms","${e.message}")
            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("TAG", "onCodeSent:$verificationId")
            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
            viewModel.onCodeSent()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    VerifyScreen()
                }
            }
        }
        phoneNumberHintIntentResultLauncher = preparePhoneHintLauncher()
        val phone =getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (NetworkUtil.isMobileConnectedToInternet(this)){
            var code: String? = phone.simCountryIso
            code = code ?: phone.networkCountryIso
            viewModel.getCountries()
            viewModel.getCountryCode(code ?: "eg")
        }else{
            Toast.makeText(this,getString(R.string.offline_message),Toast.LENGTH_LONG).show()
        }
        options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks

        requestPhoneNumberHint()

        lifecycleScope.launchWhenCreated {
            launch {
                viewModel.uiState.collect {
                    if (it.requestingCode && it.selectedCountry != null){
                            verifying("${it.selectedCountry.idd.root}${it.selectedCountry.idd.suffixes[0]}${it.phone}")
                    }

                    if (it.verifying){
                        viewModel.codeNumber.observe(this@VerifyActivity){ code ->
                            verifyWithCredential(code = code)
                            viewModel.codeNumber.removeObservers(this@VerifyActivity)
                        }
                    }
                }
            }

            launch { viewModel.isVerified.collect { updateUi(it) } }
        }
    }

    private fun verifying(phone: String){
        options.setPhoneNumber(phone)
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    private fun linkWithPhoneAuthCredential(){
        Firebase.auth.currentUser!!.linkWithCredential(mCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null){
                    viewModel.onSuccess()
                    viewModel.verifyAccount()
                    Log.e("user","Verify!")
                }else{
                    Log.e("user","null")
                }
            }.addOnFailureListener {
                when(it.message){
                    WRONG_CODE_MESSAGE -> {
                        viewModel.onWrongCode()
                        Toast.makeText(
                            this,
                            getString(R.string.wrong_code),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    PHONE_LINKED_TO_ANOTHER_EMAIL_MESSAGE -> {
                        viewModel.onPhoneIsWrong()
                        Toast.makeText(
                            this,
                            getString(R.string.wrong_phone_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    ALREADY_LINKED -> signIn()
                    else ->  Log.e("link","account: ${it.message}")
                }
            }
    }

    private fun preparePhoneHintLauncher(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val phoneNumber = Identity.getSignInClient(this)
                        .getPhoneNumberFromIntent(result.data)
                    println("phoneNumber $phoneNumber")
                    viewModel.setHintPhone(phoneNumber)
                } catch (e: Exception) {
                    println("Phone Number Hint failed")
                    e.printStackTrace()
                }
            }else{
                Log.e("phone","cancelled")
            }
        }
    }

    private fun requestPhoneNumberHint(){
        val request: GetPhoneNumberHintIntentRequest =
            GetPhoneNumberHintIntentRequest.builder().build()
        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener {
                try {
                    phoneNumberHintIntentResultLauncher.launch(IntentSenderRequest.Builder(
                        it.intentSender
                    ).build())
                } catch(e: Exception) {
                    Log.e(ContentValues.TAG, "Launching the PendingIntent failed")
                }
            }.addOnFailureListener {
                Log.e(ContentValues.TAG, "${it.message}")
            }
    }

    private fun signIn(){
        Firebase.auth.signInWithCredential(mCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("sign_in", "signInWithCredential:success")
                    viewModel.verifyAccount()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("sign_in", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun verifyWithCredential(code: String){
        if (storedVerificationId.isNotEmpty() && code.isNotBlank()) {
            mCredential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            linkWithPhoneAuthCredential()
        }
    }

    private fun updateUi(b: Boolean){
        if (b){
            val homeIntent = Intent(this, ChatsActivity::class.java)
            startActivity(homeIntent)
            finish()
        }
    }

    companion object{
        const val WRONG_CODE_MESSAGE: String = "The sms verification code used to create the phone auth credential is invalid. Please resend the verification code sms and be sure use the verification code provided by the user."
        const val PHONE_LINKED_TO_ANOTHER_EMAIL_MESSAGE: String = "This credential is already associated with a different user account."
        const val ALREADY_LINKED = "User has already been linked to the given provider."
    }

}
