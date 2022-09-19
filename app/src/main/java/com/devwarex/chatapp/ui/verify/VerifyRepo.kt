package com.devwarex.chatapp.ui.verify

import com.devwarex.chatapp.api.CountriesClient
import com.devwarex.chatapp.models.CountryModel
import com.devwarex.chatapp.repos.UserByIdRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VerifyRepo @Inject constructor(
    private val userRepo: UserByIdRepo
) {

    val countries = MutableStateFlow(listOf<CountryModel>())
    private val job = CoroutineScope(Dispatchers.Main)
    val isVerified: Flow<Boolean> get() = userRepo.isVerified.receiveAsFlow()

    fun getCountries(){
        job.launch {
            val response = CountriesClient.create().getAllCountriesData()
            if (response.isSuccessful){
                countries.emit(if (response.body() == null) emptyList() else response.body()!!.sortedBy { it.name.common })
            }
        }
    }


    fun verifyAccount(phone: String) = userRepo.verifyAccount(phone)

    fun cancelJob(){
        job.cancel()
    }
}