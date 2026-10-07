package com.tengwear.jisuanqi.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TemplatesRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAll(): List<FormulaTemplate> {
        val raw = prefs.getString(KEY_TEMPLATES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = ArrayList<FormulaTemplate>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    FormulaTemplate(
                        id = o.getLong("id"),
                        name = o.getString("name"),
                        formula = o.getString("formula"),
                        createdAt = o.optLong("createdAt", 0L)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(list: List<FormulaTemplate>) {
        val arr = JSONArray()
        for (t in list) {
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("formula", t.formula)
                put("createdAt", t.createdAt)
            })
        }
        prefs.edit().putString(KEY_TEMPLATES, arr.toString()).apply()
    }

    var sortMode: String
        get() = prefs.getString(KEY_SORT_MODE, "manual") ?: "manual"
        set(value) { prefs.edit().putString(KEY_SORT_MODE, value).apply() }

    companion object {
        private const val PREFS_NAME = "calc_templates"
        private const val KEY_TEMPLATES = "templates"
        private const val KEY_SORT_MODE = "sort_mode"
    }
}