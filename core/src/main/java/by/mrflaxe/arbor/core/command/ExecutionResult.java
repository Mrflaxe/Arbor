package by.mrflaxe.arbor.core.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExecutionResult {

    private final boolean success;
    private final String messageKey;
    private final Map<String, String> placeholders;

    private ExecutionResult(boolean success, String messageKey, Map<String, String> placeholders) {
        this.success = success;
        this.messageKey = messageKey;
        this.placeholders = Collections.unmodifiableMap(placeholders);
    }

    public static ExecutionResult success() {
        return new ExecutionResult(true, null, Map.of());
    }

    public static ExecutionResult failure(String messageKey) {
        return new ExecutionResult(false, messageKey, new LinkedHashMap<>());
    }

    public ExecutionResult with(String placeholder, String value) {
        Map<String, String> updated = new LinkedHashMap<>(placeholders);
        updated.put(placeholder, value);
        return new ExecutionResult(false, messageKey, updated);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }
}
