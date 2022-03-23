package com.devwarex.chatapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val uid: String,
    val name: String,
    val img: String,
    val email: String,
    val joinedAt: Long
)
