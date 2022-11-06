package com.devwarex.chatapp.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import com.devwarex.chatapp.R as resource

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private lateinit var galleryIntent: Intent
    private lateinit var pickPictureIntentLauncher: ActivityResultLauncher<Intent?>
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme{
              Surface(
                  modifier = Modifier.fillMaxSize(),
                  color = MaterialTheme.colors.background
              ){
                  ProfileScreen()
              }
            }
        }

        galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*"  }
        pickPictureLauncher()

        lifecycleScope.launchWhenCreated {
            launch { viewModel.insert.collect{
                if (it){
                   pickPhoto()
                }
            } }
        }
    }

    private fun pickPhoto(){
        pickPictureIntentLauncher.launch(galleryIntent)
    }

    private fun pickPictureLauncher() {
        pickPictureIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            viewModel.removeInsertPhoto()
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val uri = result.data!!.data
                    try {
                        var imageStream = uri?.let {
                            contentResolver.openInputStream(
                                it
                            )
                        }
                        var bitmap = BitmapFactory.decodeStream(imageStream)
                        viewModel.setBitmap(bitmap)
                        bitmap = null
                        imageStream = null
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
){
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = resource.string.edit_profile_title))},
                actions = { Text(
                    text = stringResource(id = resource.string.save_title),
                    modifier = modifier.clickable(
                        enabled = true, onClick = { viewModel.updateUser() }
                    )
                )}
            )
        }
    ) { padding ->
        modifier.padding(padding)
        val uiState: ProfileUiState = viewModel.uiState.collectAsState().value
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            )
        }else{
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        all = 16.dp
                    )
                    .verticalScroll(state = rememberScrollState())
            ) {
                IconButton(
                    onClick = { viewModel.insertPhoto() },
                    enabled = true
                ) {
                    Image(
                        painter = if (uiState.user!!.img.isEmpty()) painterResource(id = resource.drawable.user) else rememberAsyncImagePainter(
                            model = uiState.user.img
                        ),
                        contentDescription = "Profile pic",
                        modifier = modifier
                            .size(88.dp)
                            .clip(shape = CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = modifier.padding(top = 16.dp))

                TextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = MaterialTheme.colors.onSurface,
                        backgroundColor = MaterialTheme.colors.background
                    ),
                    label = { Text(text = stringResource(id = com.devwarex.chatapp.R.string.name_title)) },
                    placeholder = { Text(text = stringResource(id = com.devwarex.chatapp.R.string.name_title)) },
                    modifier = modifier.fillMaxWidth()
                )

                Spacer(modifier = modifier.padding(top = 32.dp))
                
                Row {
                    Icon(
                        painter = painterResource(id = resource.drawable.ic_email),
                        contentDescription = "email label",
                        tint = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = uiState.user?.email ?: "",
                        modifier = modifier.padding(start = 8.dp),
                        color = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = modifier.padding(top = 16.dp))

                Row {
                    Icon(
                        painter = painterResource(id = resource.drawable.ic_phone),
                        contentDescription = "phone label",
                        tint = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = uiState.user?.phone ?: "",
                        modifier = modifier.padding(start = 8.dp),
                        color = MaterialTheme.colors.onSurface
                    )
                }


                Spacer(modifier = modifier.padding(top = 48.dp))
                if(uiState.isNameUpdated){
                    LaunchedEffect("Unit"){
                        snackbarHostState.showSnackbar(
                            message = "Name Updated",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }
}