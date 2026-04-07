package by.mrflaxe.arbor.core;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefinitionRegistry {

    List<Definition> definitions = new ArrayList<>();

    public void add(Definition definition) {
        definitions.add(definition);
    }

    public @Unmodifiable List<Definition> getDefinitions() {
        return List.copyOf(definitions);
    }

    public<T> List<T> getSpecific(Class<T> type) {
        return definitions.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }
}
