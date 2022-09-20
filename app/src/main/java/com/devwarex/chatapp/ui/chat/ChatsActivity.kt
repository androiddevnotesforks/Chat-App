package com.devwarex.chatapp.ui.chat

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.ChatRelations
import com.devwarex.chatapp.ui.contacts.ContactsActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.utility.BroadCastUtility
import com.devwarex.chatapp.utility.DateUtility
import com.google.android.gms.ads.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatsActivity : ComponentActivity(){

    private val viewModel:  ChatsViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)

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
        //
        viewModel.chatId.observe(this,this::toConversation)
        lifecycleScope.launchWhenCreated {
            viewModel.addUser.collectLatest {
                if (it){
                    viewModel.removeToContactsObserver()
                    toContacts()
                }
            }
        }


    }

    private fun toContacts(){
        val intent = Intent(this,ContactsActivity::class.java)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        viewModel.clearChatId()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    override fun onResume() {
        super.onResume()
        viewModel.sync()
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
            IconButton(
                onClick = { viewModel.toContacts() },
                modifier = Modifier.background(color = MaterialTheme.colors.secondary, shape = MaterialTheme.shapes.small.copy(all = CornerSize(12.dp)))
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add chat" )
            }
         },
        floatingActionButtonPosition = FabPosition.End,
        isFloatingActionButtonDocked = false
    ){

        if (chats.isEmpty()){
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



@Composable
fun AddUserDialog(){
    val viewModel = hiltViewModel<ChatsViewModel>()
    CustomDialogScreen() {
        Card(
            elevation = 12.dp,
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 16.dp, end = 16.dp),
            shape = MaterialTheme.shapes.large.copy(
                all = CornerSize(12.dp)
            )
        ) {
            Column() {
                Row(modifier = Modifier.padding(16.dp)) {
                    IconButton(
                        onClick = {  },
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "close",
                            tint = MaterialTheme.colors.onSurface
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.add_user_title),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier
                        .padding(all = 24.dp)
                        .align(Alignment.CenterHorizontally),
                    label = { Text(text = stringResource(id = R.string.email_title)) },
                    placeholder = { Text(text = stringResource(id = R.string.enter_email_title))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Button(onClick = { },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 48.dp)) {
                    Text(text = stringResource(id = R.string.add_title))
                }
            }
        }
    }
}

@Composable
fun CustomDialogScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    Layout(
        modifier = modifier.fillMaxSize(),
        content = content
    ){ measurables, constraints ->

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        layout(constraints.maxWidth, constraints.maxHeight){
            placeables.forEach { placeable ->
                placeable.placeRelative(x = constraints.maxWidth/constraints.maxHeight, y = constraints.maxHeight/constraints.maxWidth)
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
                        text = when(val t =DateUtility.getChatDate(chat.chat.lastEditAt)){
                            "n" -> stringResource(id = R.string.now_name)
                            "y" -> stringResource(id = R.string.yesterday_name)
                            else ->{
                                if (t.isDigitsOnly()){
                                    t+ " "+stringResource(id = R.string.min_ago)
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
                    if (chat.chat.lastMessage == "IMAGE") {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_photo),
                            contentDescription ="photo label",
                            modifier = modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (chat.chat.lastMessage == "IMAGE") stringResource(id = R.string.photo_name) else chat.chat.lastMessage,
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