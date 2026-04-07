package by.mrflaxe.arbor.core.command.assemble;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.NamespaceDefinition;
import by.mrflaxe.arbor.core.command.CommandContext;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;
import by.mrflaxe.arbor.core.command.argument.ArgumentParseException;
import by.mrflaxe.arbor.core.command.processor.BrigadierCommandProcessor;
import by.mrflaxe.arbor.core.command.processor.CommandProcessor;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BrigadierDispatcherAssembler<S> {

    public CommandProcessor<S> assemble(DefinitionRegistry registry) {
        CommandDispatcher<S> dispatcher = new CommandDispatcher<>();
        Map<String, LiteralArgumentBuilder<S>> builders = new HashMap<>();

        addNamespaces(builders, registry);
        addExecutableCommands(builders, registry);
        attachChildrenToParents(builders, registry);
        registerRootCommands(builders, registry, dispatcher);

        return new BrigadierCommandProcessor<>(dispatcher);
    }

    private void addNamespaces(Map<String, LiteralArgumentBuilder<S>> builders, DefinitionRegistry registry) {
        registry.getSpecific(NamespaceDefinition.class)
                .forEach(ns -> builders.put(
                        ns.getNamespace(),
                        LiteralArgumentBuilder.literal(ns.getNamespace())
                ));
    }

    private void addExecutableCommands(Map<String, LiteralArgumentBuilder<S>> builders, DefinitionRegistry registry) {
        getCommandDefinitions(registry).forEach(cmd -> {
            validateSignatures(cmd.getName(), cmd.getCommandSignatures());

            LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(cmd.getName());

            for (CommandSignature<S> signature : cmd.getCommandSignatures()) {
                attachSignature(builder, signature);
            }

            builders.put(cmd.getName(), builder);
        });
    }

    private void validateSignatures(String commandName, List<CommandSignature<S>> signatures) {
        List<List<ArgumentDefinition<S, ?>>> paths = signatures.stream()
                .map(sig -> (List<ArgumentDefinition<S, ?>>) new ArrayList<>(sig.getArguments()))
                .toList();

        detectDuplicatePaths(commandName, paths);
        detectStringShadowing(commandName, paths);
    }

    private void detectDuplicatePaths(String commandName, List<List<ArgumentDefinition<S, ?>>> paths) {
        for (int i = 0; i < paths.size(); i++) {
            for (int j = i + 1; j < paths.size(); j++) {
                if (sameArgNameSequence(paths.get(i), paths.get(j))) {
                    throw new IllegalArgumentException(
                            "Command '" + commandName + "' has duplicate signature: ["
                            + toArgNameString(paths.get(i)) + "]"
                    );
                }
            }
        }
    }

    private boolean sameArgNameSequence(List<ArgumentDefinition<S, ?>> a, List<ArgumentDefinition<S, ?>> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).getName().equals(b.get(i).getName())) return false;
        }
        return true;
    }

    private void detectStringShadowing(String commandName, List<List<ArgumentDefinition<S, ?>>> paths) {
        Set<String> firstArgNames = new LinkedHashSet<>();
        for (List<ArgumentDefinition<S, ?>> path : paths) {
            if (!path.isEmpty()) firstArgNames.add(path.get(0).getName());
        }

        if (firstArgNames.size() > 1) {
            for (List<ArgumentDefinition<S, ?>> path : paths) {
                if (!path.isEmpty() && path.get(0).getValueType() == String.class) {
                    throw new IllegalArgumentException(
                            "Command '" + commandName + "': String argument '" + path.get(0).getName()
                            + "' cannot coexist with other argument types at the same branching point"
                    );
                }
            }
        }

        Map<String, List<List<ArgumentDefinition<S, ?>>>> groups = new LinkedHashMap<>();
        for (List<ArgumentDefinition<S, ?>> path : paths) {
            if (!path.isEmpty()) {
                groups.computeIfAbsent(path.get(0).getName(), k -> new ArrayList<>())
                        .add(path.subList(1, path.size()));
            }
        }
        groups.values().forEach(subPaths -> detectStringShadowing(commandName, subPaths));
    }

    private String toArgNameString(List<ArgumentDefinition<S, ?>> path) {
        return path.stream().map(ArgumentDefinition::getName).collect(Collectors.joining(", "));
    }

    private void attachSignature(LiteralArgumentBuilder<S> builder, CommandSignature<S> signature) {
        List<ArgumentDefinition<S, ?>> args = new ArrayList<>(signature.getArguments());

        if (args.isEmpty()) {
            builder.executes(brigCtx -> executeSignature(brigCtx, signature));
            return;
        }

        List<RequiredArgumentBuilder<S, ?>> argBuilders = args.stream()
                .map(this::buildArgumentBuilder)
                .collect(Collectors.toList());

        argBuilders.get(argBuilders.size() - 1)
                .executes(brigCtx -> executeSignature(brigCtx, signature));

        for (int i = argBuilders.size() - 1; i > 0; i--) {
            argBuilders.get(i - 1).then(argBuilders.get(i));
        }

        builder.then(argBuilders.get(0));
    }

    private int executeSignature(com.mojang.brigadier.context.CommandContext<S> brigCtx, CommandSignature<S> signature) {
        CommandContext<S> ctx = new CommandContext<>(brigCtx);
        return signature.execute(ctx).isSuccess() ? 1 : 0;
    }

    @SuppressWarnings("unchecked")
    private <T> RequiredArgumentBuilder<S, T> buildArgumentBuilder(ArgumentDefinition<S, ?> rawArg) {
        ArgumentDefinition<S, T> arg = (ArgumentDefinition<S, T>) rawArg;
        ArgumentType<T> type = new ArgumentType<T>() {
            @Override
            public T parse(StringReader reader) throws CommandSyntaxException {
                int cursor = reader.getCursor();
                String raw = reader.readUnquotedString();
                try {
                    return arg.parse(raw);
                } catch (ArgumentParseException e) {
                    reader.setCursor(cursor);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                            .dispatcherParseException()
                            .createWithContext(reader, e.getMessage());
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <C> CompletableFuture<Suggestions> listSuggestions(
                    com.mojang.brigadier.context.CommandContext<C> context, SuggestionsBuilder builder) {
                S source = (S) context.getSource();
                arg.suggest(source, builder.getRemaining()).forEach(builder::suggest);
                return builder.buildFuture();
            }
        };
        return RequiredArgumentBuilder.argument(arg.getName(), type);
    }

    private void attachChildrenToParents(Map<String, LiteralArgumentBuilder<S>> builders, DefinitionRegistry registry) {
        registry.getDefinitions().forEach(def -> {
            String parent = getParent(def);
            String name = getName(def);
            if (name == null || parent == null) return;

            LiteralArgumentBuilder<S> parentBuilder = builders.get(parent);
            if (parentBuilder == null) {
                throw new IllegalStateException("Parent '" + parent + "' not found for '" + name + "'");
            }
            parentBuilder.then(builders.get(name));
        });
    }

    private void registerRootCommands(
            Map<String, LiteralArgumentBuilder<S>> builders,
            DefinitionRegistry registry,
            CommandDispatcher<S> dispatcher
    ) {
        registry.getDefinitions().forEach(def -> {
            String parent = getParent(def);
            String name = getName(def);
            if (name == null || parent != null) return;
            dispatcher.register(builders.get(name));
        });
    }

    @SuppressWarnings("unchecked")
    private List<CommandDefinition<S>> getCommandDefinitions(DefinitionRegistry registry) {
        return registry.getSpecific(CommandDefinition.class)
                .stream()
                .map(raw -> (CommandDefinition<S>) raw)
                .toList();
    }

    private String getName(Object def) {
        if (def instanceof CommandDefinition<?> c) return c.getName();
        if (def instanceof NamespaceDefinition n) return n.getNamespace();
        return null;
    }

    private String getParent(Object def) {
        if (def instanceof CommandDefinition<?> c) return c.getParent();
        if (def instanceof NamespaceDefinition n) return n.getParent();
        return null;
    }
}
