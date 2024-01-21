package co.anbora.labs.todo.nodes

import co.anbora.labs.todo.HighlightedRegionProvider
import co.anbora.labs.todo.SmartTodoItemPointer
import co.anbora.labs.todo.TodoTreeBuilder
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Queryable
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.TextRange
import com.intellij.ui.HighlightedRegion
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.text.CharArrayUtil
import java.awt.Font

open class TodoItemNode(
    private val myProject: Project,
    private val value: SmartTodoItemPointer,
    private val builder: TodoTreeBuilder
): BaseToDoNode<SmartTodoItemPointer>(myProject, value, builder), HighlightedRegionProvider {

    private val myHighlightedRegions: MutableList<HighlightedRegion> = ContainerUtil.createConcurrentList()
    private val myAdditionalLines: MutableList<HighlightedRegionProvider> = ContainerUtil.createConcurrentList()

    override fun contains(element: Any?): Boolean {
        return canRepresent(element)
    }

    override fun canRepresent(element: Any?): Boolean {
        val value = getValue()
        val item = value?.todoItem
        return Comparing.equal(item, element)
    }

    override fun getFileCount(`val`: SmartTodoItemPointer?): Int {
        return 1
    }

    override fun getTodoItemCount(`val`: SmartTodoItemPointer?): Int {
        return 1
    }

    override fun getHighlightedRegions(): List<HighlightedRegion> {
        return myHighlightedRegions
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>?> {
        return emptyList<AbstractTreeNode<*>>()
    }

    protected fun getNodeInfo(todoItemPointer: SmartTodoItemPointer, myRangeMarker: RangeMarker): TodoInfo {
        val document = todoItemPointer.document
        val chars = document.charsSequence
        val startOffset = myRangeMarker.startOffset
        val endOffset = myRangeMarker.endOffset
        val lineNumber = document.getLineNumber(startOffset)
        var lineStartOffset = document.getLineStartOffset(lineNumber)
        val columnNumber = startOffset - lineStartOffset

        // skip all white space characters
        while (lineStartOffset < document.textLength && (chars[lineStartOffset] == '\t' || chars[lineStartOffset] == ' ')) {
            lineStartOffset++
        }
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val lineColumnPrefix = "(" + (lineNumber + 1) + ", " + (columnNumber + 1) + ") "
        val highlightedText = chars.subSequence(lineStartOffset, Math.min(lineEndOffset, chars.length)).toString()
        val newName = lineColumnPrefix + highlightedText

        return TodoInfo(
            document, chars, startOffset, endOffset, lineStartOffset, lineEndOffset, lineColumnPrefix, newName
        )
    }

    override fun update(presentation: PresentationData) {
        val todoItemPointer = getValue()!!
        val todoItem = todoItemPointer.todoItem
        val myRangeMarker = todoItemPointer.rangeMarker
        if (!todoItem.file.isValid || !myRangeMarker.isValid || myRangeMarker.startOffset == myRangeMarker.endOffset) {
            myRangeMarker.dispose()
            setValue(null)
            return
        }
        myHighlightedRegions.clear()
        myAdditionalLines.clear()

        // Update name

        val (document, chars, startOffset, endOffset, lineStartOffset, lineEndOffset, lineColumnPrefix, newName) =
            getNodeInfo(todoItemPointer, myRangeMarker)

        // Update icon
        val newIcon = todoItem.pattern?.attributes?.icon

        // Update highlighted regions
        myHighlightedRegions.clear()
        val highlighter = myBuilder.getHighlighter(todoItem.file, document)
        collectHighlights(myHighlightedRegions, highlighter, lineStartOffset, lineEndOffset, lineColumnPrefix.length)
        val attributes = todoItem.pattern?.attributes?.textAttributes
        myHighlightedRegions.add(
            HighlightedRegion(
                lineColumnPrefix.length + startOffset - lineStartOffset,
                lineColumnPrefix.length + endOffset - lineStartOffset,
                attributes
            )
        )

        //
        presentation.presentableText = newName
        presentation.setIcon(newIcon)
        for (additionalMarker in todoItemPointer.additionalRangeMarkers) {
            if (!additionalMarker.isValid) break
            val highlights = ArrayList<HighlightedRegion>()
            val lineNum = document.getLineNumber(additionalMarker.startOffset)
            val lineStart = document.getLineStartOffset(lineNum)
            val lineEnd = document.getLineEndOffset(lineNum)
            val lineStartNonWs = CharArrayUtil.shiftForward(chars, lineStart, " \t")
            if (lineStartNonWs > additionalMarker.startOffset || lineEnd < additionalMarker.endOffset) {
                // can happen for an invalid (obsolete) node, tree implementation can call this method for such a node
                break
            }
            collectHighlights(highlights, highlighter, lineStartNonWs, lineEnd, 0)
            highlights.add(
                HighlightedRegion(
                    additionalMarker.startOffset - lineStartNonWs,
                    additionalMarker.endOffset - lineStartNonWs,
                    attributes
                )
            )
            myAdditionalLines.add(AdditionalTodoLine(document.getText(TextRange(lineStartNonWs, lineEnd)), highlights))
        }
    }

    private fun collectHighlights(
        highlights: MutableList<in HighlightedRegion>, highlighter: EditorHighlighter,
        startOffset: Int, endOffset: Int, highlightOffsetShift: Int
    ) {
        val iterator = highlighter.createIterator(startOffset)
        while (!iterator.atEnd()) {
            val start = Math.max(iterator.start, startOffset)
            val end = Math.min(iterator.end, endOffset)
            if (start >= endOffset) break
            var attributes = iterator.textAttributes
            val fontType = attributes.fontType
            if (fontType and Font.BOLD != 0) { // suppress bold attribute
                attributes = attributes.clone()
                attributes.fontType = fontType and Font.BOLD.inv()
            }
            val region = HighlightedRegion(
                highlightOffsetShift + start - startOffset,
                highlightOffsetShift + end - startOffset,
                attributes
            )
            highlights.add(region)
            iterator.advance()
        }
    }

    override fun toTestString(printInfo: Queryable.PrintInfo?): String? {
        return "Item: " + getValue().todoItem.textRange
    }

    override fun getWeight(): Int {
        return 5
    }

    fun getAdditionalLines(): List<HighlightedRegionProvider?> {
        return myAdditionalLines
    }

    private class AdditionalTodoLine(private val myText: String, private val myHighlights: List<HighlightedRegion>) :
        HighlightedRegionProvider {
        override fun getHighlightedRegions(): Iterable<HighlightedRegion> {
            return myHighlights
        }

        override fun toString(): String {
            return myText
        }
    }

}
