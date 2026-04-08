package by.mrflaxe.arbor.core.message;

import java.util.Map;

public class DefaultMessageProvider implements MessageProvider {

    private static final Map<String, String> DEFAULTS = Map.of(
            CoreMessageKey.COMMAND_ERROR.path(), "Error: {detail}\nUsage: {usage}"
    );

    @Override
    public String get(MessageKey key) {
        return DEFAULTS.getOrDefault(key.path(), "");
    }
}
