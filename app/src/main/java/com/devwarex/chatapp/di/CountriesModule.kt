package com.devwarex.chatapp.di

import com.devwarex.chatapp.api.CountriesClient
import com.devwarex.chatapp.api.CountriesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
class CountriesModule {

    @Provides
    @ViewModelScoped
    fun provideCountriesApiService(): CountriesService = CountriesClient.create()

}