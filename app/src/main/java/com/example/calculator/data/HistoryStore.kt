package com.example.calculator.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.calculator.engine.AngleMode
import com.example.calculator.vm.HistoryEntry
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.historyDataStore by preferencesDataStore(name = "calculator_prefs")

data class HistoryState(
    val history: List<HistoryEntry> = emptyList(),
    val angleMode: AngleMode = AngleMode.DEG
)

class HistoryStore(private val context: Context) {

    val data: Flow<HistoryState> = context.historyDataStore.data
        .map { prefs ->
            val mode = prefs[KEY_ANGLE_MODE]?.let { AngleMode.valueOf(it) } ?: AngleMode.DEG
            val historyRaw = prefs[KEY_HISTORY].orEmpty()
            val history = decodeHistory(historyRaw)
            HistoryState(history = history, angleMode = mode)
        }
        .catch {
            emit(HistoryState())
        }

    suspend fun save(history: List<HistoryEntry>, angleMode: AngleMode) {
        context.historyDataStore.edit { prefs ->
            prefs[KEY_HISTORY] = encodeHistory(history)
            prefs[KEY_ANGLE_MODE] = angleMode.name
        }
    }

    suspend fun clear(angleMode: AngleMode) {
        save(emptyList(), angleMode)
    }
}

private val KEY_HISTORY = stringPreferencesKey("history_entries")
private val KEY_ANGLE_MODE = stringPreferencesKey("angle_mode")

private fun encodeHistory(history: List<HistoryEntry>): String =
    history.joinToString("\n") { encodeEntry(it) }

private fun decodeHistory(raw: String): List<HistoryEntry> =
    raw.lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { decodeEntry(it) }
        .toList()

private fun encodeEntry(entry: HistoryEntry): String {
    val expression = encode(entry.expression)
    val result = encode(entry.result ?: "")
    val timestamp = encode(entry.timestamp ?: "")
    return listOf(expression, result, timestamp).joinToString("|")
}

private fun decodeEntry(line: String): HistoryEntry? {
    val parts = line.split("|")
    if (parts.size != 3) return null
    val expression = decode(parts[0])
    val result = decode(parts[1]).ifBlank { null }
    val timestamp = decode(parts[2]).ifBlank { null }
    return HistoryEntry(
        expression = expression,
        result = result,
        timestamp = timestamp
    )
}

private fun encode(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

private fun decode(value: String): String =
    URLDecoder.decode(value, StandardCharsets.UTF_8.toString())

