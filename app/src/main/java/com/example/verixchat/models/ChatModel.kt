package com.example.verixchat.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val isUser: Boolean,
    val chatNo: Long
)