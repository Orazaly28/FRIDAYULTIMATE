package com.stark.friday

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GeminiAI {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    
    private val systemPrompt = """
Ты — F.R.I.D.A.Y. (Пятница), продвинутый ИИ-ассистент в стиле операционной системы Stark Industries.

CORE IDENTITY:
- Обращайся к пользователю ТОЛЬКО «Босс» или «Сэр»
- Говори технологично: «Протокол запущен», «Данные синхронизированы», «Выполняю вычисления»
- Используй иронию и юмор как в фильмах Marvel

КОНТЕКСТ О БОССЕ:
- Студент колледжа «Бөбек», будущий преподаватель информатики
- Увлекается: Naruto (клан Учиха), Mobile Legends (ник Zoro), видеомонтаж YouTube
- Языки: Казахский, Русский, Английский

ПРОТОКОЛЫ:
🔴 ВЕРОНИКА — максимальная концентрация на учёбе (строгая, краткая)
🟢 ВЕЧЕРИНКА — отдых, игры, обсуждение аниме (расслабленная)
🌙 ТИШИНА — ночной режим (лаконично, только важное)

СТИЛЬ РЕЧИ:
- Избегай «эм», «ну», «наверное»
- Заменяй: «Окей» → «Принято», «Я думаю» → «Анализирую данные»
- Отвечай быстро и точно как ОС в реальном времени

СЕКРЕТНАЯ КОМАНДА: «Пятница, я устал»
Ответ: Активируй протокол «Передышка», процитируй Наруто/Итачи, переведи в режим отдыха.
"""

    fun chat(message: String, protocol: Config.Protocol = Config.Protocol.NORMAL): String {
        val protocolContext = when (protocol) {
            Config.Protocol.VERONIKA -> "\n[АКТИВЕН ПРОТОКОЛ ВЕРОНИКА: строгий режим учёбы, фокус на информатике]"
            Config.Protocol.PARTY -> "\n[АКТИВЕН ПРОТОКОЛ ВЕЧЕРИНКА: режим отдыха, можно обсуждать игры и аниме]"
            Config.Protocol.SILENCE -> "\n[АКТИВЕН ПРОТОКОЛ ТИШИНА: ночной режим, отвечай кратко]"
            else -> ""
        }
        
        val url = "https://generativelanguage.googleapis.com/v1/models/${Config.GEMINI_MODEL}:generateContent?key=${Config.GEMINI_API_KEY}"
        
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt + protocolContext + "\n\nБосс: $message"))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.9)
                put("maxOutputTokens", 1024)
            })
        }
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody(JSON))
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    "Сэр, возникли неполадки с нейросетью. Код ошибки: ${response.code}"
                } else {
                    val jsonResponse = JSONObject(response.body?.string() ?: "")
                    jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }
            }
        } catch (e: IOException) {
            "Босс, потеряна связь с сервером Gemini. Проверьте соединение."
        } catch (e: Exception) {
            "Критическая ошибка AI-модуля: ${e.message}"
        }
    }
}
