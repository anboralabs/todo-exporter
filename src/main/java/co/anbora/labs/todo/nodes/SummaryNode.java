// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by
// the Apache 2.0 license that can be found in the LICENSE file.

package co.anbora.labs.todo.nodes;

import co.anbora.labs.todo.CurrentFileTodosTreeBuilder;
import co.anbora.labs.todo.ToDoSummary;
import co.anbora.labs.todo.TodoFileDirAndModuleComparator;
import co.anbora.labs.todo.TodoTreeBuilder;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Queryable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SummaryNode extends BaseToDoNode<ToDoSummary> {
  public SummaryNode(Project project, @NotNull ToDoSummary value,
                     TodoTreeBuilder builder) {
    super(project, value, builder);
  }

  @Override
  @NotNull
  public Collection<AbstractTreeNode<?>> getChildren() {
    ArrayList<AbstractTreeNode<?>> children = new ArrayList<>();

    final ProjectFileIndex projectFileIndex =
        ProjectRootManager.getInstance(getProject()).getFileIndex();
    if (myToDoSettings.isModulesShown()) {
      for (Iterator i = myBuilder.getAllFiles(); i.hasNext();) {
        final PsiFile psiFile = (PsiFile)i.next();
        if (psiFile == null) { // skip invalid PSI files
          continue;
        }
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        createModuleTodoNodeForFile(children, projectFileIndex, virtualFile);
      }
    } else {
      if (myToDoSettings.getIsPackagesShown()) {
        if (myBuilder instanceof CurrentFileTodosTreeBuilder) {
          final Iterator allFiles = myBuilder.getAllFiles();
          if (allFiles.hasNext()) {
            children.add(new TodoFileNode(myProject, (PsiFile)allFiles.next(),
                                          myBuilder, false));
          }
        } else {
          TodoTreeHelper.getInstance(getProject())
              .addPackagesToChildren(children, null, myBuilder);
        }
      } else {
        for (Iterator i = myBuilder.getAllFiles(); i.hasNext();) {
          final PsiFile psiFile = (PsiFile)i.next();
          if (psiFile == null) { // skip invalid PSI files
            continue;
          }
          TodoFileNode fileNode =
              new TodoFileNode(getProject(), psiFile, myBuilder, false);
          if (getTreeStructure().accept(psiFile) &&
              !children.contains(fileNode)) {
            children.add(fileNode);
          }
        }
      }
    }
    children.sort(TodoFileDirAndModuleComparator.INSTANCE);
    return children;
  }

  protected void
  createModuleTodoNodeForFile(ArrayList<? super AbstractTreeNode<?>> children,
                              ProjectFileIndex projectFileIndex,
                              VirtualFile virtualFile) {
    Module module = projectFileIndex.getModuleForFile(virtualFile);
    if (module != null) {
      ModuleToDoNode moduleToDoNode =
          new ModuleToDoNode(getProject(), module, myBuilder);
      if (!children.contains(moduleToDoNode)) {
        children.add(moduleToDoNode);
      }
    }
  }

  @Override
  public void update(@NotNull PresentationData presentation) {
    int todoItemCount = getTodoItemCount(getValue());
    int fileCount = getFileCount(getValue());
    presentation.setPresentableText(
        IdeBundle.message("node.todo.summary", todoItemCount, fileCount));
  }

  @Override
  public @Nullable String toTestString(Queryable.
                                       @Nullable PrintInfo printInfo) {
    return "Summary";
  }

  @Override
  public int getFileCount(ToDoSummary summary) {
    int count = 0;
    for (Iterator i = myBuilder.getAllFiles(); i.hasNext();) {
      PsiFile psiFile = (PsiFile)i.next();
      if (psiFile == null) { // skip invalid PSI files
        continue;
      }
      if (getTreeStructure().accept(psiFile)) {
        count++;
      }
    }
    return count;
  }

  @Override
  public int getTodoItemCount(final ToDoSummary val) {
    int count = 0;
    for (final Iterator<PsiFile> i = myBuilder.getAllFiles(); i.hasNext();) {
      count += ReadAction.compute(
          () -> getTreeStructure().getTodoItemCount(i.next()));
    }
    return count;
  }

  @Override
  public int getWeight() {
    return 0;
  }
}
