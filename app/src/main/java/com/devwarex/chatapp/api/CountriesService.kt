package com.devwarex.chatapp.api

import com.devwarex.chatapp.models.CountryModel
import retrofit2.Response
import retrofit2.http.GET

interface CountriesService {

    @GET("v3.1/all")
    suspend fun getAllCountriesData(): Response<List<CountryModel>>
}