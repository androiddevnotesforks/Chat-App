package com.devwarex.chatapp.db

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {


    @Query("select * from chat_table where id = :chatId")
    fun getChatByChatId(chatId: String): Flow<ChatRelations?>

    @Query("select * from chat_messages_table where chat_id = :chatId order by timestamp desc")
    fun getMessages(chatId: String): Flow<List<Message>>

    @Query("select * from chat_table order by lastEditAt desc")
    fun getChats(): Flow<List<ChatRelations>>

    @Query("select * from contacts_table order by isFound desc , name asc")
    fun getContacts(): Flow<List<Contact>>

    @Update
    suspend fun updateContactExist(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: Message): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChat(chat: Chat): Completable

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: Contact)
}