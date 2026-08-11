package com.example.tracker.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Remembers which tracking links *this device* generated as a sender, mirroring the web
 * dashboard's use of localStorage. There are no user accounts, so this list is local-only -
 * generating links from a different device gives you a separate list.
 */
data class LocalSessionEntry(val id: String, val label: String, val createdAtMillis: Long)

class LocalSessionsStore(context: Context) {
    private val prefs = context.getSharedPreferences("tracking_sessions_local", Context.MODE_PRIVATE)

    fun list(): List<LocalSessionEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            LocalSessionEntry(obj.getString("id"), obj.getString("label"), obj.getLong("createdAt"))
        }
    }

    fun add(entry: LocalSessionEntry) {
        val current = list().toMutableList()
        current.add(0, entry)
        save(current)
    }

    fun remove(id: String) {
        save(list().filter { it.id != id })
    }

    private fun save(entries: List<LocalSessionEntry>) {
        val array = JSONArray()
        for (e in entries) {
            array.put(
                JSONObject()
                    .put("id", e.id)
                    .put("label", e.label)
                    .put("createdAt", e.createdAtMillis)
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val KEY = "sessions"
    }
}
