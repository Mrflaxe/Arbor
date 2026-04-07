package by.mrflaxe.arbor.platform.bukkit.argument;

import by.mrflaxe.arbor.core.command.argument.ArgumentDefinition;
import by.mrflaxe.arbor.core.command.argument.ArgumentParseException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerArgument implements ArgumentDefinition<CommandSender, Player> {

    public static final PlayerArgument INSTANCE = new PlayerArgument();

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public Class<Player> getValueType() {
        return Player.class;
    }

    @Override
    public Player parse(String input) throws ArgumentParseException {
        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            throw new ArgumentParseException("Player '" + input + "' not found or not online");
        }
        return player;
    }

    @Override
    public List<String> suggest(CommandSender source, String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
