package com.devwarex.chatapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import com.devwarex.chatapp.R
import com.devwarex.chatapp.repos.UserByIdRepo
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.ui.verify.VerifyActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repo: UserByIdRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
        val user = Firebase.auth.currentUser
        if (user == null) {
            val signUp = Intent(this, RegistrationActivity::class.java)
            startActivity(signUp)
            finish()
        } else {
            repo.getUserById(user.uid)
            lifecycleScope.launchWhenCreated {
                launch {
                    repo.user.receiveAsFlow().collect {
                        if (it.verified && user.phoneNumber != null) {
                            val homeIntent = Intent(this@MainActivity, ChatsActivity::class.java)
                            startActivity(homeIntent)
                            finish()
                        } else {
                            val verifyIntent = Intent(this@MainActivity, VerifyActivity::class.java)
                            startActivity(verifyIntent)
                            finish()
                        }
                    }
                }
            }
        }
    }

}
