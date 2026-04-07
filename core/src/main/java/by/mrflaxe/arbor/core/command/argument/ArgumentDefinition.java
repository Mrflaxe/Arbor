package by.mrflaxe.arbor.core.command.argument;

import java.util.List;

public interface ArgumentDefinition<S, T> {

    String getName();

    Class<T> getType();

    List<String> suggest(S source, String input);
}
