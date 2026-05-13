package com.stark.friday

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallManager(private val context: Context) {
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val geminiAI = GeminiAI()
    private val voiceHandler = VoiceHandler(context)
    
    fun answerCall() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            try {
                telecomManager.acceptRingingCall()
            } catch (e: Exception) {
                // Fallback для старых версий Android
            }
        }
    }
    
    fun endCall() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            try {
                telecomManager.endCall()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    fun handleIncomingCall(phoneNumber: String, callerName: String?) {
        val isVIP = Config.VIP_CONTACTS.contains(phoneNumber)
        val currentProtocol = PreferenceManager.getCurrentProtocol(context)
        
        // Логика автоответа
        when {
            isVIP -> {
                // VIP всегда проходят
                answerCall()
                voiceHandler.speak("Босс, входящий звонок от VIP-контакта: ${callerName ?: phoneNumber}")
            }
            currentProtocol == Config.Protocol.SILENCE -> {
                // В режиме ТИШИНА - отклоняем не-VIP
                endCall()
                sendAutoSMS(phoneNumber, "Босс в режиме ТИШИНА. Перезвоните позже или пишите в экстренных случаях.")
            }
            currentProtocol == Config.Protocol.VERONIKA -> {
                // В режиме ВЕРОНИКА - отклоняем и уведомляем
                endCall()
                sendAutoSMS(phoneNumber, "Сейчас в режиме учёбы. Перезвоню позже.")
            }
            else -> {
                // Обычный режим - отвечаем
                answerCall()
                voiceHandler.speak("Принимаю звонок от ${callerName ?: phoneNumber}")
            }
        }
    }
    
    fun sendAutoSMS(phoneNumber: String, message: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    fun handleVoiceCommand(command: String, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val protocol = PreferenceManager.getCurrentProtocol(context)
            val response = geminiAI.chat(command, protocol)
            callback(response)
        }
    }
}

object PreferenceManager {
    private const val PREF_NAME = "friday_prefs"
    private const val KEY_PROTOCOL = "current_protocol"
    
    fun setCurrentProtocol(context: Context, protocol: Config.Protocol) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROTOCOL, protocol.name)
            .apply()
    }
    
    fun getCurrentProtocol(context: Context): Config.Protocol {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val protocolName = prefs.getString(KEY_PROTOCOL, Config.Protocol.NORMAL.name)
        return Config.Protocol.valueOf(protocolName ?: Config.Protocol.NORMAL.name)
    }
}
