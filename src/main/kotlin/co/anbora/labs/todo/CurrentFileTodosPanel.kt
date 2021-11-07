package co.anbora.labs.todo

import com.intellij.ide.todo.TodoPanelSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.content.Content
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.concurrency.runAsync


abstract class CurrentFileTodosPanel(
    project: Project,
    settings: TodoPanelSettings,
    content: Content
): TodoPanel(project, settings, true, content) {

    init {
        val files = FileEditorManager.getInstance(project).selectedFiles
        //runBlocking {
        //    setFile(if (files.isEmpty()) null else PsiManager.getInstance(myProject).findFile(files[0]), true)
        //}
        // It's important to remove this listener. It prevents invocation of setFile method after the tree builder is disposed
        // It's important to remove this listener. It prevents invocation of setFile method after the tree builder is disposed
        project.messageBus.connect(this)
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
                override fun selectionChanged(e: FileEditorManagerEvent) {
                    val file = e.newFile
                    val psiFile =
                        if (file != null && file.isValid) PsiManager.getInstance(myProject).findFile(file) else null
                    // This invokeLater is required. The problem is setFile does a commit to PSI, but setFile is
                    // invoked inside PSI change event. It causes an Exception like "Changes to PSI are not allowed inside event processing"
                    ApplicationManager.getApplication().invokeLater { setFile(psiFile, false) }
                }
            })
    }

    private fun setFile(file: PsiFile?, initialUpdate: Boolean) {
        // setFile method is invoked in LaterInvocator so PsiManager
        // can be already disposed, so we need to check this before using it.
        if (myProject == null || PsiManager.getInstance(myProject).isDisposed) {
            return
        }
        if (file != null && selectedFile === file) return
        val builder = myTodoTreeBuilder as CurrentFileTodosTreeBuilder
        builder.setFile(file)
        if (myTodoTreeBuilder.isUpdatable() || initialUpdate) {
            val selectableElement = builder.todoTreeStructure.firstSelectableElement
            if (selectableElement != null) {
                builder.select(selectableElement)
            }
        }
    }
}