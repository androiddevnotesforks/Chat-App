package com.devwarex.chatapp.ui.contacts

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.Contact


@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel = hiltViewModel()
){
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Select contact") }
            )
        }
    ) { padding ->
        modifier.padding(padding)
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
                                text = stringResource(id = R.string.request_contact_permission_message),
                                color = MaterialTheme.colors.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = modifier.padding(top = 16.dp))
                            Button(
                                onClick = { viewModel.requestContactPermission() },
                                modifier = modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = stringResource(id = R.string.allow_access_title))
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
fun ContactCard(contact: Contact, modifier: Modifier, viewModel: ContactsViewModel){
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
                painter = painterResource(id = R.drawable.user),
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
                    text = stringResource(id = if (contact.isFound) R.string.start_chat_title else R.string.invite_title),
                    style = if (contact.isFound) MaterialTheme.typography.caption else MaterialTheme.typography.body2,
                    color = if (contact.isFound) Color.Gray else MaterialTheme.colors.primary
                )
            }

        }
    }
}
