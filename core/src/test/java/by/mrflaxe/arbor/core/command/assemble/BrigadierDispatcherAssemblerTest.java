package by.mrflaxe.arbor.core.command.assemble;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.NamespaceDefinition;
import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;
import by.mrflaxe.arbor.core.command.argument.ArgumentParseException;
import by.mrflaxe.arbor.core.command.processor.CommandProcessor;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import by.mrflaxe.arbor.core.command.signature.CommandSignatureBuilder;
import by.mrflaxe.arbor.core.command.signature.Signature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrigadierDispatcherAssemblerTest {

    private static final String SOURCE = "testSource";

    private final BrigadierDispatcherAssembler<String> assembler = new BrigadierDispatcherAssembler<>();

    @Test
    void assemblerCreatesDispatcherForCommandWithNoArgs() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(noArgCommand("ping"));

        CommandProcessor<String> processor = assembler.assemble(registry);
        ExecutionResult result = processor.processCommand("ping", SOURCE);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void assemblerCreatesDispatcherForCommandWithTypedIntegerArg() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(intArgCommand("roll"));

        CommandProcessor<String> processor = assembler.assemble(registry);
        ExecutionResult result = processor.processCommand("roll 42", SOURCE);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void processSuggestionsReturnsArgSuggestionsForPartialInput() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(intArgCommand("roll"));

        CommandProcessor<String> processor = assembler.assemble(registry);
        List<String> suggestions = processor.processSuggestions("roll ", SOURCE);

        assertThat(suggestions).containsExactly("1", "2", "3");
    }

    @Test
    void assemblerLinksSubcommandUnderNamespace() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(namespace("admin"));
        registry.add(subcommand("kick", "admin"));

        CommandProcessor<String> processor = assembler.assemble(registry);
        ExecutionResult result = processor.processCommand("admin kick", SOURCE);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void assemblerThrowsWhenParentNamespaceNotRegistered() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(subcommand("kick", "admin"));

        assertThatThrownBy(() -> assembler.assemble(registry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin");
    }

    @Test
    void commandWithUnparseableArgReturnsFailure() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(intArgCommand("roll"));

        CommandProcessor<String> processor = assembler.assemble(registry);
        ExecutionResult result = processor.processCommand("roll notanumber", SOURCE);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void assemblerThrowsOnDuplicateSignaturePaths() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(commandWithSignatures("dup",
                List.of(IntegerArgument.INSTANCE),
                List.of(IntegerArgument.INSTANCE)
        ));

        assertThatThrownBy(() -> assembler.assemble(registry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate signature")
                .hasMessageContaining("dup");
    }

    @Test
    void assemblerThrowsWhenStringArgShadowsOtherTypeAtSameBranchingPoint() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(commandWithSignatures("shadow",
                List.of(StringArgument.INSTANCE),
                List.of(IntegerArgument.INSTANCE)
        ));

        assertThatThrownBy(() -> assembler.assemble(registry))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String argument")
                .hasMessageContaining("shadow");
    }

    @Test
    void assemblerAllowsStringArgAloneWithNoSiblings() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(commandWithSignatures("greet",
                List.of(),
                List.of(StringArgument.INSTANCE)
        ));

        assertThat(assembler.assemble(registry)).isNotNull();
    }

    @Test
    void assemblerAllowsDifferentArgTypesOnDifferentBranchLevels() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(commandWithSignatures("calc",
                List.of(IntegerArgument.INSTANCE),
                List.of(IntegerArgument.INSTANCE, IntegerArgument.INSTANCE)
        ));

        CommandProcessor<String> processor = assembler.assemble(registry);
        assertThat(processor.processCommand("calc 5", SOURCE).isSuccess()).isTrue();
        assertThat(processor.processCommand("calc 5 3", SOURCE).isSuccess()).isTrue();
    }

    private CommandDefinition<String> noArgCommand(String name) {
        return new CommandDefinition<>() {
            @Override public String getName() { return name; }

            @Override
            public List<CommandSignature<String>> getCommandSignatures() {
                return List.of(Signature.of(source -> ExecutionResult.success()));
            }
        };
    }

    private CommandDefinition<String> intArgCommand(String name) {
        return new CommandDefinition<>() {
            @Override public String getName() { return name; }

            @Override
            public List<CommandSignature<String>> getCommandSignatures() {
                return List.of(
                        Signature.of(IntegerArgument.INSTANCE,
                                (source, number) -> ExecutionResult.success())
                );
            }
        };
    }

    private CommandDefinition<String> subcommand(String name, String parent) {
        return new CommandDefinition<>() {
            @Override public String getName() { return name; }
            @Override public String getParent() { return parent; }

            @Override
            public List<CommandSignature<String>> getCommandSignatures() {
                return List.of(Signature.of(source -> ExecutionResult.success()));
            }
        };
    }

    private CommandDefinition<String> commandWithSignatures(String name, List<ArgumentDefinition<String, ?>>... argLists) {
        List<CommandSignature<String>> signatures = new java.util.ArrayList<>();
        for (List<ArgumentDefinition<String, ?>> args : argLists) {
            if (args.isEmpty()) {
                signatures.add(Signature.of(source -> ExecutionResult.success()));
            } else if (args.size() == 1) {
                signatures.add(Signature.of(args.get(0), (source, val) -> ExecutionResult.success()));
            } else {
                CommandSignatureBuilder<String> builder = Signature.<String>builder();
                for (ArgumentDefinition<String, ?> arg : args) builder.arg(arg);
                signatures.add(builder.executes(ctx -> ExecutionResult.success()).build());
            }
        }
        List<CommandSignature<String>> finalSignatures = signatures;
        return new CommandDefinition<>() {
            @Override public String getName() { return name; }
            @Override public List<CommandSignature<String>> getCommandSignatures() { return finalSignatures; }
        };
    }

    private NamespaceDefinition namespace(String name) {
        return () -> name;
    }

    private static class IntegerArgument implements ArgumentDefinition<String, Integer> {
        static final IntegerArgument INSTANCE = new IntegerArgument();

        @Override public String getName() { return "number"; }
        @Override public Class<Integer> getValueType() { return Integer.class; }

        @Override
        public Integer parse(String input) throws ArgumentParseException {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("'" + input + "' is not a valid integer");
            }
        }

        @Override
        public List<String> suggest(String source, String input) { return List.of("1", "2", "3"); }
    }

    private static class StringArgument implements ArgumentDefinition<String, String> {
        static final StringArgument INSTANCE = new StringArgument();

        @Override public String getName() { return "text"; }
        @Override public Class<String> getValueType() { return String.class; }
        @Override public String parse(String input) { return input; }
        @Override public List<String> suggest(String source, String input) { return List.of(); }
    }
}
