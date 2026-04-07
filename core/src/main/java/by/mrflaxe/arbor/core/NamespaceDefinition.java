package by.mrflaxe.arbor.core;

import org.jetbrains.annotations.Nullable;

public interface NamespaceDefinition extends Definition {

    String getNamespace();

    default @Nullable String getParent() {
        return null;
    }

}
