package com.example.verixchat.models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatModel::class], version = 2, exportSchema = false)
abstract class ChatDB: RoomDatabase() {


    abstract fun chatDao(): ChatDao

    companion object{
        private var INSTANCE: ChatDB? = null

        fun getDatabase(context: Context): ChatDB{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, ChatDB::class.java, "VerixChatDB"
                )
                    .fallbackToDestructiveMigration() // wipes DB when version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}