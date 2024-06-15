package co.anbora.labs.todo.nodes

import co.anbora.labs.todo.SmartTodoItemPointer
import co.anbora.labs.todo.TodoTreeBuilder
import com.intellij.openapi.project.Project

data class TodoExportItemNode(
    val myProject: Project,
    val smartTodoItemPointer: SmartTodoItemPointer,
    val builder: TodoTreeBuilder
): TodoItemNode(myProject, smartTodoItemPointer, builder) {

    override fun toString(): String {
        val todoItemPointer = value!!
        update(presentation)
        val info = todoItemPointer.todoItem
        val path = info.file.virtualFile.toNioPath()
        val details = presentation.presentableText
        val fileName = info.file.name
        return String.format("%s %s %s", path, details, fileName)
    }
}