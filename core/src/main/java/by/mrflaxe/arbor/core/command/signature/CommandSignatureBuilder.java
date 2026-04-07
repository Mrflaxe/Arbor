package by.mrflaxe.arbor.core.command.signature;

import by.mrflaxe.arbor.core.command.CommandExecutor;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;

import java.util.LinkedList;
import java.util.List;

public class CommandSignatureBuilder<S> {

    private final List<ArgumentDefinition<S, ?>> argumentDefinitions = new LinkedList<>();
    private CommandExecutor<S> commandExecutor;

    public <T> CommandSignatureBuilder<S> arg(ArgumentDefinition<S, T> argumentDefinition) {
        argumentDefinitions.add(argumentDefinition);
        return this;
    }

    public CommandSignatureBuilder<S> executes(CommandExecutor<S> executor) {
        this.commandExecutor = executor;
        return this;
    }

    public CommandSignature<S> build() {
        return new CommandSignature<>(List.copyOf(argumentDefinitions), commandExecutor);
    }
}
