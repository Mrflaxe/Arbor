package by.mrflaxe.arbor.core.command.signature;

import by.mrflaxe.arbor.core.command.CommandExecutor;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;

import java.util.LinkedList;
import java.util.List;

public class CommandSignatureBuilder<S> {

    private final List<ArgumentDefinition> argumentDefinitions = new LinkedList<>();
    private CommandExecutor commandExecutor;

    public CommandSignatureBuilder<S> arg(ArgumentDefinition argumentDefinition) {
        argumentDefinitions.add(argumentDefinition);
        return this;
    }

    public CommandSignatureBuilder<S> executes(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        return this;
    }

    public CommandSignature<S> build() {

    }
}
