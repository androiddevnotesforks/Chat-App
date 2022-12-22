package com.devwarex.chatapp.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.devwarex.chatapp.ui.contacts.ContactsActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.profile.ProfileActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.util.BroadCastUtility
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatsActivity : ComponentActivity(){

    private val viewModel:  ChatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChatsScreen()
                }
            }
        }
        viewModel.sync()
        viewModel.chatId.observe(this,this::toConversation)
        lifecycleScope.launchWhenStarted {
            launch { viewModel.toContacts.collectLatest { if (it){ toContacts()} } }
            launch { viewModel.toProfile.collect{ if (it) toProfile() } }
        }
    }

    private fun toProfile(){
        val profileIntent = Intent(this,ProfileActivity::class.java)
        viewModel.removeToContactsObserver()
        startActivity(profileIntent)
    }

    private fun toContacts(){
        val intent = Intent(this,ContactsActivity::class.java)
        startActivity(intent)
        viewModel.removeToContactsObserver()
    }

    override fun onStart() {
        super.onStart()
        if (Firebase.auth.currentUser == null){
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearChatId()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    private fun toConversation(chatId: String){
        if(chatId.isEmpty()) return
        val conversationIntent = Intent(this,ConversationActivity::class.java)
            .apply {
                putExtra(BroadCastUtility.CHAT_ID,chatId)
            }
        startActivity(conversationIntent)
    }


}