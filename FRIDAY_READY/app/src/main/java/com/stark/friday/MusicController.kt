package com.stark.friday

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.provider.MediaStore
import kotlinx.coroutines.*

/**
 * MusicController - Управление музыкой
 * Воспроизведение, паузы, переключение треков, управление громкостью
 */
class MusicController(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: String? = null
    
    /**
     * Воспроизведение музыки через системный плеер
     */
    fun playMusic(query: String = ""): String {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
                putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)
                if (query.isNotEmpty()) {
                    putExtra(android.provider.MediaStore.EXTRA_MEDIA_TITLE, query)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            if (query.isNotEmpty()) {
                "Запускаю '$query', Босс. Система музыкального сопровождения активна."
            } else {
                "Воспроизведение запущено, Сэр."
            }
        } catch (e: Exception) {
            "Босс, музыкальный модуль недоступен. Установите музыкальный плеер."
        }
    }
    
    /**
     * Пауза/воспроизведение
     */
    fun togglePlayPause(): String {
        return try {
            val intent = Intent("com.android.music.musicservicecommand").apply {
                putExtra("command", "togglepause")
            }
            context.sendBroadcast(intent)
            "Переключение воспроизведения выполнено, Босс."
        } catch (e: Exception) {
            "Не удалось переключить воспроизведение, Сэр."
        }
    }
    
    /**
     * Следующий трек
     */
    fun nextTrack(): String {
        return try {
            val intent = Intent("com.android.music.musicservicecommand").apply {
                putExtra("command", "next")
            }
            context.sendBroadcast(intent)
            "Переключаю на следующий трек, Босс."
        } catch (e: Exception) {
            "Не удалось переключить трек, Сэр."
        }
    }
    
    /**
     * Предыдущий трек
     */
    fun previousTrack(): String {
        return try {
            val intent = Intent("com.android.music.musicservicecommand").apply {
                putExtra("command", "previous")
            }
            context.sendBroadcast(intent)
            "Возвращаюсь к предыдущему треку, Босс."
        } catch (e: Exception) {
            "Не удалось вернуться назад, Сэр."
        }
    }
    
    /**
     * Остановка воспроизведения
     */
    fun stopMusic(): String {
        return try {
            val intent = Intent("com.android.music.musicservicecommand").apply {
                putExtra("command", "stop")
            }
            context.sendBroadcast(intent)
            mediaPlayer?.release()
            mediaPlayer = null
            "Воспроизведение остановлено, Босс."
        } catch (e: Exception) {
            "Музыкальный модуль уже неактивен, Сэр."
        }
    }
    
    /**
     * Установка громкости (0-100%)
     */
    fun setVolume(percent: Int): String {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = (maxVolume * percent / 100).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        return "Громкость установлена на $percent%, Босс."
    }
    
    /**
     * Увеличить громкость
     */
    fun volumeUp(): String {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
        val current = getCurrentVolume()
        return "Громкость увеличена до $current%, Сэр."
    }
    
    /**
     * Уменьшить громкость
     */
    fun volumeDown(): String {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
        val current = getCurrentVolume()
        return "Громкость снижена до $current%, Сэр."
    }
    
    /**
     * Получить текущую громкость
     */
    fun getCurrentVolume(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (current * 100 / max)
    }
    
    /**
     * Обработка голосовой команды
     */
    fun processVoiceCommand(command: String): String {
        return when {
            command.contains("включи") || command.contains("играй") || command.contains("воспроизведи") -> {
                val query = command.replace(Regex("включи|играй|воспроизведи|музыку|песню"), "").trim()
                playMusic(query)
            }
            command.contains("пауза") || command.contains("стоп") -> togglePlayPause()
            command.contains("следующ") || command.contains("дальше") -> nextTrack()
            command.contains("предыдущ") || command.contains("назад") -> previousTrack()
            command.contains("громче") -> volumeUp()
            command.contains("тише") -> volumeDown()
            command.contains("громкость") -> {
                val numbers = Regex("\\d+").find(command)?.value?.toIntOrNull()
                if (numbers != null) {
                    setVolume(numbers)
                } else {
                    "Текущая громкость ${getCurrentVolume()}%, Босс."
                }
            }
            else -> "Команда не распознана. Доступны: играй, пауза, следующий, громче, тише."
        }
    }
}
