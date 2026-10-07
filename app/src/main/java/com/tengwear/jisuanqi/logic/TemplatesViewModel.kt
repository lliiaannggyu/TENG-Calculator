package com.tengwear.jisuanqi.logic

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tengwear.jisuanqi.data.FormulaTemplate
import com.tengwear.jisuanqi.data.TemplatesRepository
import org.json.JSONArray
import org.json.JSONObject

enum class TemplateSortMode(val id: String, val label: String) {
    MANUAL("manual", "手动"),
    NAME("name", "按名称"),
    CREATED("created", "按时间");

    companion object {
        fun fromId(id: String): TemplateSortMode =
            values().firstOrNull { it.id == id } ?: MANUAL
    }
}

class TemplatesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TemplatesRepository(application)

    var templates by mutableStateOf(repo.loadAll())
        private set

    var sortMode by mutableStateOf(TemplateSortMode.fromId(repo.sortMode))
        private set

    /** 当前展示顺序（根据 sortMode 计算）。 */
    val displayed: List<FormulaTemplate>
        get() = when (sortMode) {
            TemplateSortMode.MANUAL -> templates
            TemplateSortMode.NAME -> templates.sortedWith(
                compareBy({ it.name.lowercase() }, { it.createdAt })
            )
            TemplateSortMode.CREATED -> templates.sortedByDescending { it.createdAt }
        }

    fun findById(id: Long): FormulaTemplate? = templates.firstOrNull { it.id == id }

    fun add(name: String, formula: String): Long {
        val now = System.currentTimeMillis()
        val id = if (templates.any { it.id == now }) now + 1 else now
        val t = FormulaTemplate(id = id, name = name, formula = formula, createdAt = id)
        templates = templates + t
        persist()
        return id
    }

    fun update(id: Long, name: String, formula: String) {
        templates = templates.map {
            if (it.id == id) it.copy(name = name, formula = formula) else it
        }
        persist()
    }

    fun delete(ids: Set<Long>) {
        if (ids.isEmpty()) return
        templates = templates.filterNot { it.id in ids }
        persist()
    }

    fun moveUp(index: Int) {
        if (index <= 0 || index >= templates.size) return
        val list = templates.toMutableList()
        val item = list.removeAt(index)
        list.add(index - 1, item)
        templates = list
        persist()
    }

    fun moveDown(index: Int) {
        if (index < 0 || index >= templates.size - 1) return
        val list = templates.toMutableList()
        val item = list.removeAt(index)
        list.add(index + 1, item)
        templates = list
        persist()
    }

    /** 改名后的排序模式切换方法，避免与 var sortMode 的自动 setter 冲突。 */
    fun changeSortMode(mode: TemplateSortMode) {
        sortMode = mode
        repo.sortMode = mode.id
    }

    /** 返回成功导入数量，-1 表示 JSON 解析失败。 */
    fun importJson(json: String): Int {
        val parsed = parseJson(json) ?: return -1
        if (parsed.isEmpty()) return 0
        val existingIds = templates.map { it.id }.toMutableSet()
        val added = mutableListOf<FormulaTemplate>()
        for (t in parsed) {
            var newId = t.id
            while (newId in existingIds) newId++
            existingIds.add(newId)
            added.add(t.copy(id = newId))
        }
        templates = templates + added
        persist()
        return added.size
    }

    fun exportJson(ids: Set<Long>): String {
        val list = templates.filter { it.id in ids }
        return buildJson(list)
    }

    private fun persist() = repo.saveAll(templates)

    companion object {
        fun parseJson(json: String): List<FormulaTemplate>? {
            return try {
                val arr = JSONArray(json.trim())
                val list = ArrayList<FormulaTemplate>(arr.length())
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        FormulaTemplate(
                            id = o.optLong("id", System.currentTimeMillis() + i),
                            name = o.getString("name"),
                            formula = o.getString("formula"),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis() + i)
                        )
                    )
                }
                list
            } catch (e: Exception) {
                null
            }
        }

        private fun buildJson(list: List<FormulaTemplate>): String {
            val arr = JSONArray()
            for (t in list) {
                arr.put(JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("formula", t.formula)
                    put("createdAt", t.createdAt)
                })
            }
            return arr.toString()
        }
    }
}