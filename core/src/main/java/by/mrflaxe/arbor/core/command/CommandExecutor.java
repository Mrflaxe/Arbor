package by.mrflaxe.arbor.core.command;

@FunctionalInterface
public interface CommandExecutor {

    ExecutionResult execute(CommandContext ctx);
}
