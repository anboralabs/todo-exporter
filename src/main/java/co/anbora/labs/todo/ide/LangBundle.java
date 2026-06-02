// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package co.anbora.labs.todo.ide;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

/**
 * Internal. Plugins may not reuse messages of the platform as they are not considered API.
 */
@ApiStatus.Internal
public final class LangBundle {
  public static final @NonNls String BUNDLE = "messages.LangBundle";
  private static final DynamicBundle INSTANCE = new DynamicBundle(LangBundle.class, BUNDLE);

  private LangBundle() {
  }

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    if (INSTANCE.containsKey(key)) {
      return INSTANCE.getMessage(key, params);
    }
    return IdeDeprecatedMessagesBundle.message(key, params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    if (INSTANCE.containsKey(key)) {
      return INSTANCE.getLazyMessage(key, params);
    }
    return IdeDeprecatedMessagesBundle.messagePointer(key, params);
  }
}