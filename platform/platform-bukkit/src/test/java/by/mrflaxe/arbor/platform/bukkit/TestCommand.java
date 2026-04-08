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
        return "greet";
    }

    @Override
    public List<CommandSignature<CommandSender>> getCommandSignatures() {
        return List.of(
                Signature.of(this::greetSelf),
                Signature.of(PlayerArgument.INSTANCE, this::greetTarget)
        );
    }

    private ExecutionResult greetSelf(CommandSender sender) {
        sender.sendMessage("Hello, " + sender.getName() + "!");
        return ExecutionResult.success();
    }

    private ExecutionResult greetTarget(CommandSender sender, Player target) {
        sender.sendMessage("Hello, " + target.getName() + "!");
        target.sendMessage(sender.getName() + " says hello to you!");
        return ExecutionResult.success();
    }
}
