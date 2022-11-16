package com.devwarex.chatapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "chat_app_datastore")

class DatastoreImpl @Inject constructor(val context: Context){

    companion object{

        private const val REFRESH_CONTACTS_TIMEOUT: String = "user_refresh_token"


        fun create(context: Context): DatastoreImpl {
            return DatastoreImpl(context)
        }

        private val REFRESH_CONTACTS_TIMEOUT_KEY = longPreferencesKey(REFRESH_CONTACTS_TIMEOUT)


    }

    val contactsTimeout: Flow<Long> get() = context.datastore.data.map { it[REFRESH_CONTACTS_TIMEOUT_KEY] ?: 0L }
    suspend fun updateContactsTimeout(time: Long) = context.datastore.edit { it[REFRESH_CONTACTS_TIMEOUT_KEY] = time}

    suspend fun clear(){
        context.datastore.edit { clear() }
    }
}