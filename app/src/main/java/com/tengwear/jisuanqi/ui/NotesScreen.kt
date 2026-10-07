@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.data.Note
import com.tengwear.jisuanqi.logic.NotesViewModel
import com.tengwear.jisuanqi.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class NotesMode { LIST, EDIT }

@Composable
fun NotesScreen(
    onDismiss: () -> Unit,
    viewModel: NotesViewModel = viewModel()
) {
    var mode by remember { mutableStateOf(NotesMode.LIST) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var editingText by remember { mutableStateOf("") }

    val notes = viewModel.notes

    /** 保存当前编辑内容（空则删除），并回到列表。 */
    fun commitAndClose() {
        val trimmed = editingText.trim()
        val id = editingId
        if (trimmed.isEmpty()) {
            if (id != null) viewModel.delete(id)
        } else {
            if (id == null) viewModel.add(trimmed)
            else viewModel.update(id, trimmed)
        }
        editingId = null
        editingText = ""
        mode = NotesMode.LIST
    }

    BackHandler(enabled = true) {
        if (mode == NotesMode.EDIT) commitAndClose() else onDismiss()
    }

    AppScaffold {
        when (mode) {
            NotesMode.LIST -> NotesListPage(
                notes = notes,
                onCreate = {
                    editingId = null
                    editingText = ""
                    mode = NotesMode.EDIT
                },
                onEdit = { note ->
                    editingId = note.id
                    editingText = note.content
                    mode = NotesMode.EDIT
                },
                onDismiss = onDismiss
            )

            NotesMode.EDIT -> NotesEditPage(
                text = editingText,
                isNew = editingId == null,
                onTextChange = { editingText = it },
                onSave = { commitAndClose() },
                onDelete = {
                    editingId?.let { viewModel.delete(it) }
                    editingId = null
                    editingText = ""
                    mode = NotesMode.LIST
                },
                onCancel = {
                    // 丢弃本次修改
                    editingId = null
                    editingText = ""
                    mode = NotesMode.LIST
                }
            )
        }
    }
}

// ============================ 列表页 ============================

@Composable
private fun NotesListPage(
    notes: List<Note>,
    onCreate: () -> Unit,
    onEdit: (Note) -> Unit,
    onDismiss: () -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()

    ScreenScaffold(
        scrollState = columnState,
        timeText = { TimeText() }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "笔记",
                    color = md_primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 4.dp)
                )
            }

            // 新建
            item {
                ButtonRow(
                    label = "＋ 新建笔记",
                    container = calcEqualsBg,
                    content = calcEqualsText,
                    modifier = Modifier.transformedHeight(this, spec)
                ) { onCreate() }
            }

            if (notes.isEmpty()) {
                item {
                    Text(
                        text = "还没有笔记\n点上方新建一条",
                        color = md_onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                            .padding(vertical = 16.dp)
                    )
                }
            } else {
                items(
                    count = notes.size,
                    key = { index -> notes[index].id }
                ) { index ->
                    val note = notes[index]
                    NoteCard(
                        note = note,
                        modifier = Modifier.transformedHeight(this, spec),
                        onClick = { onEdit(note) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
            }

            item {
                ButtonRow(
                    label = "返回",
                    container = calcFunctionBg,
                    content = calcFunctionText,
                    modifier = Modifier.transformedHeight(this, spec)
                ) { onDismiss() }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val title = remember(note.content) { note.content.lineSequence().first().take(24) }
    val preview = remember(note.content) {
        val lines = note.content.trim().split('\n')
        val body = if (lines.size > 1) lines.drop(1).joinToString(" ") else ""
        body.replace('\n', ' ').take(40)
    }
    val timeText = remember(note.updatedAt) {
        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            .format(Date(note.updatedAt))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(calcFunctionBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (title.isBlank()) "（无标题）" else title,
            color = calcFunctionText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (preview.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = preview,
                color = calcFunctionText.copy(alpha = 0.65f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = timeText,
            color = md_onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ButtonRow(
    label: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = content,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================ 编辑页 ============================

@Composable
private fun NotesEditPage(
    text: String,
    isNew: Boolean,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // 进入编辑页后自动聚焦，唤起系统键盘
    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isNew) "新建笔记" else "编辑笔记",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            // 文本输入区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(calcFunctionBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "输入内容…",
                        color = md_onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = calcDisplayText,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(md_primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isNew) {
                    NoteCircleButton(
                        label = "删除",
                        background = md_error.copy(alpha = 0.22f),
                        foreground = md_error
                    ) { onDelete() }
                }
                NoteCircleButton(
                    label = "返回",
                    background = calcFunctionBg,
                    foreground = calcFunctionText
                ) { onCancel() }
                NoteCircleButton(
                    label = "保存",
                    background = calcEqualsBg,
                    foreground = calcEqualsText
                ) { onSave() }
            }
        }
    }
}

@Composable
private fun NoteCircleButton(
    label: String,
    background: androidx.compose.ui.graphics.Color,
    foreground: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = foreground,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}