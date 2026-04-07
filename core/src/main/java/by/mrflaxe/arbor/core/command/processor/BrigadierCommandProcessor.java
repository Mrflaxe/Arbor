package by.mrflaxe.arbor.core.command.processor;

import by.mrflaxe.arbor.core.command.ExecutionResult;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;

import java.util.List;

public class BrigadierCommandProcessor<S> implements CommandProcessor<S> {

    private final CommandDispatcher<S> dispatcher;

    public BrigadierCommandProcessor(CommandDispatcher<S> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ExecutionResult processCommand(String input, S source) {
        try {
            dispatcher.execute(input, source);
        } catch (Exception e) {
            return new ExecutionResult(false, "syntax error");
        }

        return  new ExecutionResult(true, "success");
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
