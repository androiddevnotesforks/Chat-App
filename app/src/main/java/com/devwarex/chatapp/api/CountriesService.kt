package com.devwarex.chatapp.api

import com.devwarex.chatapp.models.country.CountryModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CountriesService {

    @GET("v3.1/all")
    suspend fun getAllCountriesData(): Response<List<CountryModel>>

    @GET("v3.1/alpha/{code}")
    suspend fun getCountryByCode(
        @Path("code") code: String
    ): Response<List<CountryModel>>
}