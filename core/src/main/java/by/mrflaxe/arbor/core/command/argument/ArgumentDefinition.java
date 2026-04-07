package by.mrflaxe.arbor.core.command.argument;

import java.util.List;

public interface ArgumentDefinition<S, T> {

    String getName();

    Class<T> getValueType();

    T parse(String input) throws ArgumentParseException;

    List<String> suggest(S source, String input);
}
