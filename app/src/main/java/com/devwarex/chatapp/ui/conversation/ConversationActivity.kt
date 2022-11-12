package com.devwarex.chatapp.ui.conversation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import com.devwarex.chatapp.R
import com.devwarex.chatapp.db.Message
import com.devwarex.chatapp.models.LocationPin
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.theme.Blue200
import com.devwarex.chatapp.ui.theme.LightBlack
import com.devwarex.chatapp.ui.theme.LightBlue
import com.devwarex.chatapp.util.BroadCastUtility
import com.devwarex.chatapp.util.BroadCastUtility.Companion.CHAT_ID
import com.devwarex.chatapp.util.DateUtility
import com.devwarex.chatapp.util.MessageState
import com.devwarex.chatapp.util.MessageType
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.maps.android.compose.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileNotFoundException


@AndroidEntryPoint
class ConversationActivity : ComponentActivity() {

    private var chatId = ""
    private val viewModel by viewModels<MessagesViewModel>()
    private lateinit var galleryIntent: Intent
    private lateinit var pickPictureIntentLauncher: ActivityResultLauncher<Intent?>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                viewModel.isLocationPermissionGranted(true)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.

            } else -> {
            // No location access granted.
            viewModel.locationPermissionDenied()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) { MainLayoutScreen() }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel.isLocationPermissionGranted(
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
        galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*"  }
        pickPictureLauncher()
        lifecycleScope.launchWhenCreated {
            launch { viewModel.shouldFetchChat.collect { if (it) returnToChat() } }
            launch { viewModel.locationUiState.collect{
                Log.e("location_state",Gson().toJson(it))
                if (it.requestLastKnownLocation){
                    if (!it.isLocationEnabled){
                        requestEnableLocation()
                    }
                    if (it.isLocationEnabled && !it.isLocationPermissionGranted){
                        requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
                        viewModel.pickLocation()
                    }
                    if (it.isLocationEnabled && it.isLocationPermissionGranted){
                        updateLocation()
                        viewModel.pickLocation()
                        fusedLocationClient.requestLocationUpdates(createLocationRequest(),locationCallback,
                            Looper.getMainLooper())

                    }
                }
            } }
        }

        viewModel.insert.observe(this,this::insertPhoto)

        val callback = object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
               returnToChat()
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    private fun insertPhoto(b: Boolean){
        if (b){ pickPhoto() }
    }

    private fun createLocationRequest(): LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    private fun requestEnableLocation(){
        val locationBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(createLocationRequest())
        val settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationBuilder.build())
        task.addOnSuccessListener {
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException){
                try {
                    it.startResolutionForResult(this,500)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("enable_location",sendEx.message.toString())
                }
            }
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
    override fun onResume() {
        super.onResume()
        chatId = intent.getStringExtra(CHAT_ID) ?: ""
        viewModel.sync(chatId)
        Intent().also { intent ->
            intent.action = BroadCastUtility.CONVERSATION_ACTION_ID
            intent.putExtra(CHAT_ID, chatId)
            sendBroadcast(intent)
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        viewModel.isLocationEnabled(manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }

    private fun stopUpdateLocation(){
        if (this::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateLocation(){
        var counter = 0
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations){
                    Log.e("counter","$counter")
                    viewModel.updateLocationPin(
                        location.latitude,
                        location.longitude
                    )
                    if(counter == 2){
                        stopUpdateLocation()
                        break
                    }
                    counter++
                }
            }
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
        stopUpdateLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListener()
    }

    private fun returnToChat(){
        if (isTaskRoot){
            val intent = Intent(this,ChatsActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            finish()
        }
        lifecycleScope.cancel()
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
fun MainLayoutScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
){
    val uiState = viewModel.uiState.collectAsState().value
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
                            else if (uiState.availability) stringResource(id = R.string.online_name) else stringResource(id = R.string.offline_name),
                            style = MaterialTheme.typography.caption,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        modifier.padding(padding)
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
                if (uiState.uid.isNotEmpty()) {
                    items(uiState.messages) {
                        MainMessageCard(msg = it, uid = uiState.uid, viewModel = viewModel)
                    }
                }
            }

            val text = viewModel.text.collectAsState()
            Row(modifier = modifier
                .constrainAs(edit) {
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                }
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                MessageEditText(modifier.weight(1f), viewModel = viewModel, text = text.value)
                if (text.value.isNotBlank()) {
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
                }else{
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
        if (uiState.previewBeforeSending && uiState.bitmap != null){
            PreviewImageForSending(
                bitmap = uiState.bitmap,
                viewModel = viewModel,
                modifier = modifier
            )
        }
        if (uiState.isPreviewImage && uiState.previewImage.isNotEmpty()){
            ImageMessageView(
                img = uiState.previewImage,
                viewModel = viewModel,
                modifier = modifier
            )
        }
    }

    if (uiState.requestLocation && uiState.locationPermissionGranted){
        if(uiState.locationPin.lat != 0.0 && uiState.locationPin.lng != 0.0) {
            MapDialog(
                locationPin = uiState.locationPin,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun MessageEditText(modifier: Modifier,text: String,viewModel: MessagesViewModel){
    TextField(
        value = text,
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
        ),
        trailingIcon = {
            if (text.isBlank()) {
                IconButton(
                    onClick = { viewModel.pickLocation() }
                ) {
                    val uiState = viewModel.uiState.collectAsState().value
                    if (uiState.requestLocation){
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }else {
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
fun MainMessageCard(msg: Message,uid: String,viewModel: MessagesViewModel){
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp)) {
        if (uid == msg.senderId) {
            SenderMessageCard(msg = msg, modifier = Modifier.align(Alignment.End), viewModel = viewModel)
        }else {
            ReceiveMessageCard(msg = msg, modifier = Modifier.align(Alignment.Start), viewModel = viewModel)
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
            when(msg.type){
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
                MessageType.PIN_LOCATION ->{
                    MapView(pin = LocationPin(lat = msg.pin_lat,msg.pin_lng), userName = viewModel.uiState.value.receiverUser?.name ?: "")
                }
                else -> { }
            }
            Row(modifier = Modifier
                .align(Alignment.Start)
                .padding(all = 2.dp),) {
                Text(
                    text = DateUtility.getDate(msg.timestamp),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                if (msg.state == MessageState.SENT)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sent),
                        tint =  Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Sent"
                    )
                else
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delivered),
                        tint =  Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Delivered"
                    )
            }
        }
    }
}


@Composable
fun SenderMessageCard(
    msg: Message,
    modifier: Modifier,
    viewModel: MessagesViewModel
){
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
            when(msg.type){
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
                    MapView(pin = LocationPin(lat = msg.pin_lat,msg.pin_lng), userName = "You")
                }
                else -> { }
            }

            Row(modifier = Modifier
                .align(Alignment.End)
                .padding(all = 2.dp),) {
                Text(
                    text = DateUtility.getDate(msg.timestamp),
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                if (msg.state == MessageState.SENT) 
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sent),
                        tint =  Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Sent"
                    )
                else
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delivered),
                        tint =  Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(all = 2.dp),
                        contentDescription = "Delivered"
                    )
            }
        }
    }
}

@Composable
fun MapView(
    pin: LocationPin,
    userName: String
){
    val locationPin = LatLng(pin.lat, pin.lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(locationPin, 17f)
    }
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp),
        cameraPositionState = cameraPositionState
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
){
    AlertDialog(
        modifier = modifier
            .wrapContentSize(),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0f),
        onDismissRequest = { viewModel.closePreviewImage() },
        properties = DialogProperties(dismissOnBackPress = true,dismissOnClickOutside = true),
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
){
    AlertDialog(
        modifier = modifier.wrapContentSize(),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0f),
        onDismissRequest = { viewModel.closePreviewImageForSending() },
        properties = DialogProperties(dismissOnBackPress = true,dismissOnClickOutside = true),
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
                if (progress.value == 0){
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
                }else{
                    val animateProgress = animateFloatAsState(
                        targetValue = progress.value.toFloat(),
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )
                    CircularProgressIndicator(
                        modifier = modifier
                            .padding(all = 16.dp)
                            .align(Alignment.End),
                        color = MaterialTheme.colors.secondary,
                        progress = animateProgress.value/100)
                }
                if (progress.value == 100){
                    viewModel.closePreviewImageForSending()
                }
            }

        }
    )
}



@Composable
fun MapDialog(
    locationPin: LocationPin,
    viewModel: MessagesViewModel
){
    Dialog(
        onDismissRequest = { viewModel.dismissMapDialog() },
        properties = DialogProperties(dismissOnBackPress = true,dismissOnClickOutside = false)
    ) {
        val currentLocation = LatLng(locationPin.lat, locationPin.lng)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentLocation, 17f)
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

