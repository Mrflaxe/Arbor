package by.mrflaxe.arbor.core.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CommandContext<S> {

    S source;


    public <T> T getArgumentValue(String argumentName) {

    }
}
