package me.char321.sfadvancements.core.command;

import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    boolean onExecute(CommandSender sender, Command command, String label, String[] args);

    @Nonnull
    String getCommandName();

    List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}
