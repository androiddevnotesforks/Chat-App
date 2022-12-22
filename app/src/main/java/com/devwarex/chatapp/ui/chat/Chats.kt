package com.devwarex.chatapp.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.ChatRelations
import com.devwarex.chatapp.util.DateUtility


@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatsViewModel = hiltViewModel()
){
    val (chats,isLoading) = viewModel.uiState.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { viewModel.navigateToProfile() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = "profile",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            IconButton(
                onClick = { viewModel.toContacts() },
                modifier = Modifier.background(
                    color = MaterialTheme.colors.secondary,
                    shape = MaterialTheme.shapes.small.copy(all = CornerSize(12.dp))
                )
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add chat" )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = false
    ){ padding ->
        modifier.padding(padding)
        if (chats.isEmpty() && !isLoading){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "To begin a new Chat. Tap on \"+\" Button in the bottom corner.",
                    style = MaterialTheme.typography.body1,
                    modifier = modifier
                        .wrapContentSize()
                        .padding(all = 16.dp),
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "You can start chatting with contacts who already have ChatApp installed on their phone.",
                    style = MaterialTheme.typography.caption,
                    modifier = modifier
                        .wrapContentSize()
                        .padding(all = 16.dp),
                    color = MaterialTheme.colors.onSurface
                )
            }

        }else{
            LazyColumn(modifier = modifier){
                items(chats){
                    ChatCard(chat = it)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatCard(
    chat: ChatRelations,
    viewModel: ChatsViewModel = hiltViewModel()
){
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
                painter = if (chat.user?.img.isNullOrEmpty()) painterResource(id = R.drawable.user) else rememberAsyncImagePainter(model = chat.user?.img),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            val modifier: Modifier = Modifier

            Column(modifier = modifier){
                Row(modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = chat.user?.name ?: "Name",
                        style = MaterialTheme.typography.body1,
                        modifier = modifier.padding(start = 8.dp, end = 16.dp),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = when(val t = DateUtility.getChatDate(chat.chat.lastEditAt)){
                            "n" -> stringResource(id = R.string.now_name)
                            "y" -> stringResource(id = R.string.yesterday_name)
                            else ->{
                                if (t.isDigitsOnly()){
                                    t+ " "+ stringResource(id = R.string.min_ago)
                                }else{
                                    t
                                }
                            }
                        },
                        modifier = modifier.padding(end = 8.dp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.caption
                    )
                }
                Row(modifier = modifier.padding(top = 2.dp, start = 8.dp)) {
                    if (chat.chat.lastMessage == "IMAGE" || chat.chat.lastMessage == "location_pin") {
                        Icon(
                            painter = painterResource(id = if (chat.chat.lastMessage == "IMAGE" ) R.drawable.ic_photo else R.drawable.ic_location),
                            contentDescription ="photo label",
                            modifier = modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (chat.chat.lastMessage == "IMAGE"){ stringResource(id = R.string.photo_name)
                        }else if(chat.chat.lastMessage == "location_pin"){
                            "Shared Location"
                        }else{ chat.chat.lastMessage },
                        style = MaterialTheme.typography.body2,
                        modifier = modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = if (chat.chat.lastMessage == "IMAGE") 4.dp else 0.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
}