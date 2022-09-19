package com.devwarex.chatapp.db

import androidx.room.Entity

@Entity(tableName = "contacts_table")
data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val userId: String?,
    val img: String?
)
