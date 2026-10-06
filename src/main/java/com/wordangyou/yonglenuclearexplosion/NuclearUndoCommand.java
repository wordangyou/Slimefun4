package com.wordangyou.yonglenuclearexplosion;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class NuclearUndoCommand implements CommandExecutor {

    private final YongleNuclearExplosion plugin;

    public NuclearUndoCommand(YongleNuclearExplosion plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(ChatColor.RED + "这个命令只能由玩家使用！");

            return true;
        }

        if (!player.hasPermission("yonglenuclearexplosion.undo")) {

            player.sendMessage(ChatColor.RED + "你没有权限使用这个命令！");

            return true;
        }

        int amount = 1;

        if (args.length >= 1) {

            try {

                amount = Integer.parseInt(args[0]);

            } catch (NumberFormatException e) {

                player.sendMessage(ChatColor.RED + "用法：/ynundo [次数]");

                return true;
            }
        }

        if (amount <= 0) {

            player.sendMessage(ChatColor.RED + "撤回次数必须大于 0！");

            return true;
        }

        int historySize = plugin.getNuclearHistory().size();

        if (historySize <= 0) {

            player.sendMessage(ChatColor.YELLOW + "没有可以撤回的核爆。");

            return true;
        }

        if (amount > historySize) {
            amount = historySize;
        }

        int undone = 0;
        int restoredBlocks = 0;

        for (int i = 0; i < amount; i++) {

            NuclearExplosionSnapshot snapshot = plugin.getNuclearHistory().removeLast();

            if (snapshot == null) {
                break;
            }

            try {

                int restored = snapshot.restore();

                restoredBlocks += restored;

                plugin.getNuclearHistory().delete(snapshot);

                undone++;

            } catch (Throwable throwable) {

                plugin.getLogger().warning("核爆撤回失败：" + throwable.getMessage());

                throwable.printStackTrace();
            }
        }

        if (undone > 0) {

            player.sendMessage(ChatColor.GREEN + "✓ 已撤回 " + undone + " 次核爆！");

            player.sendMessage(ChatColor.GREEN + "✓ 恢复方块：" + restoredBlocks + " 个");

            player.sendMessage(ChatColor.GRAY + "注意：掉落物、火焰和实体伤害不会被撤回。");

        } else {

            player.sendMessage(ChatColor.RED + "核爆撤回失败！");
        }

        return true;
    }
}
