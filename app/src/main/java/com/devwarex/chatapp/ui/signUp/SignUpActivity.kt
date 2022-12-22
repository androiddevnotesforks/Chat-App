package com.devwarex.chatapp.ui.signUp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.ui.verify.VerifyActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SignUpScreen()
                }
            }
        }
        val viewModel: SignUpViewModel by viewModels()
        viewModel.isSucceed.observe(this){
            if (it) {
                val homeIntent = Intent(this, VerifyActivity::class.java)
                startActivity(homeIntent)
                finish()
            }
        }
    }
}