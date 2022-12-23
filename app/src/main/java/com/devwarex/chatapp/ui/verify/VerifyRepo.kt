package com.devwarex.chatapp.ui.verify

import com.devwarex.chatapp.api.CountriesService
import com.devwarex.chatapp.models.CountryModel
import com.devwarex.chatapp.repos.UserByIdRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

class VerifyRepo @Inject constructor(
    private val userRepo: UserByIdRepo,
    private val countryService: CountriesService,
    ) {

    val countries = MutableStateFlow(listOf<CountryModel>())
    val selectedCountry = MutableStateFlow<CountryModel?>(null)
    private val coroutine = CoroutineScope(Dispatchers.Main)
    val isVerified: Flow<Boolean> get() = userRepo.isVerified.receiveAsFlow()

    fun getCountries(){
        coroutine.launch {
            val response = countryService.getAllCountriesData()
            if (response.isSuccessful){
                countries.emit(if (response.body() == null) emptyList() else response.body()!!.sortedBy { it.name.common })
            }
        }
    }


    suspend fun getCountryByCode(code: String) {
        val response: Response<List<CountryModel>> = countryService.getCountryByCode(code)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                selectedCountry.emit(body[0])
            }
        }
    }

    fun verifyAccount(phone: String) = userRepo.verifyAccount(phone)

    fun cancelCoroutine(){
        coroutine.cancel()
    }
}