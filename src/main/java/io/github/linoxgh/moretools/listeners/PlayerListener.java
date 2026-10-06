package io.github.linoxgh.moretools.listeners;

import io.github.linoxgh.moretools.MoreTools;
import io.github.linoxgh.moretools.handlers.ItemInteractHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    public PlayerListener() {
        Bukkit.getPluginManager().registerEvents(this, MoreTools.getPlugin());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (SlimefunUtils.canPlayerUseItem(e.getPlayer(), e.getItem(), true)) {
            SlimefunItem sfItem = SlimefunItem.getByItem(e.getItem());
            if (sfItem == null) return;
            sfItem.callItemHandler(ItemInteractHandler.class, handler -> handler.onInteract(e, sfItem));
        }
    }
}
