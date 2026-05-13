package com.stark.friday

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class FridayService : Service() {
    private lateinit var callManager: CallManager
    private lateinit var voiceHandler: VoiceHandler
    
    override fun onCreate() {
        super.onCreate()
        callManager = CallManager(this)
        voiceHandler = VoiceHandler(this)
        
        createNotificationChannel()
        startForeground(Config.SERVICE_NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Сервис работает в фоне постоянно
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Config.NOTIFICATION_CHANNEL_ID,
                "F.R.I.D.A.Y. Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Фоновая работа ассистента"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val protocol = PreferenceManager.getCurrentProtocol(this)
        val protocolEmoji = when (protocol) {
            Config.Protocol.VERONIKA -> "🔴"
            Config.Protocol.PARTY -> "🟢"
            Config.Protocol.SILENCE -> "🌙"
            else -> "⚪"
        }
        
        return NotificationCompat.Builder(this, Config.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("$protocolEmoji F.R.I.D.A.Y. активна")
            .setContentText("Все системы в режиме онлайн")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
