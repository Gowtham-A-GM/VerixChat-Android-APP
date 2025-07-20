package com.example.verixchat.views

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.verixchat.R
import com.example.verixchat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//
//            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
//
//            insets
//        }

//        binding.btnDark.setOnClickListener {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
//
//        binding.btnLight.setOnClickListener {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }

    }
}
