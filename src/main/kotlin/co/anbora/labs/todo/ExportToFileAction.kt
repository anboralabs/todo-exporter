package co.anbora.labs.todo

import co.anbora.labs.todo.exporter.ExporterTodoToString
import co.anbora.labs.todo.ide.util.ExportToFileUtil
import co.anbora.labs.todo.nodes.TodoExportItemNode
import com.intellij.icons.AllIcons
import com.intellij.ide.ExporterToTextFile
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.PsiTodoSearchHelper
import com.intellij.psi.search.TodoItem
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.function.Supplier

class ExportToFileAction(
    private val currentProject: Project,
    private val supplierBuilder: Supplier<TodoTreeBuilder>
): AnAction(IdeBundle.message("dialog.title.export.to.file"), null, AllIcons.ToolbarDecorator.Export) {

    private val mySearchHelper: PsiTodoSearchHelper = PsiTodoSearchHelper.getInstance(currentProject)

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        val project = CommonDataKeys.PROJECT.getData(dataContext)

        var toPrint = ""

        ProgressManager.getInstance().run(object : Task.Backgroundable(currentProject, "Collecting..."){
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                ReadAction.nonBlocking<String> {
                    mySearchHelper.processFilesWithTodoItems {
                        toPrint += mySearchHelper.findTodoItems(it).asIterable()
                            .joinToString(separator = "\n") { todoItem: TodoItem ->
                                val document = PsiDocumentManager.getInstance(currentProject).getDocument(todoItem.file)
                                val pointer = SmartTodoItemPointer(todoItem, document!!)
                                TodoExportItemNode(currentProject, pointer, supplierBuilder.get()).toString()
                            }
                        true
                    }
                    toPrint
                }.finishOnUiThread(ModalityState.nonModal()) {
                    val exporterToTextFile: ExporterToTextFile = ExporterTodoToString(it)
                    if (project != null && exporterToTextFile.canExport()) {
                        ExportToFileUtil.chooseFileAndExport(project, exporterToTextFile)
                    }
                }.submit(AppExecutorUtil.getAppExecutorService())
            }
        })
    }
}
