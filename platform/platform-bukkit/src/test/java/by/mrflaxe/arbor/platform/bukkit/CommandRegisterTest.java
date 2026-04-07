package by.mrflaxe.arbor.platform.bukkit;

import by.mrflaxe.arbor.core.CommandDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommandRegisterTest {

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("Should register base command")
    void shouldRegisterBaseCommand() {
        CommandDefinition testCommand = new TestCommand();
    }
}
