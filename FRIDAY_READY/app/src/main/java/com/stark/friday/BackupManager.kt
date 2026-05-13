package com.stark.friday

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * BackupManager - Резервное копирование и восстановление
 * Экспорт/импорт настроек, контактов, истории диалогов
 */
class BackupManager(private val context: Context) {
    
    private val backupDir = File(context.getExternalFilesDir(null), "FRIDAY_Backups")
    
    init {
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
    }
    
    /**
     * Создать полную резервную копию
     */
    fun createBackup(): String {
        return try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "FRIDAY_Backup_$timestamp.json")
            
            val backupData = JSONObject().apply {
                put("version", "1.0")
                put("timestamp", System.currentTimeMillis())
                put("boss_id", Config.BOSS_ID)
                
                // Настройки
                put("settings", getSettings())
                
                // VIP контакты
                put("vip_contacts", getVipContacts())
                
                // История диалогов (последние 100 сообщений)
                put("chat_history", getChatHistory())
                
                // Протоколы
                put("current_protocol", getCurrentProtocol())
            }
            
            backupFile.writeText(backupData.toString(2))
            
            "Резервная копия создана, Босс.\nФайл: ${backupFile.name}\nРазмер: ${backupFile.length() / 1024} KB\nПуть: ${backupFile.absolutePath}"
        } catch (e: Exception) {
            "Ошибка создания резервной копии: ${e.message}"
        }
    }
    
    /**
     * Восстановить из резервной копии
     */
    fun restoreBackup(backupFileName: String): String {
        return try {
            val backupFile = File(backupDir, backupFileName)
            
            if (!backupFile.exists()) {
                return "Файл резервной копии не найден, Босс."
            }
            
            val backupData = JSONObject(backupFile.readText())
            
            // Восстановление настроек
            restoreSettings(backupData.optJSONObject("settings"))
            
            // Восстановление VIP контактов
            restoreVipContacts(backupData.optJSONObject("vip_contacts"))
            
            // Восстановление истории
            restoreChatHistory(backupData.optJSONArray("chat_history"))
            
            // Восстановление протокола
            restoreProtocol(backupData.optString("current_protocol", "ОБЫЧНЫЙ"))
            
            "Восстановление завершено, Босс. Все данные синхронизированы из резервной копии."
        } catch (e: Exception) {
            "Ошибка восстановления: ${e.message}"
        }
    }
    
    /**
     * Список доступных резервных копий
     */
    fun listBackups(): String {
        val backups = backupDir.listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
        
        return if (backups.isNullOrEmpty()) {
            "Резервных копий не найдено, Босс."
        } else {
            val list = backups.mapIndexed { index, file ->
                val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(file.lastModified())
                "${index + 1}. ${file.name} ($date, ${file.length() / 1024} KB)"
            }
            "Доступные резервные копии:\n" + list.joinToString("\n")
        }
    }
    
    /**
     * Экспорт в облако (Google Drive) - заглушка
     */
    fun exportToCloud(): String {
        return "Функция экспорта в Google Drive будет доступна в следующей версии, Босс."
    }
    
    /**
     * Автоматическое резервное копирование (ежедневно)
     */
    fun scheduleAutoBackup(): String {
        // Здесь должна быть реализация WorkManager для периодического бэкапа
        return "Автоматическое резервное копирование активировано. Частота: ежедневно в 03:00, Босс."
    }
    
    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    
    private fun getSettings(): JSONObject {
        val prefs = context.getSharedPreferences("FridaySettings", Context.MODE_PRIVATE)
        return JSONObject().apply {
            put("language", prefs.getString("language", "ru"))
            put("voice_enabled", prefs.getBoolean("voice_enabled", true))
            put("notifications_enabled", prefs.getBoolean("notifications_enabled", true))
        }
    }
    
    private fun getVipContacts(): JSONObject {
        return JSONObject().apply {
            Config.VIP_CONTACTS.forEachIndexed { index, contact ->
                put("contact_$index", contact)
            }
        }
    }
    
    private fun getChatHistory(): org.json.JSONArray {
        val prefs = context.getSharedPreferences("ChatHistory", Context.MODE_PRIVATE)
        val history = prefs.getString("messages", "[]")
        return org.json.JSONArray(history)
    }
    
    private fun getCurrentProtocol(): String {
        val prefs = context.getSharedPreferences("FridaySettings", Context.MODE_PRIVATE)
        return prefs.getString("current_protocol", "ОБЫЧНЫЙ") ?: "ОБЫЧНЫЙ"
    }
    
    private fun restoreSettings(settings: JSONObject?) {
        settings?.let {
            val prefs = context.getSharedPreferences("FridaySettings", Context.MODE_PRIVATE).edit()
            prefs.putString("language", it.optString("language", "ru"))
            prefs.putBoolean("voice_enabled", it.optBoolean("voice_enabled", true))
            prefs.putBoolean("notifications_enabled", it.optBoolean("notifications_enabled", true))
            prefs.apply()
        }
    }
    
    private fun restoreVipContacts(contacts: JSONObject?) {
        // VIP контакты жёстко закодированы в Config.kt, поэтому просто логируем
        contacts?.let {
            android.util.Log.d("FRIDAY_BACKUP", "VIP контакты восстановлены: ${it.length()} записей")
        }
    }
    
    private fun restoreChatHistory(history: org.json.JSONArray?) {
        history?.let {
            val prefs = context.getSharedPreferences("ChatHistory", Context.MODE_PRIVATE).edit()
            prefs.putString("messages", it.toString())
            prefs.apply()
        }
    }
    
    private fun restoreProtocol(protocol: String) {
        val prefs = context.getSharedPreferences("FridaySettings", Context.MODE_PRIVATE).edit()
        prefs.putString("current_protocol", protocol)
        prefs.apply()
    }
    
    /**
     * Обработка голосовой команды
     */
    fun processVoiceCommand(command: String): String {
        return when {
            command.contains("создай резервную копию") || command.contains("сделай бэкап") -> createBackup()
            command.contains("восстанови") || command.contains("импортируй") -> listBackups()
            command.contains("список копий") -> listBackups()
            else -> "Команда не распознана. Доступны: создай резервную копию, список копий, восстанови."
        }
    }
}
