package by.mrflaxe.arbor.api;

import by.mrflaxe.arbor.core.Definition;
import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.Platform;

class ArborApiImpl implements ArborAPI {

    private final DefinitionRegistry registry = new DefinitionRegistry();
    private final Platform platform;
    private final Object platformContext;

    ArborApiImpl(Platform platform, Object platformContext) {
        this.platform = platform;
        this.platformContext = platformContext;
    }

    @Override
    public ArborAPI register(Definition definition) {
        registry.add(definition);
        return this;
    }

    @Override
    public void start() {
        platform.initialize(platformContext, registry);
    }
}
