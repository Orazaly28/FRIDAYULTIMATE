package com.stark.friday

object Config {
    // Gemini API
    const val GEMINI_API_KEY = "AIzaSyCVgxNOaOuLkH5xbAGCra5UJfJ2rlgqIV8"
    const val GEMINI_MODEL = "gemini-1.5-flash"
    
    // Telegram Bot (для логирования, опционально)
    const val TELEGRAM_BOT_TOKEN = "8650648555:AAGLsJKp4Gzu1St5QUUK_Wg3vRM0fiojepE"
    const val BOSS_TELEGRAM_ID = "5554266021"
    
    // VIP контакты (26 номеров - все bypass DND)
    val VIP_CONTACTS = setOf(
        "+77052885579", "+77472885579", "+77015993142", "+77085993142",
        "+77002213694", "+77472213694", "+77473334469", "+77003334469",
        "+77473334453", "+77003334453", "+77473334456", "+77003334456",
        "+77473334454", "+77003334454", "+77473334457", "+77003334457",
        "+77473334455", "+77003334455", "+77473334467", "+77003334467",
        "+77473334459", "+77003334459", "+77473334471", "+77003334471",
        "+77473334470", "+77003334470"
    )
    
    // Режимы работы
    enum class Protocol {
        NORMAL,      // Обычный режим
        VERONIKA,    // 🔴 Максимальная концентрация (учёба)
        PARTY,       // 🟢 Отдых и медиа
        SILENCE      // 🌙 Ночной режим (тихо, только критические)
    }
    
    // Настройки голоса
    const val TTS_LANGUAGE = "ru-RU"
    const val TTS_PITCH = 1.1f  // Женский голос
    const val TTS_SPEED = 1.0f
    
    // Системные настройки
    const val NOTIFICATION_CHANNEL_ID = "friday_service"
    const val NOTIFICATION_ID = 100
    const val SERVICE_NOTIFICATION_ID = 101
}
