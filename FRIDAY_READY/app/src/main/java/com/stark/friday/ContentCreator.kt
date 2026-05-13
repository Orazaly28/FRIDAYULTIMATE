package com.stark.friday

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

/**
 * Модуль создания контента (документы, презентации, таблицы, сайты, код)
 * Работает через Gemini AI + локальное сохранение файлов
 */
class ContentCreator(private val context: Context) {
    private val geminiAI = GeminiAI()
    
    // Базовая папка для всех файлов F.R.I.D.A.Y.
    private val baseFolder = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "FRIDAY_Files"
    )
    
    init {
        // Создаём папки при инициализации
        createFolders()
    }
    
    private fun createFolders() {
        val folders = listOf(
            Config.FOLDER_DOCUMENTS,
            Config.FOLDER_PRESENTATIONS,
            Config.FOLDER_TABLES,
            Config.FOLDER_WEBSITES,
            Config.FOLDER_CODE
        )
        
        folders.forEach { folderName ->
            File(baseFolder, folderName).apply {
                if (!exists()) mkdirs()
            }
        }
    }
    
    /**
     * Создать документ Word/PDF
     */
    fun createDocument(topic: String, language: String = "ru"): Result<String> {
        return try {
            // Генерируем контент через Gemini
            val content = geminiAI.generateDocument(topic, "report")
            
            // Создаём текстовый файл (упрощённая версия, для полного Word нужна библиотека Apache POI)
            val fileName = "Document_${System.currentTimeMillis()}.txt"
            val file = File(File(baseFolder, Config.FOLDER_DOCUMENTS), fileName)
            
            FileOutputStream(file).use { output ->
                output.write("=== F.R.I.D.A.Y. DOCUMENT ===\n".toByteArray())
                output.write("Тема: $topic\n\n".toByteArray())
                output.write(content.toByteArray())
                output.write("\n\n=== Создано F.R.I.D.A.Y. ===".toByteArray())
            }
            
            Result.success("Документ создан: ${file.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Создать презентацию
     */
    fun createPresentation(topic: String): Result<String> {
        return try {
            val content = geminiAI.generateDocument(topic, "presentation")
            
            val fileName = "Presentation_${System.currentTimeMillis()}.txt"
            val file = File(File(baseFolder, Config.FOLDER_PRESENTATIONS), fileName)
            
            FileOutputStream(file).use { output ->
                output.write("=== F.R.I.D.A.Y. PRESENTATION ===\n".toByteArray())
                output.write("Тема: $topic\n\n".toByteArray())
                output.write(content.toByteArray())
                output.write("\n\n=== Создано F.R.I.D.A.Y. ===".toByteArray())
            }
            
            Result.success("Презентация создана: ${file.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Создать таблицу Excel
     */
    fun createTable(description: String): Result<String> {
        return try {
            val content = geminiAI.generateDocument(description, "table")
            
            val fileName = "Table_${System.currentTimeMillis()}.csv"
            val file = File(File(baseFolder, Config.FOLDER_TABLES), fileName)
            
            FileOutputStream(file).use { output ->
                output.write("# F.R.I.D.A.Y. TABLE\n".toByteArray())
                output.write("# Описание: $description\n\n".toByteArray())
                output.write(content.toByteArray())
            }
            
            Result.success("Таблица создана: ${file.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Создать веб-сайт (HTML)
     */
    fun createWebsite(description: String): Result<String> {
        return try {
            val prompt = "Создай полный HTML код для сайта: $description. Включи HTML, CSS и JavaScript в один файл. Адаптивный дизайн."
            val htmlContent = geminiAI.chat(prompt)
            
            val fileName = "Website_${System.currentTimeMillis()}.html"
            val file = File(File(baseFolder, Config.FOLDER_WEBSITES), fileName)
            
            FileOutputStream(file).use { output ->
                output.write(htmlContent.toByteArray())
            }
            
            Result.success("Веб-сайт создан: ${file.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Написать код
     */
    fun writeCode(language: String, task: String): Result<String> {
        return try {
            val code = geminiAI.generateCode(language, task)
            
            val extension = when (language.lowercase()) {
                "python" -> "py"
                "java" -> "java"
                "javascript", "js" -> "js"
                "kotlin" -> "kt"
                "c++" -> "cpp"
                else -> "txt"
            }
            
            val fileName = "Code_${System.currentTimeMillis()}.$extension"
            val file = File(File(baseFolder, Config.FOLDER_CODE), fileName)
            
            FileOutputStream(file).use { output ->
                output.write("# F.R.I.D.A.Y. CODE GENERATOR\n".toByteArray())
                output.write("# Язык: $language\n".toByteArray())
                output.write("# Задача: $task\n\n".toByteArray())
                output.write(code.toByteArray())
            }
            
            Result.success("Код создан: ${file.absolutePath}\n\n$code")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Перевод текста
     */
    fun translateText(text: String, fromLang: String, toLang: String): String {
        return geminiAI.translate(text, fromLang, toLang)
    }
    
    /**
     * Получить список созданных файлов
     */
    fun getCreatedFiles(category: String): List<File> {
        val folder = when (category) {
            "documents" -> File(baseFolder, Config.FOLDER_DOCUMENTS)
            "presentations" -> File(baseFolder, Config.FOLDER_PRESENTATIONS)
            "tables" -> File(baseFolder, Config.FOLDER_TABLES)
            "websites" -> File(baseFolder, Config.FOLDER_WEBSITES)
            "code" -> File(baseFolder, Config.FOLDER_CODE)
            else -> baseFolder
        }
        
        return folder.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
