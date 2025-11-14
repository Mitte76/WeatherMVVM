package com.example.weathermvvm.helpers

import android.content.Context
import com.example.weathermvvm.Constants
import com.example.weathermvvm.models.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.readText

object NetworkHelper {
    suspend fun httpGet(url: String): Result<String> = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Result.success(responseBody)
            } else {
                Result.failure(IOException("HTTP Error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection.disconnect()
        }
    }

    fun loadWeatherFromCache(context: Context, fileName: String): WeatherResponse? {
        val cacheFile = File(context.filesDir, fileName)
        return try {
            if (
                cacheFile.exists() &&
                System.currentTimeMillis() - cacheFile.lastModified() < Constants.CACHEFILE_LIFETIME &&
                cacheFile.containsJson() &&
                cacheFile.length() > 100L
            ) {
                val data = cacheFile.readText()
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<WeatherResponse>(data)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun writeFileToCache(context: Context, fileName: String, jsonText: String) {
        val cacheFile = File(context.filesDir, fileName)
        println("Writing to cache: $fileName")
        cacheFile.writeText(jsonText)
    }

    fun clearCache(context: Context) {
        val cacheDir = context.filesDir
        // List all files in the directory
        val files = cacheDir.listFiles()
        println("Clearing cache...")
        files?.filter { it.isFile && it.extension == "json" }
            ?.forEach { file ->
                if (file.delete()) {
                    println("Deleted cache file: ${file.name}")
                } else {
                    println("Failed to delete cache file: ${file.name}")
                }
            }
    }

    fun String.isJson(): Boolean = this.trimStart().startsWith("{")

    fun File.containsJson(): Boolean = try {
        this.bufferedReader().use { reader ->
            reader.lineSequence()
                .map { it.trimStart() }
                .firstOrNull()
                ?.startsWith("{") == true
        }
    } catch (_: Exception) {
        false
    }

}