package by.mrflaxe.arbor.core;

import by.mrflaxe.arbor.core.command.requirement.RequirementDefinition;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandDefinition<S> extends Definition {

    String getName();

    default @Nullable String getParent() {
        return null;
    }

    default List<RequirementDefinition> getRequirements() {
        return List.of();
    }

    default List<CommandSignature<S>> getCommandSignatures() {
        return List.of();
    }
}
