package com.devwarex.chatapp.ui.chat

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.ChatRelations
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.ui.signUp.ErrorsState
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.utility.BroadCastUtility
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
        lifecycleScope.launchWhenCreated {
            launch { viewModel.emailMessage.collect { updateUiOnEmailError(it) } }
            launch {
                viewModel.isAdded.collect { if (it){
                    Toast.makeText(this@ChatsActivity, "User added",Toast.LENGTH_LONG).show()
                    viewModel.hideDialog()
                } else {
                    Toast.makeText(this@ChatsActivity, "User already exist",Toast.LENGTH_LONG).show()
                    viewModel.hideDialog()
                } }
            }
        }

    }

    private fun updateUiOnEmailError(state: ErrorsState){
        when(state){
            ErrorsState.INVALID_EMAIL -> {
                Toast.makeText(this,getString(R.string.invalid_email_message),Toast.LENGTH_LONG).show()
            }
            ErrorsState.EMAIL_NOT_FOUND -> {
                Toast.makeText(this,getString(R.string.email_not_found_message),Toast.LENGTH_LONG).show()
            }
            ErrorsState.NONE -> { }
            else -> viewModel.hideDialog()
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

@Composable
fun ChatsScreen(modifier: Modifier = Modifier){
    val viewModel: ChatsViewModel = hiltViewModel()
    val (chats,isLoading,showDialog) = viewModel.uiState.collectAsState().value
    Log.e("chats_$showDialog",Gson().toJson(chats))
    Scaffold(
        topBar = {
            TopAppBar(
                title ={ Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        floatingActionButton = { 
            FloatingActionButton(onClick = { if (!showDialog) viewModel.showDialog() }) {
                Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Add chat" )
        }},
        floatingActionButtonPosition = FabPosition.End
    ) {

        if (showDialog){
            AddUserDialog()
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
    val email = viewModel.email.collectAsState().value
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
                Row() {
                    OutlinedButton(
                        onClick = { viewModel.hideDialog() },
                        shape = MaterialTheme.shapes.small.copy(all = CornerSize(4.dp)),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "close", tint = MaterialTheme.colors.onSurface )
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
                    value = email,
                    onValueChange = { viewModel.setEmail(it) },
                    modifier = Modifier.padding(all = 24.dp).align(Alignment.CenterHorizontally),
                    label = { Text(text = stringResource(id = R.string.email_title)) },
                    placeholder = { Text(text = stringResource(id = R.string.enter_email_title))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Button(onClick = { viewModel.addUser()},
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