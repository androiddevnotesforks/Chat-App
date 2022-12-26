package com.devwarex.chatapp.ui.verify

import com.devwarex.chatapp.models.country.CountryModel

data class VerifyUiState(
    val sent: Boolean = false,
    val requestingCode: Boolean = false,
    val verifying: Boolean = false,
    val success: Boolean = false,
    val drop: Boolean = false,
    val selectedCountry: CountryModel? = null,
    val phone: String = "",
    val hintPhone: String = ""
)
