package by.mrflaxe.arbor.core.command.signature;

import by.mrflaxe.arbor.core.command.CommandContext;
import by.mrflaxe.arbor.core.command.CommandExecutor;
import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandSignature<S> {

    Collection<ArgumentDefinition<S, ?>> arguments;
    CommandExecutor<S> executor;

    public Collection<ArgumentDefinition<S, ?>> getArguments() {
        return List.copyOf(arguments);
    }

    public ExecutionResult execute(CommandContext<S> ctx) {
        return executor.execute(ctx);
    }
}
