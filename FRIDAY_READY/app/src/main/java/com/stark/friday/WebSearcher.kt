package com.stark.friday

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.*

/**
 * WebSearcher - Поиск в интернете
 * Google, YouTube, карты, новости
 */
class WebSearcher(private val context: Context) {
    
    private val geminiAI = GeminiAI(context)
    
    /**
     * Поиск в Google
     */
    fun searchGoogle(query: String): String {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Ищу '$query' в Google, Босс."
        } catch (e: Exception) {
            "Поиск недоступен. Проверьте подключение к интернету, Сэр."
        }
    }
    
    /**
     * Открыть URL
     */
    fun openUrl(url: String): String {
        return try {
            val uri = if (!url.startsWith("http")) "https://$url" else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Открываю $url, Босс."
        } catch (e: Exception) {
            "Не удалось открыть ссылку: ${e.message}"
        }
    }
    
    /**
     * Поиск на YouTube
     */
    fun searchYouTube(query: String): String {
        return try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Ищу '$query' на YouTube, Сэр."
        } catch (e: Exception) {
            // Fallback: открыть через браузер
            openUrl("https://www.youtube.com/results?search_query=${query.replace(" ", "+")}")
        }
    }
    
    /**
     * Поиск на картах
     */
    fun searchMaps(query: String): String {
        return try {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Ищу '$query' на картах, Босс."
        } catch (e: Exception) {
            "Карты недоступны: ${e.message}"
        }
    }
    
    /**
     * Умный поиск с ИИ (использует Gemini для ответа)
     */
    suspend fun smartSearch(query: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                Ты - F.R.I.D.A.Y., AI-ассистент Тони Старка.
                Обращайся "Босс" или "Сэр".
                
                Пользователь спрашивает: $query
                
                Если это простой вопрос - ответь кратко и технологично.
                Если нужен поиск в интернете - скажи "Требуется поиск в Google".
                """.trimIndent()
                
                geminiAI.processMessage(prompt)
            } catch (e: Exception) {
                "Босс, умный поиск временно недоступен. Ошибка: ${e.message}"
            }
        }
    }
    
    /**
     * Поиск новостей
     */
    fun searchNews(query: String): String {
        return openUrl("https://news.google.com/search?q=${query.replace(" ", "+")}")
    }
    
    /**
     * Поиск изображений
     */
    fun searchImages(query: String): String {
        return openUrl("https://www.google.com/search?tbm=isch&q=${query.replace(" ", "+")}")
    }
    
    /**
     * Обработка голосовой команды
     */
    suspend fun processVoiceCommand(command: String): String {
        return when {
            command.contains("найди") || command.contains("поищи") || command.contains("поиск") -> {
                val query = command.replace(Regex("найди|поищи|поиск|в|гугле|google"), "").trim()
                
                when {
                    command.contains("ютуб") || command.contains("youtube") -> searchYouTube(query)
                    command.contains("карт") || command.contains("maps") -> searchMaps(query)
                    command.contains("новост") || command.contains("news") -> searchNews(query)
                    command.contains("картинк") || command.contains("фото") -> searchImages(query)
                    else -> searchGoogle(query)
                }
            }
            command.contains("открой") -> {
                val url = command.replace("открой", "").trim()
                openUrl(url)
            }
            command.startsWith("что такое") || command.startsWith("кто такой") || 
            command.startsWith("как") || command.startsWith("почему") -> {
                smartSearch(command)
            }
            else -> {
                "Команда не распознана. Доступны: найди [запрос], открой [сайт], что такое [вопрос]."
            }
        }
    }
}
