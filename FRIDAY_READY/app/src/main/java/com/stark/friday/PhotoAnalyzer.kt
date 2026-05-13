package com.stark.friday

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.*

/**
 * PhotoAnalyzer - Модуль анализа изображений
 * Использует Gemini Vision API для распознавания объектов, текста, лиц
 */
class PhotoAnalyzer(private val context: Context) {
    
    private val geminiAI = GeminiAI(context)
    
    /**
     * Анализ фото по URI
     */
    suspend fun analyzePhoto(uri: Uri, question: String = "Что изображено на этом фото?"): String {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                analyzePhoto(bitmap, question)
            } catch (e: Exception) {
                "Ошибка загрузки изображения: ${e.message}"
            }
        }
    }
    
    /**
     * Анализ фото из Bitmap
     */
    suspend fun analyzePhoto(bitmap: Bitmap, question: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Конвертация Bitmap в Base64 для отправки в Gemini
                val base64Image = bitmapToBase64(bitmap)
                
                val prompt = """
                Ты - F.R.I.D.A.Y., AI-ассистент Тони Старка.
                Обращайся к пользователю "Босс" или "Сэр".
                Анализируй изображение и отвечай технологично, кратко и точно.
                
                Вопрос: $question
                """.trimIndent()
                
                // Отправка в Gemini Vision API
                geminiAI.analyzeImage(prompt, base64Image)
            } catch (e: Exception) {
                "Босс, сканирование изображения не удалось. Ошибка: ${e.message}"
            }
        }
    }
    
    /**
     * Быстрый анализ: распознавание объектов
     */
    suspend fun detectObjects(uri: Uri): String {
        return analyzePhoto(uri, "Перечисли все объекты на изображении. Кратко, списком.")
    }
    
    /**
     * OCR - извлечение текста
     */
    suspend fun extractText(uri: Uri): String {
        return analyzePhoto(uri, "Извлеки весь текст с изображения. Сохрани форматирование.")
    }
    
    /**
     * Распознавание лиц
     */
    suspend fun detectFaces(uri: Uri): String {
        return analyzePhoto(uri, "Опиши людей на фото: количество, примерный возраст, эмоции, одежду.")
    }
    
    /**
     * Анализ документов (паспорт, справки и т.д.)
     */
    suspend fun analyzeDocument(uri: Uri): String {
        return analyzePhoto(uri, "Это документ. Извлеки все данные: ФИО, даты, номера, адреса. Структурируй информацию.")
    }
    
    /**
     * Сравнение двух изображений
     */
    suspend fun comparePhotos(uri1: Uri, uri2: Uri): String {
        val analysis1 = analyzePhoto(uri1, "Опиши что на фото")
        val analysis2 = analyzePhoto(uri2, "Опиши что на фото")
        
        return geminiAI.processMessage("""
            Первое фото: $analysis1
            Второе фото: $analysis2
            
            Сравни эти два изображения. Что общего? Чем отличаются?
        """.trimIndent())
    }
    
    /**
     * Голосовая команда: "Что на этом фото?"
     */
    fun processVoiceCommand(command: String, uri: Uri): String {
        return when {
            command.contains("что") || command.contains("опиши") -> 
                runBlocking { analyzePhoto(uri, "Подробно опиши что на фото") }
            command.contains("текст") || command.contains("прочитай") -> 
                runBlocking { extractText(uri) }
            command.contains("лиц") || command.contains("люди") -> 
                runBlocking { detectFaces(uri) }
            command.contains("объект") -> 
                runBlocking { detectObjects(uri) }
            else -> runBlocking { analyzePhoto(uri, command) }
        }
    }
    
    /**
     * Утилита: конвертация Bitmap в Base64
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }
}
