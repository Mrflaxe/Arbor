package by.mrflaxe.arbor.core.error;

import by.mrflaxe.arbor.core.command.ExecutionResult;

public interface ErrorHandler<S> {

    void onCommandError(S source, String input, int cursor, String detail, String usage);

    void onExecutionFailure(S source, ExecutionResult result);
}
