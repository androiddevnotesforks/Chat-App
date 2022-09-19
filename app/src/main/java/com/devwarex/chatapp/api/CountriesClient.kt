package com.devwarex.chatapp.api

import com.devwarex.chatapp.utility.Paths.Companion.REST_COUNTRIES_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CountriesClient {

    fun create(): CountriesService =
        Retrofit.Builder()
            .baseUrl(REST_COUNTRIES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountriesService::class.java)

}