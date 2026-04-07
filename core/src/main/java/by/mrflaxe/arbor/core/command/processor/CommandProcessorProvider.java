package by.mrflaxe.arbor.core.command.processor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandProcessorProvider {

    public static CommandProcessorProvider getInstance() {
        return new CommandProcessorProvider();
    }
}
