package by.mrflaxe.arbor.api;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.DefinitionRegistry;

public class ArborApiImpl implements ArborAPI {

    private final DefinitionRegistry registry;

    public ArborApiImpl() {
        this.registry = new DefinitionRegistry();
    }

    public boolean registerCommandDefinition(CommandDefinition commandDefinition) {
        registry.add(commandDefinition);
        return true;
    }
}
