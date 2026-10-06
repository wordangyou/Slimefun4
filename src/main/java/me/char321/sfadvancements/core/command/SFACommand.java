package me.char321.sfadvancements.core.command;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import me.char321.sfadvancements.SFAdvancements;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

public class SFACommand implements CommandExecutor {
    private final List<SubCommand> subcommands = new LinkedList<>();

    public SFACommand() {
        subcommands.add(new SaveCommand());
        subcommands.add(new RevokeCommand());
        subcommands.add(new GrantCommand());
        subcommands.add(new GuiCommand());
        subcommands.add(new DumpItemCommand());
        subcommands.add(new ReloadCommand());
        subcommands.add(new ImportCommand());

        PluginCommand command = SFAdvancements.getPlugin().getCommand("sfadvancements");
        if (command != null) {
            command.setTabCompleter(new SFATabCompleter(this));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            for (SubCommand subcmd : subcommands) {
                if (args[0].equalsIgnoreCase(subcmd.getCommandName())) {
                    if (sender.hasPermission("sfa.command." + subcmd.getCommandName())) {
                        return subcmd.onExecute(sender, command, label, args);
                    } else {
                        sender.sendMessage("你没有权限。");
                        return false;
                    }
                }
            }
            sender.sendMessage("未知指令! 可用指令："
                    + subcommands.stream().map(SubCommand::getCommandName).collect(Collectors.joining(", ")));
            return false;
        }
        sender.sendMessage("SlimefunAdvancements 粘液进度 版本 "
                + SFAdvancements.getPlugin().getDescription().getVersion());
        return true;
    }

    public List<SubCommand> getSubCommands() {
        return subcommands;
    }
}
