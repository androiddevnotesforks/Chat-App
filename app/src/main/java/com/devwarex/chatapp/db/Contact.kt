package com.devwarex.chatapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_table")
data class Contact(
    val name: String,
    @PrimaryKey val phone: String,
    val isFound: Boolean
)
