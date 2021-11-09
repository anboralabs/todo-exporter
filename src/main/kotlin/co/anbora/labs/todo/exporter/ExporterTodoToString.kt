package co.anbora.labs.todo.exporter

import com.intellij.ide.ExporterToTextFile

private const val FILE_PATH = "todo.txt"

class ExporterTodoToString(private val toExport: String?): ExporterToTextFile {
    override fun getReportText(): String = toExport.orEmpty()

    override fun getDefaultFilePath(): String = FILE_PATH

    override fun canExport(): Boolean = toExport != null
}