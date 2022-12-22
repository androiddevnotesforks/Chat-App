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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
            e.printStackTrace()
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

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}
