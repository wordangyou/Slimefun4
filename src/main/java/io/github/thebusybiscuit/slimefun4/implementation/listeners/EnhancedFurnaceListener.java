package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import io.github.thebusybiscuit.cscorelib2.recipes.MinecraftRecipe;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.EnhancedFurnace;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class EnhancedFurnaceListener implements Listener {

    public EnhancedFurnaceListener(SlimefunPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBurn(FurnaceBurnEvent e) {
        SlimefunItem furnace = BlockStorage.check(e.getBlock());

        if (furnace instanceof EnhancedFurnace && ((EnhancedFurnace) furnace).getFuelEfficiency() > 0) {
            e.setBurnTime(((EnhancedFurnace) furnace).getFuelEfficiency() * e.getBurnTime());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSmelt(FurnaceSmeltEvent e) {
        SlimefunItem sfItem = BlockStorage.check(e.getBlock());

        if (sfItem instanceof EnhancedFurnace) {
            Furnace furnace = (Furnace) e.getBlock().getState();
            int amount = furnace.getInventory().getSmelting().getType().toString().endsWith("_ORE") ? ((EnhancedFurnace) sfItem).getOutput() : 1;
            Optional<ItemStack> result = Optional.ofNullable(furnace.getInventory().getResult());

            if (!result.isPresent()) {
                result = SlimefunPlugin.getMinecraftRecipes().getRecipeOutput(MinecraftRecipe.FURNACE, furnace.getInventory().getSmelting());
            }

            if (result.isPresent()) {
                ItemStack item = result.get();
                furnace.getInventory().setResult(new CustomItem(item, Math.min(item.getAmount() + amount, item.getMaxStackSize())));
            }
        }
    }

}