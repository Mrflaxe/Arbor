package by.mrflaxe.arbor.core.command;

@FunctionalInterface
public interface SenderExecutor<S> {

    ExecutionResult execute(S source);
}
