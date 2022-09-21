package com.devwarex.chatapp.ui.contacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.devwarex.chatapp.db.Contact
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.R as resource
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.util.BroadCastUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

@AndroidEntryPoint
class ContactsActivity : ComponentActivity(),LoaderManager.LoaderCallbacks<Cursor> {


    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val viewModel: ContactsViewModel by viewModels()

    private val PHONE_PROJECTION: Array<out String> = arrayOf(
        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ContactsScreen()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            launch {
                viewModel.requestContactPermission.collectLatest {
                    if (it){
                        requestContactPermission()
                        viewModel.loading()
                        viewModel.removeRequestObserving()
                    }
                }
            }
            launch {
                viewModel.shouldInviteContact.collectLatest { inviteUser(it) }
            }

            launch { viewModel.chatId.collectLatest { toConversion(it) } }
        }
        when{
            isContactPermissionGranted() -> {
                lifecycleScope.launchWhenStarted {
                    viewModel.shouldRetrieveContacts.collectLatest {
                        if (it){
                            LoaderManager.getInstance(this@ContactsActivity).initLoader(0,null,this@ContactsActivity)
                        }
                    }
                }
            }
           else -> {
               registerPermissionLauncher()
               viewModel.showPermissionMessage()
           }
       }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancelJobs()
    }

    private fun toConversion(chatId: String?){
        if (chatId == null) return
        val conversationIntent = Intent(this, ConversationActivity::class.java)
            .apply {
                putExtra(BroadCastUtility.CHAT_ID,chatId)
            }
        startActivity(conversationIntent)
        finish()
    }
    private fun inviteUser(phone: String){
        if (phone.isBlank()) return
        val i = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("sms:$phone")
            putExtra("sms_body", getString(resource.string.share_app_body))
        }
        try {
            startActivity(i)
        }catch (e: ActivityNotFoundException){

        }
    }
    private fun isContactPermissionGranted() = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun registerPermissionLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                LoaderManager.getInstance(this).initLoader(0,null,this)
                viewModel.loading()
            }else{
                viewModel.showPermissionMessage()
            }
        }
    }

    private fun requestContactPermission(){
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when(id){
            0 -> {
                CursorLoader(
                    this,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    PHONE_PROJECTION,
                    null,
                    null,
                    null
                )
            }
            else -> CursorLoader(this,ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        }

    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data == null) return
        when(loader.id){
            0 -> {
                if (data.isClosed) return
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val list: Queue<Contact> = LinkedList()
                        if (data.isClosed) return@launch
                        while (data.moveToNext()) {
                            if (data.getInt(3) > 0 && data.getString(2).isNotBlank()){
                                data.apply {
                                    list.offer(
                                        Contact(
                                            isFound = false,
                                            name = getString(1),
                                            phone = getString(2)
                                        )
                                    )
                                }
                            }
                        }
                        viewModel.updateContacts(list)
                    }catch (e: IllegalAccessException){
                        Log.e("error",e.message!!)
                    }finally {
                        data.close()
                    }
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }


}

@Composable
fun ContactsScreen(modifier: Modifier = Modifier){
    val viewModel = hiltViewModel<ContactsViewModel>()
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Select contact")}
            )
        }
    ) {
        ConstraintLayout(
            modifier = modifier.fillMaxSize()
        ) {
            val (loading,lazyColumn,messageColumn) = createRefs()
            when(val ui = uiState.value){
                is ContactsUiState.Loading -> {
                    if (ui.isLoading) {
                        LinearProgressIndicator(modifier = modifier.constrainAs(loading) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        })
                    }
                }

                is ContactsUiState.PermissionState -> {
                    if (ui.showPermissionMessage){
                        Column(modifier = modifier
                            .constrainAs(messageColumn) {
                                top.linkTo(parent.top)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                            }
                            .fillMaxWidth()) {
                            Text(
                                text = stringResource(id = resource.string.request_contact_permission_message),
                                color = MaterialTheme.colors.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = modifier.padding(top = 16.dp))
                            Button(
                                onClick = { viewModel.requestContactPermission() },
                                modifier = modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = stringResource(id = resource.string.allow_access_title))
                            }
                        }
                    }
                }

                is ContactsUiState.Success -> {
                   LazyColumn(modifier = modifier
                       .constrainAs(lazyColumn) {
                           top.linkTo(parent.top)
                           end.linkTo(parent.end)
                           bottom.linkTo(parent.bottom)
                           start.linkTo(parent.start)
                       }
                       .fillMaxSize()){
                        items(ui.contacts){contact -> ContactCard(contact = contact, modifier = modifier,viewModel = viewModel)}
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactCard(contact: Contact,modifier: Modifier,viewModel: ContactsViewModel){
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        modifier = modifier
            .padding(all = 2.dp)
            .fillMaxWidth(),
        enabled = true,
        onClick = { if (contact.isFound) viewModel.createChat(contact.phone) else viewModel.inviteContact(contact.phone)}
    ) {
        Row(modifier = modifier.padding(all = 16.dp)) {
            Image(
                painter = painterResource(id = resource.drawable.user),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = modifier.padding(start = 16.dp, end = 16.dp)) {
                Text(
                    text = contact.name,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = modifier.padding(top = 8.dp))
                Text(
                    text = stringResource(id = if (contact.isFound) resource.string.start_chat_title else resource.string.invite_title),
                    style = if (contact.isFound) MaterialTheme.typography.caption else MaterialTheme.typography.body2,
                    color = if (contact.isFound) Color.Gray else MaterialTheme.colors.primary
                )
            }

        }
    }
}
