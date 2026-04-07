package by.mrflaxe.arbor.core.command.assemble;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.NamespaceDefinition;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;
import by.mrflaxe.arbor.core.command.processor.BrigadierCommandProcessor;
import by.mrflaxe.arbor.core.command.processor.CommandProcessor;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BrigadierDispatcherAssembler<S> {

    public CommandProcessor<S> assemble(DefinitionRegistry registry) {
        CommandDispatcher<S> dispatcher = new CommandDispatcher<>();

        // Временное хранилище всех созданных literal-узлов
        Map<String, LiteralArgumentBuilder<S>> builders = new HashMap<>();

        addNamespaces(builders, registry);
        addExecutableCommands(builders, registry);

        // 3. Связываем parent → child
        registry.definitions().forEach(def -> {
            String parent = def.getParent();
            String name = switch (def) {
                case CommandDefinition c -> c.getName();
                case NamespaceDefinition n -> n.getNamespace();
                default -> null;
            };

            if (name == null) return;

            LiteralCommandNode<S> node = builders.get(name);

            if (parent == null) {
                dispatcher.getRoot().addChild(node);
            } else {
                LiteralCommandNode<S> parentNode = builders.get(parent);
                if (parentNode == null) {
                    throw new IllegalStateException(
                            "Parent '" + parent + "' not found for '" + name + "'"
                    );
                }
                parentNode.addChild(node);
            }
        });

        return new BrigadierCommandProcessor<>(dispatcher);
    }

    private void addNamespaces(Map<String, LiteralArgumentBuilder<S>> builders, DefinitionRegistry registry) {
        registry.getSpecific(NamespaceDefinition.class)
                .forEach(namespaceDefinition -> {
                    LiteralArgumentBuilder<S> literalBuilder = LiteralArgumentBuilder.literal(namespaceDefinition.getNamespace());
                    builders.put(namespaceDefinition.getNamespace(), literalBuilder);
                });
    }

    private void addExecutableCommands(Map<String, LiteralArgumentBuilder<S>> builders, DefinitionRegistry registry) {
        getCommandDefinitions(registry).forEach(cmd -> {
                    LiteralArgumentBuilder<S> builder = LiteralArgumentBuilder.literal(cmd.getName());

                    for (CommandSignature<S> signature : cmd.getCommandSignatures()) {
                        ArgumentBuilder<S, ?> current = builder;

                        for (ArgumentDefinition<S> arg : signature.getArguments()) {
                            RequiredArgumentBuilder<S, ?> argBuilder =
                                    RequiredArgumentBuilder.argument(
                                            arg.getName(),
                                            new ArgumentType<?>() {

                                                @Override
                                                public ? parse(StringReader reader) throws CommandSyntaxException {
                                                    return arg.parse(reader.getString());
                                                }

                                                @Override
                                                public <T> CompletableFuture<Suggestions> listSuggestions(CommandContext<T> context, SuggestionsBuilder builder) {
                                                    return ArgumentType.super.listSuggestions(context, builder);
                                                }

                                                @Override
                                                public Collection<String> getExamples() {
                                                    return ArgumentType.super.getExamples();
                                                }
                                            }
                                    );

                            current.then(argBuilder);
                            current = argBuilder;
                        }

                        current.executes(signature::execute);
                    }

                    builders.put(cmd.getName(), builder);
        });
    }

    @SuppressWarnings("unchecked")
    private List<CommandDefinition<S>> getCommandDefinitions(DefinitionRegistry registry) {
        return registry.getSpecific(CommandDefinition.class)
                .stream()
                .map(raw -> (CommandDefinition<S>) raw)
                .toList();
    }

}
