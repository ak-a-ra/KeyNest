package com.example.core.network

import com.example.core.model.AuthType
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.model.ProviderProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface ConnectionResult {
    data class Success(
        val latencyMs: Long,
        val statusCode: Int = 200,
        val message: String = "Connected successfully"
    ) : ConnectionResult

    data class Failure(
        val statusCode: Int,
        val errorMessage: String,
        val latencyMs: Long? = null
    ) : ConnectionResult

    data class Error(
        val exception: Throwable,
        val message: String
    ) : ConnectionResult
}

object ProviderConnectionTester {

    suspend fun testConnection(
        profile: ProviderProfile,
        overrideKey: String? = null
    ): ConnectionResult = withContext(Dispatchers.IO) {
        val apiKey = overrideKey ?: profile.activeApiKey
        if (apiKey.isBlank() && profile.id != "ollama") {
            return@withContext ConnectionResult.Failure(
                statusCode = 400,
                errorMessage = "No active API key set for testing"
            )
        }

        val preset = ProviderPresets.findById(profile.id)
        val rawBaseUrl = profile.baseUrl.ifBlank { preset.defaultEndpoint }
        if (rawBaseUrl.isBlank()) {
            return@withContext ConnectionResult.Failure(
                statusCode = 400,
                errorMessage = "Base URL is not configured"
            )
        }

        val cleanBaseUrl = rawBaseUrl.trimEnd('/')
        val (targetUrlStr, authHeaders) = buildProbeUrlAndHeaders(preset, cleanBaseUrl, apiKey, profile.customHeadersJson)

        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null
        try {
            val url = URL(targetUrlStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/json")
                authHeaders.forEach { (k, v) ->
                    setRequestProperty(k, v)
                }
            }

            val statusCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime

            when (statusCode) {
                in 200..299 -> ConnectionResult.Success(
                    latencyMs = latency,
                    statusCode = statusCode,
                    message = "HTTP $statusCode OK"
                )
                401 -> ConnectionResult.Failure(
                    statusCode = 401,
                    errorMessage = "HTTP 401: Unauthorized (Invalid Key)",
                    latencyMs = latency
                )
                403 -> ConnectionResult.Failure(
                    statusCode = 403,
                    errorMessage = "HTTP 403: Forbidden (Access Denied)",
                    latencyMs = latency
                )
                404 -> ConnectionResult.Failure(
                    statusCode = 404,
                    errorMessage = "HTTP 404: Endpoint Not Found",
                    latencyMs = latency
                )
                429 -> ConnectionResult.Failure(
                    statusCode = 429,
                    errorMessage = "HTTP 429: Rate Limit Exceeded",
                    latencyMs = latency
                )
                else -> ConnectionResult.Failure(
                    statusCode = statusCode,
                    errorMessage = "HTTP $statusCode (${connection.responseMessage ?: "Error"})",
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            ConnectionResult.Error(
                exception = e,
                message = e.localizedMessage ?: "Connection failed"
            )
        } finally {
            connection?.disconnect()
        }
    }

    private fun buildProbeUrlAndHeaders(
        preset: ProviderPreset,
        baseUrl: String,
        apiKey: String,
        customHeadersJson: String
    ): Pair<String, Map<String, String>> {
        val headers = mutableMapOf<String, String>()
        preset.defaultHeaders.forEach { (k, v) -> headers[k] = v }

        try {
            if (customHeadersJson.isNotBlank() && customHeadersJson != "{}") {
                val json = JSONObject(customHeadersJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    headers[k] = json.getString(k)
                }
            }
        } catch (_: Exception) {}

        return when (preset.id.lowercase()) {
            "gemini" -> {
                val url = if (baseUrl.contains("/v1beta")) {
                    "$baseUrl/models?key=$apiKey"
                } else {
                    "$baseUrl/v1beta/models?key=$apiKey"
                }
                Pair(url, headers)
            }
            "openai", "deepseek", "groq", "openrouter" -> {
                val probe = if (baseUrl.endsWith("/models")) baseUrl else "$baseUrl/models"
                headers["Authorization"] = "Bearer $apiKey"
                Pair(probe, headers)
            }
            "anthropic" -> {
                val probe = if (baseUrl.endsWith("/models")) baseUrl else "$baseUrl/models"
                headers["x-api-key"] = apiKey
                if (!headers.containsKey("anthropic-version")) {
                    headers["anthropic-version"] = "2023-06-01"
                }
                Pair(probe, headers)
            }
            "ollama" -> {
                val probe = if (baseUrl.endsWith("/api/tags")) baseUrl else "$baseUrl/api/tags"
                if (apiKey.isNotBlank()) headers["Authorization"] = "Bearer $apiKey"
                Pair(probe, headers)
            }
            "github" -> {
                val probe = if (baseUrl.endsWith("/user")) baseUrl else "$baseUrl/user"
                headers["Authorization"] = "token $apiKey"
                if (!headers.containsKey("User-Agent")) headers["User-Agent"] = "KeyNest"
                Pair(probe, headers)
            }
            "stripe" -> {
                val probe = if (baseUrl.endsWith("/balance")) baseUrl else "$baseUrl/balance"
                headers["Authorization"] = "Bearer $apiKey"
                Pair(probe, headers)
            }
            "supabase" -> {
                headers["apikey"] = apiKey
                headers["Authorization"] = "Bearer $apiKey"
                Pair(baseUrl, headers)
            }
            else -> {
                when (preset.authType) {
                    AuthType.BEARER -> if (apiKey.isNotBlank()) headers["Authorization"] = "Bearer $apiKey"
                    AuthType.TOKEN -> if (apiKey.isNotBlank()) headers["Authorization"] = "token $apiKey"
                    AuthType.HEADER_X_API_KEY -> if (apiKey.isNotBlank()) headers["x-api-key"] = apiKey
                    AuthType.HEADER_APIKEY -> if (apiKey.isNotBlank()) headers["apikey"] = apiKey
                    AuthType.QUERY_PARAM -> {}
                    AuthType.NONE -> {}
                }
                val probe = if (preset.probePath.isNotBlank()) {
                    "$baseUrl${if (preset.probePath.startsWith("/")) "" else "/"}${preset.probePath}"
                } else baseUrl
                Pair(probe, headers)
            }
        }
    }
}
