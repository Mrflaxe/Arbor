package by.mrflaxe.arbor.platform.bukkit.message;

import by.mrflaxe.arbor.core.message.MessageKey;
import by.mrflaxe.arbor.core.message.MessageProvider;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlMessageProvider implements MessageProvider {

    private final Map<String, String> messages;

    public YamlMessageProvider(Map<String, String> messages) {
        this.messages = messages;
    }

    public static YamlMessageProvider fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, String> flat = new LinkedHashMap<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                flat.put(key, config.getString(key, ""));
            }
        }
        return new YamlMessageProvider(flat);
    }

    @Override
    public String get(MessageKey key) {
        return messages.getOrDefault(key.path(), "");
    }
}
