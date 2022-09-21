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
import androidx.hilt.navigation.compose.hiltViewModel
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
                ) { MainScreen() }
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


@Composable
fun MainScreen(){
    val viewModel = hiltViewModel<RegistrationViewModel>()
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "app logo",
            modifier = Modifier
                .padding(top = 32.dp)
                .align(CenterHorizontally)
                .size(72.dp),
            tint = MaterialTheme.colors.onSurface
        )
        Text(
            text = stringResource(id = R.string.app_name),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(top = 16.dp),
            fontWeight = FontWeight.Bold

        )
        Spacer(modifier = Modifier.padding(top = 72.dp))
        Button(
            onClick = { viewModel.signInWithGoogle() },
            elevation = ButtonDefaults.elevation(4.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.surface
            ),
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Row() {
                Image(
                    painter = painterResource(id = R.drawable.googleg_standard_color_18),
                    contentDescription = "google icon"
                )
                Text(
                    text = stringResource(id = R.string.google_sign_in_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 72.dp))
        Text(text = stringResource(id = R.string.or_title), modifier = Modifier.align(CenterHorizontally))
        Spacer(modifier = Modifier.padding(top = 72.dp))
        Button(
            onClick = { viewModel.toSignIn() },
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Row() {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "email sign in",
                    tint = MaterialTheme.colors.onBackground
                )
                Text(
                    text = stringResource(id = R.string.sign_in_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 16.dp))
        Button(
            onClick = { viewModel.toSignUp() },
            modifier = Modifier.align(CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary
            )
        ) {
            Row() {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "email sign up",
                    tint = LightBlack
                )
                Text(
                    text = stringResource(id = R.string.sign_up_title),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    color = LightBlack
                )
            }
        }
    }
}