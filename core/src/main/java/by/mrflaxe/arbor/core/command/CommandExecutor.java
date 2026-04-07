package by.mrflaxe.arbor.core.command;

@FunctionalInterface
public interface CommandExecutor<S> {

    ExecutionResult execute(CommandContext<S> ctx);
}
