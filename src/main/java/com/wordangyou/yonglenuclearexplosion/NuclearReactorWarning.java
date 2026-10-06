package com.wordangyou.yonglenuclearexplosion;

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.reactors.Reactor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class NuclearReactorWarning implements Listener {

    private final JavaPlugin plugin;

    public NuclearReactorWarning(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 监听 Slimefun 方块放置
     *
     * 只处理核反应堆。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlimefunBlockPlace(SlimefunBlockPlaceEvent event) {

        /*
         * 判断是不是核反应堆
         */
        if (!(event.getSlimefunItem() instanceof Reactor)) {
            return;
        }

        /*
         * 获取反应堆位置
         */
        Location blockLocation = event.getBlockPlaced().getLocation();

        /*
         * 从配置读取爆炸范围
         */
        int explosionRadius = plugin.getConfig().getInt("explosion.radius", 15);

        /*
         * 建议安全距离：
         *
         * 爆炸范围 + 10
         */
        int recommendedDistance = explosionRadius + 10;

        /*
         * 显示悬浮警告
         */
        showWarning(blockLocation, recommendedDistance);

        /*
         * ==============================
         * 聊天框警告
         * ==============================
         */

        event.getPlayer().sendMessage(ChatColor.RED + "⚠ 警告！");

        event.getPlayer().sendMessage(ChatColor.YELLOW + "散热不佳会导致核爆炸！");

        event.getPlayer().sendMessage(ChatColor.RED + "爆炸范围非常大！");
        event.getPlayer().sendMessage(ChatColor.RED + "领地/地皮保护无法阻止核爆炸！");

        event.getPlayer().sendMessage(ChatColor.GRAY + "请确保反应堆周围有足够的水并正确安装冷却组件。");

        event.getPlayer()
                .sendMessage(ChatColor.GOLD
                        + "建议远离其他机器及重要建筑至少 "
                        + ChatColor.RED
                        + recommendedDistance
                        + ChatColor.GOLD
                        + " 格，以免受到核爆牵连！");
    }

    /**
     * 在反应堆上方生成临时警告
     */
    private void showWarning(Location blockLocation, int recommendedDistance) {

        /*
         * 是否启用警告
         */
        if (!plugin.getConfig().getBoolean("warning.enabled", true)) {
            return;
        }

        /*
         * 警告高度
         */
        double height = plugin.getConfig().getDouble("warning.height", 1.8);

        /*
         * TextDisplay 位置
         *
         * 方块中心：
         * X + 0.5
         * Y + height
         * Z + 0.5
         */
        Location location = blockLocation.clone().add(0.5, height, 0.5);

        /*
         * 创建 TextDisplay
         */
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class, entity -> {

            /*
             * ==========================
             * 警告文字
             * ==========================
             */
            entity.setText(ChatColor.RED
                    + "⚠ 警告！ ⚠\n"
                    + ChatColor.YELLOW
                    + "散热不佳会导致核爆炸！\n"
                    + ChatColor.RED
                    + "爆炸范围非常大！\n"
                    + ChatColor.GRAY
                    + "请确保冷却系统正常！\n"
                    + ChatColor.RED
                    + "注意：领地/地皮保护无法阻止核爆！\n"
                    + ChatColor.GOLD
                    + "建议远离其他机器及重要建筑至少 "
                    + ChatColor.RED
                    + recommendedDistance
                    + ChatColor.GOLD
                    + " 格！");

            /*
             * ==========================
             * 显示设置
             * ==========================
             */

            /*
             * 始终朝向玩家
             */
            entity.setBillboard(Display.Billboard.CENTER);

            /*
             * 黑色半透明背景
             */
            entity.setBackgroundColor(Color.fromARGB(120, 0, 0, 0));

            /*
             * 红色发光颜色
             */
            entity.setGlowColorOverride(Color.RED);

            /*
             * 不穿墙显示
             */
            entity.setSeeThrough(false);

            /*
             * 最大观看距离
             */
            entity.setViewRange(32.0f);

            /*
             * 文字居中
             */
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);

            /*
             * 文字阴影
             */
            entity.setShadowed(true);
        });

        /*
         * ==========================
         * 自动删除
         * ==========================
         *
         * 默认 300 tick
         * = 15 秒
         */
        long duration = plugin.getConfig().getLong("warning.duration-ticks", 300L);

        /*
         * <= 0 表示不自动删除
         */
        if (duration <= 0) {
            return;
        }

        /*
         * 延迟删除 TextDisplay
         */
        Bukkit.getScheduler()
                .runTaskLater(
                        plugin,
                        () -> {
                            if (display.isValid()) {
                                display.remove();
                            }
                        },
                        duration);
    }
}
