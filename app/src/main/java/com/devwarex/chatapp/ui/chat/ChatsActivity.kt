package com.devwarex.chatapp.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.ChatRelations
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.utility.BroadCastUtility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsActivity : ComponentActivity() {

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
        viewModel.chatId.observe(this,this::toConversation)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    private fun toConversation(chatId: String){
        val conversationIntent = Intent(this,ConversationActivity::class.java)
            .apply {
                putExtra(BroadCastUtility.CHAT_ID,chatId)
            }
        startActivity(conversationIntent)
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
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = { }) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add chat" )
        }},
        floatingActionButtonPosition = FabPosition.End
    ) {

        LazyColumn(modifier = modifier){
            items(chats){
                ChatCard(chat = it)
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatCard(chat: ChatRelations){
    val viewModel: ChatsViewModel = hiltViewModel()
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier
            .padding(all = 2.dp)
            .fillMaxWidth(),
        onClick = { viewModel.onChatClick(chat.chat.id) }
    ) {
        Row(Modifier.padding(all = 16.dp)) {
            Image(
                painter = if (chat.user?.img.isNullOrEmpty()) painterResource(id = R.drawable.user) else rememberImagePainter(data = chat.user?.img),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column{
                Text(
                    text = chat.user?.name ?: "Name",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                    maxLines = 1
                )
                Text(
                    text = chat.chat.lastMessage,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
    }
}