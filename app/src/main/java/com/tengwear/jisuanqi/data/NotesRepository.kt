package com.tengwear.jisuanqi.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class NotesRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAll(): List<Note> {
        val raw = prefs.getString(KEY_NOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = ArrayList<Note>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Note(
                        id = o.getLong("id"),
                        content = o.getString("content"),
                        updatedAt = o.getLong("updatedAt")
                    )
                )
            }
            list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(notes: List<Note>) {
        val arr = JSONArray()
        for (n in notes) {
            arr.put(JSONObject().apply {
                put("id", n.id)
                put("content", n.content)
                put("updatedAt", n.updatedAt)
            })
        }
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "calc_notes"
        private const val KEY_NOTES = "notes"
    }
}