package by.mrflaxe.arbor.core.command;

import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;

public class CommandContext<S> {

    private final com.mojang.brigadier.context.CommandContext<S> brigCtx;

    public CommandContext(com.mojang.brigadier.context.CommandContext<S> brigCtx) {
        this.brigCtx = brigCtx;
    }

    public S getSource() {
        return brigCtx.getSource();
    }

    public <T> T get(ArgumentDefinition<S, T> arg) {
        return brigCtx.getArgument(arg.getName(), arg.getValueType());
    }
}
