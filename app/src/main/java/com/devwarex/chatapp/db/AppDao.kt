package com.devwarex.chatapp.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {


    @Query("select * from user_table where uid = :uid")
    fun getUserById(uid: String):Flow<User?>

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
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: Contact)

    @Query("delete from chat_messages_table")
    suspend fun dropMessages()

    @Query("delete from chat_table")
    suspend fun dropChats()

}