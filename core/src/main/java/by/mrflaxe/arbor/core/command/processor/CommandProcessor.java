package by.mrflaxe.arbor.core.command.processor;

import by.mrflaxe.arbor.core.command.ExecutionResult;

import java.util.List;

public interface CommandProcessor<S> {

    ExecutionResult processCommand(String input, S source);

    List<String> processSuggestions(String input, S source);
}
