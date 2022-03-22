package com.devwarex.chatapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {


    @Query("select * from chat_messages_table order by timestamp desc")
    fun getMessages(): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: Message): Completable
}