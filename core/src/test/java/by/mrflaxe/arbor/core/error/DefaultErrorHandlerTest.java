package by.mrflaxe.arbor.core.error;

import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.message.DefaultMessageProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultErrorHandlerTest {

    private final List<String> sent = new ArrayList<>();
    private final DefaultErrorHandler<String> handler = new DefaultErrorHandler<>(
            new DefaultMessageProvider(),
            (source, message) -> sent.add(message)
    );

    @Test
    void rendersDetailAndUsageFromTemplate() {
        handler.onCommandError("source", "greet xyz", 6, "unexpected argument", "/greet\n/greet <player>");

        assertThat(sent).hasSize(1);
        assertThat(sent.get(0)).contains("unexpected argument").contains("/greet");
    }

    @Test
    void rendersCursorMarkerAtCorrectPosition() {
        DefaultErrorHandler<String> cursorHandler = new DefaultErrorHandler<>(
                key -> "{input}\n{cursor}",
                (source, message) -> sent.add(message)
        );

        cursorHandler.onCommandError("source", "greet xyz", 6, "unexpected argument", "/greet");

        String message = sent.get(0);
        assertThat(message).contains("      ^^^");
    }

    @Test
    void delegatesExecutionFailureToMessageSender() {
        handler.onExecutionFailure("source", ExecutionResult.failure("no-permission"));

        assertThat(sent).containsExactly("no-permission");
    }

    @Test
    void doesNotSendWhenExecutionFailureHasNullKey() {
        handler.onExecutionFailure("source", ExecutionResult.failure(null));

        assertThat(sent).isEmpty();
    }
}
