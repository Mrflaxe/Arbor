package by.mrflaxe.arbor.core.command.processor;

import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.error.ErrorHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;

import java.util.List;
import java.util.Map;

public class BrigadierCommandProcessor<S> implements CommandProcessor<S> {

    private final CommandDispatcher<S> dispatcher;
    private final ErrorHandler<S> errorHandler;
    private final Map<String, String> usageMap;

    public BrigadierCommandProcessor(CommandDispatcher<S> dispatcher, ErrorHandler<S> errorHandler, Map<String, String> usageMap) {
        this.dispatcher = dispatcher;
        this.errorHandler = errorHandler;
        this.usageMap = usageMap;
    }

    public BrigadierCommandProcessor(CommandDispatcher<S> dispatcher) {
        this(dispatcher, null, Map.of());
    }

    @Override
    public ExecutionResult processCommand(String input, S source) {
        try {
            dispatcher.execute(input, source);
        } catch (CommandSyntaxException e) {
            if (errorHandler != null) {
                String commandName = input.trim().split("\\s+")[0];
                String usage = usageMap.getOrDefault(commandName, "/" + commandName);
                errorHandler.onCommandError(source, e.getInput(), e.getCursor(), e.getRawMessage().getString(), usage);
            }
            return ExecutionResult.failure("command-error");
        }
        return ExecutionResult.success();
    }

    @Override
    public List<String> processSuggestions(String input, S source) {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, source))
                .join()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .toList();
    }
}
