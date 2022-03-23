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
import com.devwarex.chatapp.R
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.signUp.SignUpActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
               Screen()
            }
        }

        val user = Firebase.auth.currentUser
        if (user == null){
            val signUp = Intent(this, SignUpActivity::class.java)
            startActivity(signUp)
            finish()
        }else{
            val homeIntent = Intent(this, ChatsActivity::class.java)
                .putExtra("chat_id","testing chat id ya rab")
            startActivity(homeIntent)
            finish()
        }
    }


    @Composable
    fun Screen(modifier: Modifier = Modifier){

        CustomLayout(modifier = modifier) {
            Text(
                text = stringResource(id = R.string.email_exist_message),
                color = MaterialTheme.colors.primary,
                style = MaterialTheme.typography.h3,
                modifier = modifier,
                fontWeight = FontWeight.Bold
            )
        }




    }

    @Composable
    fun CustomLayout(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ){
        Layout(
            modifier = modifier,
            content = content
        ){ measurables, constraints ->

            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            layout(constraints.maxWidth, constraints.maxHeight){
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = constraints.maxWidth/5, y = constraints.maxHeight/2)
                }
            }
        }
    }
}