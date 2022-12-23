package com.devwarex.chatapp.ui.conversation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.devwarex.chatapp.ui.theme.ChatAppTheme
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.util.BroadCastUtility
import com.devwarex.chatapp.util.BroadCastUtility.Companion.CHAT_ID
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
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

            }
            else -> {
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
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
        galleryIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        pickPictureLauncher()
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { viewModel.shouldFetchChat.collect { if (it) returnToChat() } }
                launch {
                    viewModel.locationUiState.collect {
                        Log.e("location_state", Gson().toJson(it))
                        if (it.requestLastKnownLocation) {
                            if (!it.isLocationEnabled) {
                                requestEnableLocation()
                            }
                            if (it.isLocationEnabled && !it.isLocationPermissionGranted) {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                viewModel.pickLocation()
                            }
                            if (it.isLocationEnabled && it.isLocationPermissionGranted) {
                                updateLocation()
                                viewModel.pickLocation()
                                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED &&
                                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    fusedLocationClient.requestLocationUpdates(
                                        createLocationRequest(), locationCallback,
                                        Looper.getMainLooper()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.insert.observe(this, this::insertPhoto)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                returnToChat()
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    private fun insertPhoto(b: Boolean) {
        if (b) {
            pickPhoto()
        }
    }

    private fun createLocationRequest(): LocationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    private fun requestEnableLocation() {
        val locationBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(createLocationRequest())
        val settingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationBuilder.build())
        task.addOnSuccessListener {
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    it.startResolutionForResult(this, 500)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.e("enable_location", sendEx.message.toString())
                }
            }
        }

    }


    private fun pickPhoto() {
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
                        val imageStream = uri?.let {
                            contentResolver.openInputStream(
                                it
                            )
                        }
                        val bitmap = BitmapFactory.decodeStream(imageStream)
                        viewModel.setBitmap(bitmap)
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
        viewModel.available()
    }

    private fun stopUpdateLocation() {
        if (this::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateLocation() {
        var counter = 0
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for (location in p0.locations) {
                    Log.e("counter", "$counter")
                    viewModel.updateLocationPin(
                        location.latitude,
                        location.longitude
                    )
                    if (counter == 2) {
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

    private fun returnToChat() {
        if (isTaskRoot) {
            val intent = Intent(this, ChatsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            finish()
        }
        lifecycleScope.cancel()
    }
}
