package com.tengwear.jisuanqi.logic

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tengwear.jisuanqi.data.Note
import com.tengwear.jisuanqi.data.NotesRepository

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NotesRepository(application)

    var notes by mutableStateOf(repo.loadAll())
        private set

    /** 新增一条笔记，返回新 id。 */
    fun add(content: String): Long {
        val now = System.currentTimeMillis()
        // 极短时间内连续新增时避免 id 冲突
        val id = if (notes.any { it.id == now }) now + 1 else now
        val note = Note(id = id, content = content, updatedAt = id)
        notes = (listOf(note) + notes).sortedByDescending { it.updatedAt }
        persist()
        return id
    }

    fun update(id: Long, content: String) {
        val now = System.currentTimeMillis()
        notes = notes.map {
            if (it.id == id) it.copy(content = content, updatedAt = now) else it
        }.sortedByDescending { it.updatedAt }
        persist()
    }

    fun delete(id: Long) {
        notes = notes.filterNot { it.id == id }
        persist()
    }

    private fun persist() = repo.saveAll(notes)
}