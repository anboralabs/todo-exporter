package co.anbora.labs.todo.nodes

import com.intellij.openapi.editor.Document

data class TodoInfo(
    val document: Document,
    val chars: CharSequence,
    val startOffset: Int,
    val endOffset: Int,
    val lineStartOffset: Int,
    val lineEndOffset: Int,
    val lineColumnPrefix: String,
    val newName: String
)
