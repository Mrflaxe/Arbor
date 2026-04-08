package by.mrflaxe.arbor.platform.bukkit;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.DefinitionRegistry;
import by.mrflaxe.arbor.core.NamespaceDefinition;
import by.mrflaxe.arbor.core.Platform;
import by.mrflaxe.arbor.core.command.assemble.BrigadierDispatcherAssembler;
import by.mrflaxe.arbor.core.command.processor.CommandProcessor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class BukkitPlatform implements Platform {

    @Override
    public boolean supports(Class<?> contextType) {
        return JavaPlugin.class.isAssignableFrom(contextType);
    }

    @Override
    public void initialize(Object context, DefinitionRegistry registry) {
        JavaPlugin plugin = (JavaPlugin) context;
        BrigadierDispatcherAssembler<CommandSender> assembler = new BrigadierDispatcherAssembler<>();
        CommandProcessor<CommandSender> processor = assembler.assemble(registry);

        rootCommandNames(registry).forEach(name -> bindToPlugin(plugin, processor, name));
    }

    private List<String> rootCommandNames(DefinitionRegistry registry) {
        List<String> names = new ArrayList<>();
        registry.getSpecific(CommandDefinition.class).stream()
                .filter(cmd -> cmd.getParent() == null)
                .map(CommandDefinition::getName)
                .forEach(names::add);

        registry.getSpecific(NamespaceDefinition.class).stream()
                .filter(ns -> ns.getParent() == null)
                .map(NamespaceDefinition::getNamespace)
                .forEach(names::add);

        return names;
    }

    private void bindToPlugin(JavaPlugin plugin, CommandProcessor<CommandSender> processor, String name) {
        PluginCommand cmd = plugin.getServer().getPluginCommand(name);
        if (cmd == null) {
            plugin.getLogger().severe("Command /" + name + " must be declared in plugin.yml");
            return;
        }
        cmd.setExecutor((sender, command, label, args) -> {
            String input = label + (args.length > 0 ? " " + String.join(" ", args) : "");
            processor.processCommand(input, sender);
            return true;
        });
        cmd.setTabCompleter((sender, command, label, args) -> {
            String input = label + " " + String.join(" ", args);
            return processor.processSuggestions(input, sender);
        });
    }
}
