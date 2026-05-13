package com.stark.friday

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var geminiAI: GeminiAI
    private lateinit var voiceHandler: VoiceHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        geminiAI = GeminiAI()
        voiceHandler = VoiceHandler(this)
        
        checkPermissions()
        
        // Запуск фонового сервиса
        val serviceIntent = Intent(this, FridayService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
        
        ActivityCompat.requestPermissions(this, permissions, 100)
    }
}
