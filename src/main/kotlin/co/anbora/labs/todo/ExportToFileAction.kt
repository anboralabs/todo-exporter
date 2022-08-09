package co.anbora.labs.todo

import co.anbora.labs.todo.exporter.ExporterTodoToString
import co.anbora.labs.todo.ide.util.ExportToFileUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.ide.IdeBundle
import com.intellij.icons.AllIcons
import com.intellij.psi.search.PsiTodoSearchHelper
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.search.TodoItem
import com.intellij.psi.PsiDocumentManager
import co.anbora.labs.todo.nodes.TodoExportItemNode
import com.intellij.ide.ExporterToTextFile
import com.intellij.openapi.project.Project
import java.util.function.Supplier

class ExportToFileAction(
    private val myProject: Project,
    private val supplierBuilder: Supplier<TodoTreeBuilder>
): AnAction(IdeBundle.message("dialog.title.export.to.file"), null, AllIcons.ToolbarDecorator.Export) {

    private val mySearchHelper: PsiTodoSearchHelper = PsiTodoSearchHelper.getInstance(myProject)

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext)

        val toPrint = mySearchHelper.findFilesWithTodoItems()
            .flatMap { mySearchHelper.findTodoItems(it).asIterable() }
            .joinToString(separator = "\n") { todoItem: TodoItem ->
                val document = PsiDocumentManager.getInstance(myProject).getDocument(todoItem.file)
                val pointer = SmartTodoItemPointer(todoItem, document!!)
                TodoExportItemNode(myProject, pointer, supplierBuilder.get()).toString()
            }

        val exporterToTextFile: ExporterToTextFile = ExporterTodoToString(toPrint)
        if (project == null) return
        if (!exporterToTextFile.canExport()) return
        ExportToFileUtil.chooseFileAndExport(project, exporterToTextFile)
    }
}
