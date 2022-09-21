package com.devwarex.chatapp.di

import android.content.Context
import com.devwarex.chatapp.datastore.DatastoreImpl
import com.devwarex.chatapp.db.AppDao
import com.devwarex.chatapp.db.AppRoomDatabase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun getInstanceRoomDbDao(@ApplicationContext context: Context): AppDao = AppRoomDatabase.getInstance(context).appDbDao()

    @Provides
    @Singleton
    fun getInstanceDatastore(@ApplicationContext context: Context): DatastoreImpl = DatastoreImpl(context)

}