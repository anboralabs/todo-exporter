package co.anbora.labs.todo;

import co.anbora.labs.todo.nodes.TodoItemNode;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiTodoSearchHelper;
import com.intellij.psi.search.TodoItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ExportToFileAction extends AnAction {

    private Supplier<TodoTreeBuilder> supplierBuilder;
    protected final Project myProject;
    protected final PsiTodoSearchHelper mySearchHelper;

    public ExportToFileAction(final Project project, Supplier<TodoTreeBuilder> supplierBuilder) {
        super(IdeBundle.message("dialog.title.export.to.file"), null, AllIcons.ToolbarDecorator.Export);
        this.supplierBuilder = supplierBuilder;
        this.myProject = project;
        mySearchHelper = PsiTodoSearchHelper.SERVICE.getInstance(project);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile[] files = mySearchHelper.findFilesWithTodoItems();
        List<TodoItemNode> children = new ArrayList<>();
        for (PsiFile file : files) {
            final Document document = PsiDocumentManager.getInstance(myProject).getDocument(file);
            for (final TodoItem todoItem : mySearchHelper.findTodoItems(file)) {
                if (todoItem.getTextRange().getEndOffset() < document.getTextLength() + 1) {
                    final SmartTodoItemPointer pointer = new SmartTodoItemPointer(todoItem, document);
                    TodoItemNode todoItemNode = new TodoItemNode(myProject, pointer, this.supplierBuilder.get());
                    todoItemNode.update(new PresentationData());
                    children.add(todoItemNode);
                 }
            }
        }
        System.out.println(children);
    }

}
