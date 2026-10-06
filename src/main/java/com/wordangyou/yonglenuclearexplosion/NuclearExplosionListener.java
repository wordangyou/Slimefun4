package com.wordangyou.yonglenuclearexplosion;

import io.github.thebusybiscuit.slimefun4.api.events.ReactorExplodeEvent;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class NuclearExplosionListener implements Listener {

    private final YongleNuclearExplosion plugin;
    private final NuclearExplosion nuclearExplosion;

    public NuclearExplosionListener(YongleNuclearExplosion plugin) {
        this.plugin = plugin;
        this.nuclearExplosion = new NuclearExplosion(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReactorExplode(ReactorExplodeEvent event) {

        if (!plugin.getConfig().getBoolean("explosion.enabled", true)) {
            return;
        }

        Location location = event.getLocation();

        if (location == null || location.getWorld() == null) {
            return;
        }

        /*
         * 找到这个核反应堆是谁放置的
         */
        UUID owner = plugin.getReactorOwner(location);

        Bukkit.getScheduler()
                .runTaskLater(
                        plugin.getJavaPlugin(),
                        () -> {

                            /*
                             * Slimefun 原本产生的岩浆清掉
                             */
                            if (location.getBlock().getType() == org.bukkit.Material.LAVA) {

                                location.getBlock().setType(org.bukkit.Material.AIR);
                            }

                            /*
                             * 执行永乐核爆
                             *
                             * owner：
                             * 这个核反应堆的所有者
                             */
                            nuclearExplosion.explode(location, owner);

                            /*
                             * 爆炸完成后删除记录
                             */
                            plugin.removeReactorOwner(location);
                        },
                        2L);
    }
}
