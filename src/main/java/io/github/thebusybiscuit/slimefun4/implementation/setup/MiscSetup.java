package io.github.thebusybiscuit.slimefun4.implementation.setup;

import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.AutomatedCraftingChamber;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItemSerializer;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItemSerializer.ItemFlag;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.Alloy;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunMachine;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class MiscSetup {

    private MiscSetup() {}

    public static void setupMisc() {
        SlimefunItem talisman = SlimefunItem.getByID("COMMON_TALISMAN");

        if (talisman != null && (boolean) Slimefun.getItemValue(talisman.getID(), "recipe-requires-nether-stars")) {
            talisman.setRecipe(new ItemStack[]{
                    SlimefunItems.MAGIC_LUMP_2, SlimefunItems.GOLD_8K, SlimefunItems.MAGIC_LUMP_2,
                    null, new ItemStack(Material.NETHER_STAR), null,
                    SlimefunItems.MAGIC_LUMP_2, SlimefunItems.GOLD_8K, SlimefunItems.MAGIC_LUMP_2
            });
        }

        Slimefun.getLogger().log(Level.INFO, "正在加载 Wiki 页面...");
        WikiSetup.addWikiPages(SlimefunPlugin.instance);
    }

    public static void loadItems() {
        Iterator<SlimefunItem> iterator = SlimefunPlugin.getRegistry().getEnabledSlimefunItems().iterator();

        while (iterator.hasNext()) {
            SlimefunItem item = iterator.next();

            if (item == null) {
                Slimefun.getLogger().log(Level.WARNING, "Removed bugged Item ('NULL?')");
                iterator.remove();
            } else if (item.getItem() == null) {
                Slimefun.getLogger().log(Level.WARNING, "Removed bugged Item ('" + item.getID() + "')");
                iterator.remove();
            }
        }

        List<SlimefunItem> pre = new ArrayList<>();
        List<SlimefunItem> init = new ArrayList<>();
        List<SlimefunItem> post = new ArrayList<>();

        for (SlimefunItem item : SlimefunPlugin.getRegistry().getEnabledSlimefunItems()) {
            if (item instanceof Alloy) pre.add(item);
            else if (item instanceof SlimefunMachine) init.add(item);
            else post.add(item);
        }

        for (SlimefunItem item : pre) {
            item.load();
        }

        for (SlimefunItem item : init) {
            item.load();
        }

        for (SlimefunItem item : post) {
            item.load();
        }

        AutomatedCraftingChamber crafter = (AutomatedCraftingChamber) SlimefunItem.getByID("AUTOMATED_CRAFTING_CHAMBER");

        if (crafter != null) {
            SlimefunMachine machine = (SlimefunMachine) SlimefunItem.getByID("ENHANCED_CRAFTING_TABLE");

            for (ItemStack[] inputs : RecipeType.getRecipeInputList(machine)) {
                StringBuilder builder = new StringBuilder();
                int i = 0;

                for (ItemStack item : inputs) {
                    if (i > 0) {
                        builder.append(" </slot> ");
                    }

                    builder.append(CustomItemSerializer.serialize(item, ItemFlag.MATERIAL, ItemFlag.ITEMMETA_DISPLAY_NAME, ItemFlag.ITEMMETA_LORE));

                    i++;
                }

                SlimefunPlugin.getRegistry().getAutomatedCraftingChamberRecipes().put(builder.toString(), RecipeType.getRecipeOutputList(machine, inputs));
            }

        }

        List<ItemStack[]> grinderRecipes = new ArrayList<>();

        SlimefunItem grinder = SlimefunItem.getByID("GRIND_STONE");
        if (grinder != null) {
            ItemStack[] input = null;

            for (ItemStack[] recipe : ((SlimefunMachine) grinder).getRecipes()) {
                if (input == null) {
                    input = recipe;
                }
                else {
                    if (input[0] != null && recipe[0] != null) {
                        grinderRecipes.add(new ItemStack[] {input[0], recipe[0]});
                    }
                    input = null;
                }
            }
        }

        SlimefunItem crusher = SlimefunItem.getByID("ORE_CRUSHER");
        if (crusher != null) {
            ItemStack[] input = null;

            for (ItemStack[] recipe : ((SlimefunMachine) crusher).getRecipes()) {
                if (input == null) {
                    input = recipe;
                }
                else {
                    if (input[0] != null && recipe[0] != null) {
                        grinderRecipes.add(new ItemStack[] {input[0], recipe[0]});
                    }
                    input = null;
                }
            }
        }

        // Favour 8 Cobblestone -> 1 Sand Recipe over 1 Cobblestone -> 1 Gravel Recipe
        Stream<ItemStack[]> stream = grinderRecipes.stream();

        if (!SlimefunPlugin.getCfg().getBoolean("options.legacy-ore-grinder")) {
            stream = stream.sorted((a, b) -> Integer.compare(b[0].getAmount(), a[0].getAmount()));
        }

        stream.forEach(recipe -> registerMachineRecipe("ELECTRIC_ORE_GRINDER", 4, new ItemStack[] {recipe[0]}, new ItemStack[] {recipe[1]}));

        SlimefunItem smeltery = SlimefunItem.getByID("SMELTERY");
        if (smeltery != null) {
            ItemStack[] input = null;

            for (ItemStack[] recipe : ((SlimefunMachine) smeltery).getRecipes()) {
                if (input == null) {
                    input = recipe;
                }
                else {
                    if (input[0] != null && recipe[0] != null) {
                        List<ItemStack> inputs = new ArrayList<>();
                        boolean dust = false;

                        for (ItemStack item : input) {
                            if (item != null) {
                                inputs.add(item);
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.ALUMINUM_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.COPPER_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.GOLD_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.IRON_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.LEAD_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.MAGNESIUM_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.SILVER_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.TIN_DUST, true)) dust = true;
                                if (SlimefunManager.isItemSimilar(item, SlimefunItems.ZINC_DUST, true)) dust = true;
                            }
                        }

                        // We want to exclude Dust to Ingot Recipes
                        if (!(dust && inputs.size() == 1)) {
                            registerMachineRecipe("ELECTRIC_SMELTERY", 12, inputs.toArray(new ItemStack[0]), new ItemStack[] {recipe[0]});
                        }
                    }

                    input = null;
                }
            }
        }

        CommandSender sender = Bukkit.getConsoleSender();

        int total = SlimefunPlugin.getRegistry().getEnabledSlimefunItems().size();
        int vanilla = SlimefunPlugin.getRegistry().countVanillaItems();


        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "######################### - Slimefun v" + SlimefunPlugin.getVersion() + " - #########################");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "成功载入了 " + total + " 个物品和" + SlimefunPlugin.getRegistry().getResearches().size() + " 个研究)");
        sender.sendMessage(ChatColor.GREEN + "( " + vanilla + " 物品来自 Slimefun, " + (total - vanilla) + " 物品来自 " + SlimefunPlugin.getInstalledAddons().size() + " 个扩展 )");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Slimefun 是一个由社区开发者维护的开源项目!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + " -- 源代码:   https://github.com/StarWishsama/Slimefun4");
        sender.sendMessage(ChatColor.GREEN + " -- Wiki:   https://github.com/TheBusyBiscuit/Slimefun4/wiki");
        sender.sendMessage(ChatColor.GREEN + " -- Bug 反馈:   https://github.com/StarWishsama/Slimefun4/issues");
        sender.sendMessage("");

        SlimefunPlugin.getItemCfg().save();
        SlimefunPlugin.getResearchCfg().save();
        SlimefunPlugin.getWhitelist().save();
    }

    private static void registerMachineRecipe(String machine, int seconds, ItemStack[] input, ItemStack[] output) {
        for (SlimefunItem item : SlimefunPlugin.getRegistry().getEnabledSlimefunItems()) {
            if (item instanceof AContainer && ((AContainer) item).getMachineIdentifier().equals(machine)) {
                ((AContainer) item).registerRecipe(seconds, input, output);
            }
        }
    }

    public static void setupItemSettings() {
        for (World world : Bukkit.getWorlds()) {
            SlimefunPlugin.getWhitelist().setDefaultValue(world.getName() + ".enabled-items.SLIMEFUN_GUIDE", true);
        }

        Slimefun.setItemVariable("ORE_CRUSHER", "double-ores", true);

        for (Enchantment enchantment : Enchantment.values()) {
            for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
                Slimefun.setItemVariable("MAGICIAN_TALISMAN", "allow-enchantments." + enchantment.getKey().getKey() + ".level." + i, true);
            }
        }
    }
}