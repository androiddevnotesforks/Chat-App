package com.devwarex.chatapp.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.devwarex.chatapp.R as resource
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import com.devwarex.chatapp.utility.PhoneUtil
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
            viewModel.requestContactPermission.collectLatest {
                if (it){
                    requestContactPermission()
                    viewModel.loading()
                    viewModel.removeRequestObserving()
                }
            }
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
                    Log.e("phones","${ui.contacts.size}")
                   LazyColumn(modifier = modifier
                        .constrainAs(lazyColumn) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .fillMaxSize()){
                        items(ui.contacts){
                            Text(text = it.name + "  "+PhoneUtil.filterPhoneNumber(it.phone))
                        }
                    }
                }
            }
        }
    }
}
