package by.mrflaxe.arbor.core.command;

@FunctionalInterface
public interface SingleArgExecutor<S, T> {

    ExecutionResult execute(S source, T arg);
}
