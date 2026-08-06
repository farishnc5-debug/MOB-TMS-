package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedShipmentAiResult(
    val originHub: String = "Jeddah",
    val truckType: String = "4-Ton Reefer Chilled",
    val destination: String = "Riyadh",
    val clientName: String = "",
    val extraDrops: Int = 0,
    val waitingHours: Int = 2,
    val rawText: String = ""
)

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(apiKeyOverride: String, systemInstruction: String, prompt: String): String = withContext(Dispatchers.IO) {
        val key = apiKeyOverride.ifBlank {
            try {
                BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }
        }
        if (key.isBlank()) {
            return@withContext "Gemini API key is not configured. Please enter your API key in Setup / Settings."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$key"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            if (systemInstruction.isNotBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
            }
        }

        try {
            val httpRequest = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(httpRequest).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Gemini API Error (${response.code}): $bodyString"
            }

            val responseObj = JSONObject(bodyString)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                }
            }
            "No output received from Gemini AI."
        } catch (e: Exception) {
            "Error calling Gemini API: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    suspend fun parseShipmentPrompt(apiKeyOverride: String, userPrompt: String): ParsedShipmentAiResult = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are a logistics shipment parser for Yalla Muv Company in Saudi Arabia.
            Extract shipment parameters from the prompt and return strictly a JSON object with:
            - "originHub": string ("Jeddah", "Riyadh", or "Dammam")
            - "truckType": string (e.g. "4-Ton Reefer Chilled", "4-Ton Reefer Freezer", "4-Ton Open Top", "40ft Flatbed")
            - "destination": string (e.g. "Riyadh", "Mecca", "Dammam", "Madinah")
            - "clientName": string
            - "extraDrops": integer
            - "waitingHours": integer
            Do not include Markdown formatting or code block backticks.
        """.trimIndent()

        val rawResponse = generateContent(apiKeyOverride, systemPrompt, userPrompt)

        try {
            val cleanJson = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)
            ParsedShipmentAiResult(
                originHub = json.optString("originHub", "Jeddah"),
                truckType = json.optString("truckType", "4-Ton Reefer Chilled"),
                destination = json.optString("destination", "Riyadh"),
                clientName = json.optString("clientName", ""),
                extraDrops = json.optInt("extraDrops", 0),
                waitingHours = json.optInt("waitingHours", 2),
                rawText = rawResponse
            )
        } catch (e: Exception) {
            ParsedShipmentAiResult(rawText = rawResponse)
        }
    }
}
