package by.mrflaxe.arbor.platform.bukkit;

import by.mrflaxe.arbor.core.DefinitionRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BukkitPlatform {

    JavaPlugin plugin;
    Logger logger;

    public BukkitPlatform(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }


    public void registerCommands(JavaPlugin plugin, DefinitionRegistry registry) {
        CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();


    }

    public void registerInBukkit(CommandDispatcher<CommandSender> dispatcher, String literal) {
        try {
            PluginCommand cmd = plugin.getServer().getPluginCommand(literal);
            if (cmd == null) {
                logger.severe("Command " + literal + " should be declared in plugin.yml!");
                return;
            }

            cmd.setExecutor((sender, command, label, args) -> {
                String input = label + " " + String.join(" ", args);
                try {
                    dispatcher.execute(input, sender);
                } catch (Exception e) { // TODO wrapping with error message handling and custom messages
                    sender.sendMessage("Exception on command execution: " + e.getMessage());
                    logger.severe("Exception on command execution: " + e.getMessage());
                }
                return true;
            });

            cmd.setTabCompleter((sender, command, label, args) -> {
                String input = label + " " + String.join(" ", args);
                try {
                    return dispatcher.getCompletionSuggestions(dispatcher.parse(input, sender))
                            .get()
                            .getList()
                            .stream()
                            .map(Suggestion::getText)
                            .toList();
                } catch (Exception e) {
                    return List.of();
                }
            });
        } catch (Exception e) {
            logger.severe("Exception on " + literal + " command registration: " + e.getMessage());
        }
    }
}
