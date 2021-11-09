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
        val myRangeMarker = todoItemPointer.rangeMarker
        val info = getNodeInfo(todoItemPointer, myRangeMarker)
        return String.format("%s %s", info.document,info.newName)
    }
}