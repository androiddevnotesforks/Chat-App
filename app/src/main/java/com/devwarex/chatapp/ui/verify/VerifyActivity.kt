package com.devwarex.chatapp.ui.verify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.devwarex.chatapp.R
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VerifyActivity : ComponentActivity() {

    private var storedVerificationId: String = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCredential: PhoneAuthCredential
    private lateinit var options: PhoneAuthOptions.Builder
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

        options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks

        lifecycleScope.launchWhenCreated {
            launch {
                viewModel.uistate.collect {
                    if (it.requestingCode){
                        launch { viewModel.phone.collect { phone -> verifying(phone)} }
                    }

                    if (it.verifying){
                        launch { viewModel.code.collect { code -> verifyWithCredential(code) } }
                    }
                }
            }

            launch { viewModel.isVerified.collect { updateUi(it) } }
        }
    }

    private fun verifying(phone: String){
        options.setPhoneNumber("+2$phone")
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    private fun linkWithPhoneAuthCredential(){
        Firebase.auth.currentUser!!.linkWithCredential(mCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) viewModel.onSuccess()
                viewModel.verifyAccount()
            }.addOnFailureListener { Log.e("link","account: ${it.message}") }
    }


    private fun verifyWithCredential(code: String){
        mCredential = PhoneAuthProvider.getCredential(storedVerificationId,code)
        linkWithPhoneAuthCredential()
    }

    private fun updateUi(b: Boolean){
        if (b){
            val homeIntent = Intent(this, ChatsActivity::class.java)
            startActivity(homeIntent)
            finish()
        }
    }

}


@Composable
fun VerifyScreen(modifier: Modifier = Modifier){
    val viewModel = hiltViewModel<VerifyViewModel>()
    val (sent,requestingCode,verifying,success) = viewModel.uistate.collectAsState().value
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = ScrollState(0))
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.primary,
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.verify_its_you),
            modifier = modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.verify_message),
            modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = modifier.height(16.dp))
        val phone = viewModel.phone.collectAsState()
        Row() {
            Text(
                text = "+2",
                modifier = modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
                )
            OutlinedTextField(
                value = phone.value,
                onValueChange = viewModel::setPhone,
                singleLine = true,
                enabled = !sent || !verifying || !success,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.background
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp),
                label = { Text(text = stringResource(id = R.string.phone_title)) },
                placeholder = { Text(text = stringResource(id = R.string.phone_title)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )  
        }
        Spacer(modifier = modifier.height(16.dp))
        if (!requestingCode && !sent && !verifying && !success) {
            Button(onClick = { viewModel.onRequestCode() }, modifier = modifier.align(Alignment.CenterHorizontally)) {
                Text(text = "Get verification code")
            }
        }
        if (requestingCode){
            LinearProgressIndicator(modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp)
                .fillMaxSize()
            )
        }
        if (sent){
            val code = viewModel.code.collectAsState()
            OutlinedTextField(
                value = code.value,
                onValueChange = viewModel::setCode,
                singleLine = true,
                enabled = !verifying,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colors.onSurface,
                    backgroundColor = MaterialTheme.colors.background
                ),
                modifier = modifier
                    .padding(end = 16.dp, start = 16.dp)
                    .align(Alignment.CenterHorizontally),
                label = { Text(text = stringResource(id = R.string.verification_code)) },
                placeholder = { Text(text = stringResource(id = R.string.code_title)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = modifier.height(16.dp))
            Button(
                onClick = {viewModel.onVerify() },
                colors =ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary) ,
                modifier = modifier.align(Alignment.CenterHorizontally)) {
                Text(text = "Verify")
            }
        }

        if (verifying){
            LinearProgressIndicator(modifier = modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = 32.dp)
                .fillMaxSize()
            )
        }

    }
}