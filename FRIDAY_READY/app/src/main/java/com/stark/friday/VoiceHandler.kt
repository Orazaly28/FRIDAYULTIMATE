package com.stark.friday

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.*

class VoiceHandler(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    interface VoiceCallback {
        fun onSpeechRecognized(text: String)
        fun onError(error: String)
    }
    
    init {
        // Инициализация TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ru", "RU")
                tts?.setPitch(Config.TTS_PITCH)
                tts?.setSpeechRate(Config.TTS_SPEED)
            }
        }
        
        // Инициализация Speech Recognition
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }
    
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "friday_utterance")
    }
    
    fun speakWithLanguage(text: String, language: String) {
        val locale = when (language.lowercase()) {
            "kk", "казахский", "kazakh" -> Locale("kk", "KZ")
            "ru", "русский", "russian" -> Locale("ru", "RU")
            "en", "английский", "english" -> Locale.ENGLISH
            else -> Locale("ru", "RU")
        }
        tts?.language = locale
        speak(text)
    }
    
    fun startListening(callback: VoiceCallback, language: String = "ru-RU") {
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }
            
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            
            override fun onError(error: Int) {
                isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Речь не распознана"
                    SpeechRecognizer.ERROR_AUDIO -> "Ошибка микрофона"
                    else -> "Неизвестная ошибка: $error"
                }
                callback.onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    callback.onSpeechRecognized(matches[0])
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        isListening = false
    }
}
