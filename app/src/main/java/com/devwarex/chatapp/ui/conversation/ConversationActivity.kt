package com.devwarex.chatapp.ui.conversation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.devwarex.chatapp.ChatAppBroadCastReceiver
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.ui.theme.LightBlack
import com.devwarex.chatapp.ui.theme.LightBlue
import com.devwarex.chatapp.utility.BroadCastUtility
import com.devwarex.chatapp.utility.BroadCastUtility.Companion.CHAT_ID
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConversationActivity : ComponentActivity() {

    private var chatId = ""
    private val viewModel by viewModels<MessagesViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) { MainLayoutScreen() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        chatId = intent.getStringExtra(CHAT_ID) ?: ""
        viewModel.sync(chatId)
        Intent().also { intent ->
            intent.action = BroadCastUtility.CONVERSATION_ACTION_ID
            intent.putExtra(CHAT_ID, chatId)
            sendBroadcast(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        Intent().also { intent ->
            intent.action = BroadCastUtility.CONVERSATION_ACTION_ID
            intent.putExtra(CHAT_ID, BroadCastUtility.CONVERSATION_ON_STOP_KEY)
            sendBroadcast(intent)
        }

        viewModel.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }
}


@Preview
@Composable
fun TextWithNormalPaddingPreview(modifier: Modifier = Modifier) {
    ChatAppTheme {
        MainLayoutScreen()
    }
}


@Composable
fun MainLayoutScreen(modifier: Modifier = Modifier){
    val viewModel =  hiltViewModel<MessagesViewModel>()
    val (messages,enable,uid,isLoading,chat,user,availability,typing) = viewModel.uiState.collectAsState().value
    Scaffold(
        topBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .requiredHeight(56.dp)
                    .background(MaterialTheme.colors.primarySurface),
            ) {
                Row(modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)) {
                    Image(
                        painter = if (user?.img.isNullOrEmpty()) painterResource(id = R.drawable.user) else rememberImagePainter(data = user?.img),
                        contentDescription = "User Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(
                            text = user?.name ?: "Name",
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.onPrimary
                        )
                        Text(
                            text = if (availability && typing) stringResource(id = R.string.typing_name)
                            else if (availability) stringResource(id = R.string.online_name) else stringResource(id = R.string.offline_name),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    ) {
        ConstraintLayout{
            val ( list , edit ) = createRefs()
            LazyColumn(
                modifier = modifier
                    .fillMaxHeight()
                    .constrainAs(list) {
                        top.linkTo(parent.top)
                        bottom.linkTo(edit.top)
                        height = Dimension.preferredWrapContent
                    }
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                if (uid.isNotEmpty()) {
                    items(messages) {
                        MainMessageCard(msg = it, uid = uid)
                    }
                }
            }

            Row(modifier = modifier
                .constrainAs(edit) {
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                }
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                MessageEditText(modifier.weight(1f), viewModel = viewModel)
                FloatingActionButton(
                    onClick = { if (enable) viewModel.send() },
                    modifier = modifier
                        .padding(end = 8.dp)
                        .align(alignment = Alignment.CenterVertically),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_send) , contentDescription = "send", modifier = modifier)
                }
            }

        }

    }
}

@Composable
fun MessageEditText(modifier: Modifier,viewModel: MessagesViewModel){
    val t = viewModel.text.collectAsState()
    TextField(
        value = t.value,
        onValueChange = viewModel::setText ,
        modifier = modifier
            .padding(end = 8.dp),
        maxLines = 3,
        placeholder = { Text(text = stringResource(id = R.string.message_title))},
        colors = TextFieldDefaults.textFieldColors(
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            textColor = MaterialTheme.colors.onSurface
        )
    )
}

@Composable
fun MainMessageCard(msg: Message,uid: String){
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp)) {
        if (uid == msg.senderId) {
            SenderMessageCard(msg = msg, modifier = Modifier.align(Alignment.End))
        }else {
            ReceiveMessageCard(msg = msg, modifier = Modifier.align(Alignment.Start))
        }
    }
}
@Composable
fun ReceiveMessageCard(msg: Message,modifier: Modifier) {
    Card(
        shape = MaterialTheme.shapes.medium.copy(
            bottomStart = CornerSize(6.dp),
            bottomEnd = CornerSize(6.dp),
            topEnd = CornerSize(6.dp),
            topStart = CornerSize(0.dp)
        ),
        elevation = 2.dp,
        modifier = modifier.padding(end = 32.dp, start = 4.dp, bottom = 4.dp)
    ) {
        Column {
            Text(
                text = msg.body,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(all = 4.dp)
            )
            Text(
                text = "27 Mar 2:50",
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(all = 2.dp),
                color = Color.LightGray
            )
        }
    }
}


@Composable
fun SenderMessageCard(msg: Message,modifier: Modifier){
    Card(
        shape = MaterialTheme.shapes.medium.copy(
            bottomStart = CornerSize(6.dp),
            bottomEnd = CornerSize(6.dp),
            topEnd = CornerSize(0.dp),
            topStart = CornerSize(6.dp)
        ),
        elevation = 2.dp,
        modifier = modifier.padding(end = 2.dp, start = 32.dp, bottom = 4.dp),
        backgroundColor = LightBlue
    ) {
        Column {
            Text(
                text = msg.body,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(all = 4.dp),
                color = LightBlack
            )
            Text(
                text = "27 Mar 2:50",
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(all = 2.dp),
                color = Color.Gray
            )
        }
    }
}

