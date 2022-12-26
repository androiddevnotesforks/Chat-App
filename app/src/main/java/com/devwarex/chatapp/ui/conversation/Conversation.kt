package com.devwarex.chatapp.ui.conversation

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.LocationPin
import com.devwarex.chatapp.ui.theme.Blue200
import com.devwarex.chatapp.ui.theme.LightBlack
import com.devwarex.chatapp.ui.theme.LightBlue
import com.devwarex.chatapp.util.DateUtility
import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.MessageType
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*


@Composable
fun MainLayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .requiredHeight(56.dp)
                    .background(MaterialTheme.colors.primarySurface),
            ) {
                val uiState = viewModel.uiState.collectAsState().value
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    Image(
                        painter = if (uiState.receiverUser?.img.isNullOrEmpty())
                            painterResource(id = R.drawable.user) else
                            rememberAsyncImagePainter(model = uiState.receiverUser?.img),
                        contentDescription = "User Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(
                            text = uiState.receiverUser?.name ?: "Name",
                            style = MaterialTheme.typography.h5,
                            color = Color.White
                        )
                        Text(
                            text = if (uiState.availability && uiState.typing) stringResource(id = R.string.typing_name)
                            else if (uiState.availability) stringResource(id = R.string.online_name) else stringResource(
                                id = R.string.offline_name
                            ),
                            style = MaterialTheme.typography.caption,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        modifier.padding(padding)
        val uiState = viewModel.uiState.collectAsState().value
        ConstraintLayout {
            val (list, edit) = createRefs()
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

                if (uiState.uid.isNotEmpty()) {
                    items(uiState.messages) {
                        MainMessageCard(
                            msg = it,
                            uid = uiState.uid,
                            viewModel = viewModel
                        )
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
                MessageEditText(modifier.weight(1f), viewModel = viewModel, text = uiState.text)
                if (uiState.text.isNotBlank()) {
                    FloatingActionButton(
                        onClick = { if (uiState.enable) viewModel.send() },
                        modifier = modifier
                            .padding(end = 8.dp)
                            .align(alignment = Alignment.CenterVertically),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "send",
                            modifier = modifier
                        )
                    }
                } else {
                    IconButton(
                        onClick = { viewModel.insertPhoto() }, modifier = modifier
                            .padding(end = 8.dp)
                            .align(alignment = Alignment.CenterVertically)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = "insert photo",
                            tint = Color.Gray
                        )
                    }
                }
            }

        }
    }
    val uiState = viewModel.uiState.collectAsState().value
    if (uiState.previewBeforeSending && uiState.bitmap != null) {
        PreviewImageForSending(
            bitmap = uiState.bitmap,
            viewModel = viewModel,
            modifier = modifier
        )
    }

    if (uiState.isPreviewImage && uiState.previewImage.isNotEmpty()) {
        ImageMessageView(
            img = uiState.previewImage,
            viewModel = viewModel,
            modifier = modifier
        )
    }

    if (uiState.deleteMessageId.isNotEmpty()) {
        DeleteMessageDialog(
            viewModel = viewModel,
            modifier = modifier
        )
    }

    if (uiState.requestLocation && uiState.locationPermissionGranted) {
        if (uiState.locationPin.lat != 0.0 && uiState.locationPin.lng != 0.0) {
            MapDialog(
                locationPin = uiState.locationPin,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun MessageEditText(
    modifier: Modifier,
    text: String,
    viewModel: MessagesViewModel
) {
    TextField(
        value = text,
        onValueChange = viewModel::setText,
        modifier = modifier
            .padding(end = 8.dp),
        maxLines = 3,
        placeholder = { Text(text = stringResource(id = R.string.message_title)) },
        colors = TextFieldDefaults.textFieldColors(
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            textColor = MaterialTheme.colors.onSurface
        ),
        trailingIcon = {
            if (text.isBlank()) {
                IconButton(
                    onClick = { viewModel.pickLocation() }
                ) {
                    val uiState = viewModel.uiState.collectAsState().value
                    if (uiState.requestLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "Location label",
                            tint = Blue200
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MainMessageCard(
    msg: Message,
    uid: String,
    viewModel: MessagesViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        if (uid == msg.senderId) {
            SenderMessageCard(
                msg = msg,
                modifier = Modifier.align(Alignment.End),
                viewModel = viewModel
            )
        } else {
            ReceiveMessageCard(
                msg = msg,
                modifier = Modifier.align(Alignment.Start),
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ReceiveMessageCard(
    msg: Message,
    modifier: Modifier,
    viewModel: MessagesViewModel
) {
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
            when (msg.type) {
                MessageType.TEXT -> {
                    Text(
                        text = msg.body,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(all = 4.dp),
                    )
                }

                MessageType.IMAGE -> {
                    Image(
                        painter = rememberAsyncImagePainter(model = msg.body),
                        contentDescription = "photo message",
                        modifier = Modifier
                            .wrapContentSize()
                            .height(200.dp)
                            .padding(top = 6.dp, start = 6.dp, end = 6.dp)
                            .clickable { viewModel.onPreviewImage(msg.body) },
                        contentScale = ContentScale.Fit
                    )
                }
                MessageType.PIN_LOCATION -> {
                    MapView(
                        pin = LocationPin(lat = msg.pin_lat, msg.pin_lng),
                        userName = viewModel.uiState.value.receiverUser?.name ?: "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 6.dp, start = 6.dp, end = 6.dp)
                    )
                }
                else -> {}
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(all = 2.dp),
            ) {
                Text(
                    text = DateUtility.getDate(msg.timestamp),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                if (msg.state == MessageState.SENT)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sent),
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Sent"
                    )
                else
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delivered),
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Delivered"
                    )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SenderMessageCard(
    msg: Message,
    modifier: Modifier,
    viewModel: MessagesViewModel
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = true,
                onLongClick = { viewModel.onDeleteMessage(msg.id) },
                onClick = { },
                onClickLabel = "Delete"
            )
    ) {
        Card(
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = CornerSize(6.dp),
                bottomEnd = CornerSize(6.dp),
                topEnd = CornerSize(0.dp),
                topStart = CornerSize(6.dp)
            ),
            elevation = 2.dp,
            modifier = modifier
                .padding(end = 2.dp, start = 32.dp)
                .align(Alignment.Start),
            backgroundColor = LightBlue
        ) {
            Column {
                when (msg.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = msg.body,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(all = 4.dp),
                            color = LightBlack
                        )
                    }

                    MessageType.IMAGE -> {
                        Image(
                            painter = rememberAsyncImagePainter(model = msg.body),
                            contentDescription = "photo message",
                            modifier = Modifier
                                .wrapContentSize()
                                .height(200.dp)
                                .padding(top = 6.dp, start = 6.dp, end = 6.dp)
                                .clickable { viewModel.onPreviewImage(msg.body) },
                            contentScale = ContentScale.Crop
                        )
                    }

                    MessageType.PIN_LOCATION -> {
                        MapView(
                            pin = LocationPin(lat = msg.pin_lat, msg.pin_lng),
                            userName = "You",
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 6.dp, start = 6.dp, end = 6.dp)
                        )
                    }
                    else -> {}
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(all = 2.dp),
                ) {
                    Text(
                        text = DateUtility.getDate(msg.timestamp),
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                    if (msg.state == MessageState.SENT)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sent),
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(all = 2.dp),
                            contentDescription = "Sent"
                        )
                    else
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delivered),
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(all = 2.dp),
                            contentDescription = "Delivered"
                        )
                }
            }
        }
    }
}

@Composable
fun MapView(
    pin: LocationPin,
    userName: String,
    modifier: Modifier
) {
    val locationPin = LatLng(pin.lat, pin.lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationPin, 10f)
    }
    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp, min = 180.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings().copy(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            scrollGesturesEnabled = false
        )
    ) {
        Marker(
            state = MarkerState(position = locationPin),
            title = userName
        )
    }
}

@Composable
fun ImageMessageView(
    img: String,
    viewModel: MessagesViewModel,
    modifier: Modifier
) {
    AlertDialog(
        modifier = modifier
            .wrapContentSize(),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0f),
        onDismissRequest = { viewModel.closePreviewImage() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        text = {
            Column(modifier = modifier.fillMaxWidth()) {
                Image(
                    painter = rememberAsyncImagePainter(model = img),
                    contentDescription = "photo message",
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Inside
                )
            }
        },
        buttons = {}
    )
}

@Composable
fun PreviewImageForSending(
    bitmap: Bitmap,
    modifier: Modifier,
    viewModel: MessagesViewModel
) {
    AlertDialog(
        modifier = modifier.wrapContentSize(),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0f),
        onDismissRequest = { viewModel.closePreviewImageForSending() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        text = {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Preview image",
                modifier = modifier
                    .wrapContentSize(),
                contentScale = ContentScale.Inside
            )
        },
        buttons = {
            Column(modifier = modifier.fillMaxWidth()) {
                val progress = viewModel.uploadProgress.collectAsState()
                if (progress.value == 0) {
                    FloatingActionButton(
                        onClick = { viewModel.sendImage() },
                        modifier = modifier
                            .padding(all = 16.dp)
                            .align(Alignment.End)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "send image"
                        )
                    }
                } else {
                    val animateProgress = animateFloatAsState(
                        targetValue = progress.value.toFloat(),
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )
                    CircularProgressIndicator(
                        modifier = modifier
                            .padding(all = 16.dp)
                            .align(Alignment.End),
                        color = MaterialTheme.colors.secondary,
                        progress = animateProgress.value / 100
                    )
                }
                if (progress.value == 100) {
                    viewModel.closePreviewImageForSending()
                }
            }

        }
    )
}

@Composable
fun DeleteMessageDialog(
    viewModel: MessagesViewModel,
    modifier: Modifier
) {
    AlertDialog(
        onDismissRequest = { viewModel.onDismissDeleteMessage() },
        text = {
            Column {
                Text(
                    text = "Delete message?",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = modifier.padding(top = 16.dp))
                Text(
                    text = "This message will be delete for everyone.",
                    style = MaterialTheme.typography.body1
                )
            }
        },
        buttons = {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = modifier.fillMaxWidth().padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            ) {
                TextButton(onClick = { viewModel.onDismissDeleteMessage() }) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = modifier.padding(16.dp))
                Button(onClick = { viewModel.deleteMessage() }) {
                    Text(text = "Delete")
                }
            }
        }
    )
}

@Composable
fun MapDialog(
    locationPin: LocationPin,
    viewModel: MessagesViewModel
) {
    Dialog(
        onDismissRequest = { viewModel.dismissMapDialog() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        val currentLocation = LatLng(locationPin.lat, locationPin.lng)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentLocation, 16f)
        }
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colors.surface)
                .padding(bottom = 8.dp)
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp, min = 300.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings().copy(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMyLocationClick = {},
                properties = MapProperties().copy(
                    isMyLocationEnabled = true
                )
            ) {
            }
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Button(
                onClick = { viewModel.shareCurrentLocation() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Share Current Location")
            }
        }
    }
}

