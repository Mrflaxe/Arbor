package by.mrflaxe.arbor.core.command.signature;

import by.mrflaxe.arbor.core.command.SenderExecutor;
import by.mrflaxe.arbor.core.command.SingleArgExecutor;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;

import java.util.List;

public class Signature {

    public static <S> CommandSignature<S> of(SenderExecutor<S> executor) {
        return new CommandSignature<>(List.of(), ctx -> executor.execute(ctx.getSource()));
    }

    public static <S, T> CommandSignature<S> of(ArgumentDefinition<S, T> arg, SingleArgExecutor<S, T> executor) {
        return new CommandSignature<>(List.of(arg), ctx -> executor.execute(ctx.getSource(), ctx.get(arg)));
    }

    public static <S> CommandSignatureBuilder<S> builder() {
        return new CommandSignatureBuilder<>();
    }
}
