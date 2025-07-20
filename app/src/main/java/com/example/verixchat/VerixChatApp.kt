package com.example.verixchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class VerixChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}