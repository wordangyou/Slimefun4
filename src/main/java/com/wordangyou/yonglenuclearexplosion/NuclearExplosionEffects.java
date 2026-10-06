package com.wordangyou.yonglenuclearexplosion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NuclearExplosionEffects {

    private final JavaPlugin plugin;

    public NuclearExplosionEffects(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 播放全服核爆效果
     */
    public void play(Location location) {

        if (location == null || location.getWorld() == null) {
            return;
        }

        /*
         * 是否启用核爆全服效果
         */
        if (!plugin.getConfig().getBoolean("effects.enabled", true)) {
            return;
        }

        /*
         * ==============================
         * 全服聊天栏提示
         * ==============================
         */
        String message = ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚠ 一处发生了核爆炸！";

        Bukkit.broadcastMessage(message);

        /*
         * 副提示
         */
        Bukkit.broadcastMessage(ChatColor.RED + "核反应堆发生严重事故，爆炸范围非常大！");

        /*
         * ==============================
         * 全服雷声
         * ==============================
         *
         * 每个玩家都能听见。
         */
        for (Player player : Bukkit.getOnlinePlayers()) {

            try {

                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 0.6f);

            } catch (Throwable ignored) {
            }
        }

        /*
         * ==============================
         * TNT 连续爆炸
         * ==============================
         *
         * 从核爆位置播放。
         */
        int explosionCount = plugin.getConfig().getInt("effects.tnt-explosions", 8);

        long interval = plugin.getConfig().getLong("effects.tnt-interval", 4L);

        if (explosionCount <= 0) {
            return;
        }

        if (interval < 1) {
            interval = 1;
        }

        /*
         * 保存最终值
         */
        final long soundInterval = interval;

        /*
         * 连续播放 TNT 声音
         */
        for (int i = 0; i < explosionCount; i++) {

            final int index = i;

            Bukkit.getScheduler()
                    .runTaskLater(
                            plugin,
                            () -> {
                                if (location.getWorld() == null) {
                                    return;
                                }

                                /*
                                 * TNT 爆炸声音
                                 */
                                location.getWorld()
                                        .playSound(
                                                location, Sound.ENTITY_GENERIC_EXPLODE, 8.0f, 0.5f + (index * 0.04f));

                                /*
                                 * 最后一声稍微加重
                                 */
                                if (index == explosionCount - 1) {

                                    location.getWorld()
                                            .playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 0.5f);
                                }
                            },
                            soundInterval * i);
        }
    }
}
