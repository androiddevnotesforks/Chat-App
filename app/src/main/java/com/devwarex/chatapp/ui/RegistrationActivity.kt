package com.devwarex.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.RegistrationViewModel
import com.devwarex.chatapp.ui.signIn.SignInActivity
import com.devwarex.chatapp.ui.signUp.SignUpActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.ui.theme.LightBlack
import com.devwarex.chatapp.util.Paths
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationActivity : ComponentActivity() {

    private lateinit var mGoogleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private  val viewModel: RegistrationViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { 
            ChatAppTheme() {
                Surface(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .fillMaxSize()
                ) { RegistrationScreen() }
            }
        }

        viewModel.signIn.observe(this,this::toSignIn)
        viewModel.signUp.observe(this,this::toSignUp)
        viewModel.googleSignIn.observe(this,this::signInWithGoogle)
        prepareGoogleLauncher()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Paths.FIREBASE_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
        viewModel.isSucceed.observe(this){
            if (it) {
                val homeIntent = Intent(this, MainActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }

    }



    override fun onResume() {
        super.onResume()
        val user = Firebase.auth.currentUser
        if (user != null){
            finish()
        }
    }

    private fun toSignUp(b: Boolean){
        if (b){
            val signUpIntent = Intent(this,SignUpActivity::class.java)
            startActivity(signUpIntent)
            viewModel.clearListeners()
        }
    }

    private fun toSignIn(b: Boolean){
        if (b){
            val signInIntent = Intent(this,SignInActivity::class.java)
            startActivity(signInIntent)
            viewModel.clearListeners()
        }
    }

    private fun signInWithGoogle(b: Boolean){
        if (b){
            val googleSignInIntent = mGoogleSignInClient.signInIntent
            mGoogleSignInLauncher.launch(googleSignInIntent)
            viewModel.clearListeners()
        }
    }

    private fun prepareGoogleLauncher(){
        mGoogleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == RESULT_OK){
                Log.e("launcher","${result.resultCode}")
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    viewModel.signingWithGoogle(account)
                    Log.d("google_sign_in","${account.email}, ${account.displayName}\n ${account.idToken}")
                }catch (e: ApiException){
                    Log.e("google_sign_in",e.message!!)
                }
            }else{
                Log.e("launcher","${result.resultCode}")
            }
        }
    }
}