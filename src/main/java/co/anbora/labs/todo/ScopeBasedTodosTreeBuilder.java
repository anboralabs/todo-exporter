// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source
// code is governed by the Apache 2.0 license.

package co.anbora.labs.todo;

import com.intellij.ide.util.scopeChooser.ScopeChooserCombo;
import com.intellij.openapi.project.Project;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;

public final class ScopeBasedTodosTreeBuilder extends TodoTreeBuilder {

  private final @NotNull ScopeChooserCombo myScopes;

  public ScopeBasedTodosTreeBuilder(@NotNull JTree tree,
                                    @NotNull Project project,
                                    @NotNull ScopeChooserCombo scopes) {
    super(tree, project);
    myScopes = scopes;
  }

  @Override
  protected @NotNull TodoTreeStructure createTreeStructure() {
    return new ScopeBasedTodosTreeStructure(myProject, myScopes);
  }
}
