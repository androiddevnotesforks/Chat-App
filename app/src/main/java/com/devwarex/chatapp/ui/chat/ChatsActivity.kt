package com.devwarex.chatapp.ui.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.models.ChatRelations
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsActivity : ComponentActivity() {

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
    }
}

@Composable
fun ChatsScreen(modifier: Modifier = Modifier){
    val viewModel: ChatsViewModel = hiltViewModel()
    val (chats,isLoading) = viewModel.uiState.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title ={ Text(text = stringResource(id = R.string.app_name)) }
            )
        }
    ) {

        LazyColumn(modifier = modifier){
            items(chats){
                ChatCard(chat = it)
            }
        }
    }
}


@Composable
fun ChatCard(chat: ChatRelations){
    Card() {
        Text(text = chat.user?.name ?: "Name")
    }
}