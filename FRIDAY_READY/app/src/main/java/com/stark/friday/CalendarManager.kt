package com.stark.friday

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.*

/**
 * CalendarManager - Управление календарём
 * Добавление событий, напоминаний, просмотр расписания
 */
class CalendarManager(private val context: Context) {
    
    /**
     * Добавить событие в календарь
     */
    fun addEvent(
        title: String,
        description: String = "",
        startTime: Long,
        endTime: Long,
        location: String = ""
    ): String {
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                "Событие '$title' добавлено в календарь, Босс."
            } else {
                "Не удалось добавить событие, Сэр."
            }
        } catch (e: SecurityException) {
            "Требуется разрешение на доступ к календарю, Босс."
        } catch (e: Exception) {
            "Ошибка добавления события: ${e.message}"
        }
    }
    
    /**
     * Добавить напоминание
     */
    fun addReminder(title: String, minutes: Int): String {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
        }
        
        val endTime = calendar.timeInMillis + (30 * 60 * 1000) // 30 минут длительность
        return addEvent(
            title = "Напоминание: $title",
            description = "Установлено через F.R.I.D.A.Y.",
            startTime = calendar.timeInMillis,
            endTime = endTime
        )
    }
    
    /**
     * Получить события на сегодня
     */
    fun getTodayEvents(): String {
        return try {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
            
            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.DESCRIPTION
            )
            
            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
            val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )
            
            val events = mutableListOf<String>()
            cursor?.use {
                while (it.moveToNext()) {
                    val title = it.getString(0)
                    val start = it.getLong(1)
                    val time = android.text.format.DateFormat.format("HH:mm", start)
                    events.add("$time - $title")
                }
            }
            
            if (events.isEmpty()) {
                "Босс, на сегодня событий не запланировано. Протоколы свободны."
            } else {
                "Расписание на сегодня, Сэр:\n" + events.joinToString("\n")
            }
        } catch (e: SecurityException) {
            "Требуется разрешение на доступ к календарю, Босс."
        } catch (e: Exception) {
            "Ошибка чтения календаря: ${e.message}"
        }
    }
    
    /**
     * Обработка голосовой команды
     */
    fun processVoiceCommand(command: String): String {
        return when {
            command.contains("добавь") || command.contains("создай событие") -> {
                // Извлечение данных из команды (упрощённо)
                val title = command.replace(Regex("добавь|создай событие|в календарь"), "").trim()
                val tomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                }
                addEvent(
                    title = title,
                    startTime = tomorrow.timeInMillis,
                    endTime = tomorrow.timeInMillis + (60 * 60 * 1000)
                )
            }
            command.contains("напомни") -> {
                val title = command.replace(Regex("напомни|мне|через"), "").trim()
                val minutes = Regex("\\d+").find(command)?.value?.toIntOrNull() ?: 60
                addReminder(title, minutes)
            }
            command.contains("что сегодня") || command.contains("расписание") -> getTodayEvents()
            else -> "Команда не распознана. Доступны: добавь событие, напомни через N минут, что сегодня."
        }
    }
}
