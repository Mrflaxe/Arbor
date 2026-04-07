package by.mrflaxe.arbor.core.command.signature;

import by.mrflaxe.arbor.core.command.CommandExecutor;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Signature {

    public static<S> CommandSignature<S> of (CommandExecutor executor) {
        return of(List.of(), executor);
    }

    public static<S> CommandSignature<S> of(Collection<ArgumentDefinition<S, ?>> args, CommandExecutor executor) {
        return new CommandSignature<>(args, executor);
    }
}
