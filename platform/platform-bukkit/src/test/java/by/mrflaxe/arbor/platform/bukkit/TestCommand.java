package by.mrflaxe.arbor.platform.bukkit;

import by.mrflaxe.arbor.core.CommandDefinition;
import by.mrflaxe.arbor.core.command.ExecutionResult;
import by.mrflaxe.arbor.core.command.signature.CommandSignature;
import by.mrflaxe.arbor.core.command.signature.Signature;
import by.mrflaxe.arbor.platform.bukkit.argument.PlayerArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TestCommand implements CommandDefinition<CommandSender> {

    @Override
    public String getName() {
        return "test";
    }

    public List<CommandSignature<CommandSender>> getSignatures() {
        return List.of(
            Signature.of(
                    List.of(new PlayerArgument()),
                    ctx -> execute((CommandSender) ctx.source())
            )
        );
    }

    public ExecutionResult execute(CommandSender sender) {

    }

    public void execute(CommandSender sender, Player player) {

    }
}
