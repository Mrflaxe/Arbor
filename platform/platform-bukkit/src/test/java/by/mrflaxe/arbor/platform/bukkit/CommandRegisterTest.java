package by.mrflaxe.arbor.platform.bukkit;

import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.command.argument.ArgumentParseException;
import by.mrflaxe.arbor.core.command.assemble.BrigadierDispatcherAssembler;
import by.mrflaxe.arbor.core.command.processor.CommandProcessor;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import by.mrflaxe.arbor.platform.bukkit.argument.PlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommandRegisterTest {

    @Test
    void playerArgumentHasCorrectName() {
        assertThat(PlayerArgument.INSTANCE.getName()).isEqualTo("player");
    }

    @Test
    void playerArgumentHasCorrectValueType() {
        assertThat(PlayerArgument.INSTANCE.getValueType()).isEqualTo(Player.class);
    }

    @Test
    void playerArgumentParsesOnlinePlayer() throws ArgumentParseException {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Steve");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("Steve")).thenReturn(player);

            Player result = PlayerArgument.INSTANCE.parse("Steve");

            assertThat(result).isEqualTo(player);
        }
    }

    @Test
    void playerArgumentThrowsWhenPlayerNotOnline() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer("Unknown")).thenReturn(null);

            assertThatThrownBy(() -> PlayerArgument.INSTANCE.parse("Unknown"))
                    .isInstanceOf(ArgumentParseException.class)
                    .hasMessageContaining("Unknown");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void playerArgumentSuggestsOnlinePlayersMatchingPrefix() {
        Player steve = mock(Player.class);
        when(steve.getName()).thenReturn("Steve");
        Player alex = mock(Player.class);
        when(alex.getName()).thenReturn("Alex");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(steve, alex));

            CommandSender sender = mock(CommandSender.class);
            List<String> suggestions = PlayerArgument.INSTANCE.suggest(sender, "S");

            assertThat(suggestions).containsExactly("Steve");
        }
    }

    @Test
    void testCommandHasExpectedName() {
        assertThat(new TestCommand().getName()).isEqualTo("greet");
    }

    @Test
    void testCommandHasTwoSignatures() {
        List<CommandSignature<CommandSender>> signatures = new TestCommand().getCommandSignatures();
        assertThat(signatures).hasSize(2);
    }

    @Test
    void testCommandFirstSignatureHasNoArguments() {
        CommandSignature<CommandSender> noArgSignature = new TestCommand().getCommandSignatures().get(0);
        assertThat(noArgSignature.getArguments()).isEmpty();
    }

    @Test
    void testCommandSecondSignatureHasPlayerArgument() {
        CommandSignature<CommandSender> playerSignature = new TestCommand().getCommandSignatures().get(1);
        assertThat(playerSignature.getArguments()).hasSize(1);
        assertThat(playerSignature.getArguments().iterator().next()).isInstanceOf(PlayerArgument.class);
    }

    @Test
    void assemblerBuildsDispatcherForTestCommand() {
        DefinitionRegistry registry = new DefinitionRegistry();
        registry.add(new TestCommand());

        CommandProcessor<CommandSender> processor =
                new BrigadierDispatcherAssembler<CommandSender>().assemble(registry);

        assertThat(processor).isNotNull();
    }
}
