package by.mrflaxe.arbor.core.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMessageProviderTest {

    private final DefaultMessageProvider provider = new DefaultMessageProvider();

    @Test
    void returnsHardcodedTemplateForCommandError() {
        String template = provider.get(CoreMessageKey.COMMAND_ERROR);
        assertThat(template).contains("{detail}").contains("{usage}");
    }

    @Test
    void returnsEmptyStringForUnknownKey() {
        MessageKey unknown = () -> "does.not.exist";
        assertThat(provider.get(unknown)).isEmpty();
    }
}
