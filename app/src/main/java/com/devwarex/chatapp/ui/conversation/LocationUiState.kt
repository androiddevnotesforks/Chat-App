package com.devwarex.chatapp.ui.conversation


data class LocationUiState(
    val requestLastKnownLocation: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val isLocationPermissionGranted: Boolean = false
)
