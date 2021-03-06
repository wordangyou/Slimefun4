package io.github.thebusybiscuit.slimefun4.implementation.items.multiblocks;

import io.github.thebusybiscuit.cscorelib2.inventory.ItemUtils;
import io.github.thebusybiscuit.cscorelib2.item.CustomItem;
import me.mrCookieSlime.Slimefun.Lists.Categories;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.multiblocks.MultiBlockMachine;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArmorForge extends MultiBlockMachine {

    public ArmorForge() {
        super(Categories.MACHINES_1, (SlimefunItemStack) SlimefunItems.ARMOR_FORGE, new ItemStack[]{
                null, null, null,
                null, new ItemStack(Material.ANVIL), null,
                null, new CustomItem(Material.DISPENSER, "Dispenser (Facing up)"), null
        }, new ItemStack[0], BlockFace.SELF);
    }

    @Override
    public void onInteract(Player p, Block b) {
        Block dispBlock = b.getRelative(BlockFace.DOWN);
        Dispenser disp = (Dispenser) dispBlock.getState();
        Inventory inv = disp.getInventory();
        List<ItemStack[]> inputs = RecipeType.getRecipeInputList(this);

        for (ItemStack[] input : inputs) {
            if (isCraftable(inv, input)) {
                ItemStack output = RecipeType.getRecipeOutputList(this, input).clone();

                if (Slimefun.hasUnlocked(p, output, true)) {
                    Inventory outputInv = findOutputInventory(output, dispBlock, inv);

                    if (outputInv != null) {
                        craft(p, output, inv, outputInv);
                    } else SlimefunPlugin.getLocal().sendMessage(p, "machines.full-inventory", true);
                }
                return;
            }
        }

        SlimefunPlugin.getLocal().sendMessage(p, "machines.pattern-not-found", true);
    }

    private boolean isCraftable(Inventory inv, ItemStack[] recipe) {
        for (int j = 0; j < inv.getContents().length; j++) {
            if (!SlimefunManager.isItemSimilar(inv.getContents()[j], recipe[j], true)) {
                return false;
            }
        }

        return true;
    }

    private void craft(Player p, ItemStack output, Inventory inv, Inventory outputInv) {
        for (int j = 0; j < 9; j++) {
            ItemStack item = inv.getContents()[j];
            if (item != null && item.getType() != Material.AIR) {
                ItemUtils.consumeItem(item, true);
            }
        }

        for (int j = 0; j < 4; j++) {
            int current = j;

            Bukkit.getScheduler().runTaskLater(SlimefunPlugin.instance, () -> {
                if (current < 3) {
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1F, 2F);
                } else {
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1F, 1F);
                    outputInv.addItem(output);
                }
            }, j * 20L);
        }
    }

}