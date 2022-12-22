package com.devwarex.chatapp.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.R


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
                title = { Text(text = stringResource(id = R.string.edit_profile_title)) },
                actions = { Text(
                    text = stringResource(id = R.string.save_title),
                    modifier = modifier.clickable(
                        enabled = true, onClick = { viewModel.updateUser() }
                    )
                )
                }
            )
        },
        modifier = modifier.fillMaxSize()
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
                        painter = if (uiState.user!!.img.isEmpty()) painterResource(id = R.drawable.user) else rememberAsyncImagePainter(
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
                    label = { Text(text = stringResource(id = R.string.name_title)) },
                    placeholder = { Text(text = stringResource(id = R.string.name_title)) },
                    modifier = modifier.fillMaxWidth()
                )

                Spacer(modifier = modifier.padding(top = 32.dp))

                Row {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
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
                        painter = painterResource(id = R.drawable.ic_phone),
                        contentDescription = "phone label",
                        tint = MaterialTheme.colors.onSurface
                    )
                    Text(
                        text = uiState.user?.phone ?: "",
                        modifier = modifier.padding(start = 8.dp),
                        color = MaterialTheme.colors.onSurface
                    )
                }

                Spacer(modifier = modifier.padding(top = 16.dp))

                Column(
                    modifier = modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = { viewModel.signOut() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary
                        )
                    ) {
                        Text(text = stringResource(id = R.string.sign_out_title))
                    }
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