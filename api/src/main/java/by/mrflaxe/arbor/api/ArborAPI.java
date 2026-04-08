package by.mrflaxe.arbor.api;

import by.mrflaxe.arbor.core.Definition;

public interface ArborAPI {

    ArborAPI register(Definition definition);

    void start();
}
