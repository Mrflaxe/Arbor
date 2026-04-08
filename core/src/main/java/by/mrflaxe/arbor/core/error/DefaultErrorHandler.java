package by.mrflaxe.arbor.core.error;

import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.message.CoreMessageKey;
import by.mrflaxe.arbor.core.message.MessageProvider;
import by.mrflaxe.arbor.core.message.MessageSender;

public class DefaultErrorHandler<S> implements ErrorHandler<S> {

    private final MessageProvider messageProvider;
    private final MessageSender<S> messageSender;

    public DefaultErrorHandler(MessageProvider messageProvider, MessageSender<S> messageSender) {
        this.messageProvider = messageProvider;
        this.messageSender = messageSender;
    }

    @Override
    public void onCommandError(S source, String input, int cursor, String detail, String usage) {
        String template = messageProvider.get(CoreMessageKey.COMMAND_ERROR);
        String cursorMarker = cursor >= 0 ? " ".repeat(cursor) + "^^^" : "";
        String message = template
                .replace("{detail}", detail)
                .replace("{usage}", usage)
                .replace("{input}", input != null ? input : "")
                .replace("{cursor}", cursorMarker);
        messageSender.send(source, message);
    }

    @Override
    public void onExecutionFailure(S source, ExecutionResult result) {
        String key = result.getMessageKey();
        if (key != null) {
            messageSender.send(source, key);
        }
    }
}
