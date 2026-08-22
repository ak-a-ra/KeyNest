package com.example.core.files

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VaultFileManager(private val context: Context) {

    suspend fun exportTextFile(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("Failed to open output stream"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importTextFile(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Failed to open input stream"))
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
