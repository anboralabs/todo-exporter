package co.anbora.labs.todo.ide;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class IdeBundle {
    public static final String BUNDLE = "messages.IdeBundle";

    private static final DynamicBundle INSTANCE = new DynamicBundle(IdeBundle.class, BUNDLE);

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.containsKey(key) ? INSTANCE.getMessage(key, params) : IdeDeprecatedMessagesBundle.message(key, params);
    }

    public static Supplier<String> messagePointer(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.containsKey(key) ? INSTANCE.getLazyMessage(key, params) : IdeDeprecatedMessagesBundle.messagePointer(key, params);
    }

    private IdeBundle() {
    }
}
