package by.mrflaxe.arbor.platform.bukkit.message;

import by.mrflaxe.arbor.core.message.CoreMessageKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YamlMessageProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsMessagesFromFlatYamlFile() throws IOException {
        File file = tempDir.resolve("messages.yml").toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("core:\n  command-error: \"Custom error: {detail}\"\n");
        }

        YamlMessageProvider provider = YamlMessageProvider.fromFile(file);

        assertThat(provider.get(CoreMessageKey.COMMAND_ERROR)).isEqualTo("Custom error: {detail}");
    }

    @Test
    void returnsEmptyStringForMissingKey() {
        YamlMessageProvider provider = new YamlMessageProvider(Map.of());

        assertThat(provider.get(CoreMessageKey.COMMAND_ERROR)).isEmpty();
    }
}
