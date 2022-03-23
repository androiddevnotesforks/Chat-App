package com.devwarex.chatapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devwarex.chatapp.models.ChatRelations
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {


    @Query("select * from chat_messages_table order by timestamp desc")
    fun getMessages(): Flow<List<Message>>

    @Query("select * from chat_table order by lastEditAt desc")
    fun getChats(): Flow<List<ChatRelations>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: Message): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: Chat): Completable
}