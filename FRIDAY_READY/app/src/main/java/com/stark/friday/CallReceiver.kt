package com.stark.friday

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Входящий звонок
                    phoneNumber?.let {
                        val callManager = CallManager(context)
                        callManager.handleIncomingCall(it, null)
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Звонок принят
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Звонок завершён
                }
            }
        }
    }
}
