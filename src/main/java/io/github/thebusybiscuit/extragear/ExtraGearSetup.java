package io.github.thebusybiscuit.extragear;

import io.github.bakedlibs.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.utils.compatibility.VersionedEnchantment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * This class holds and registers all items and researches of "ExtraGear".
 *
 * <p>"ExtraGear" was originally a standalone addon created by TheBusyBiscuit, with a Chinese
 * translation by ybw0014. Both projects are licensed under GPL-3.0, and the addon has been
 * merged into this project so its items are available without installing an extra plugin.
 *
 * @author TheBusyBiscuit
 * @author ybw0014
 *
 * @see <a href="https://github.com/TheBusyBiscuit/ExtraGear">TheBusyBiscuit/ExtraGear</a>
 */
public final class ExtraGearSetup {

    /**
     * The original {@link NamespacedKey} namespace of the addon. It has to be kept as-is,
     * otherwise the research progress of existing players could be lost.
     */
    private static final String NAMESPACE = "extragear";

    private static final List<Research> researches = new ArrayList<>();

    private static int researchId = 3300;
    private static boolean registered = false;

    private ExtraGearSetup() {}

    /**
     * This method registers all items of "ExtraGear" into the given {@link ItemGroup}s.
     * The swords are registered in the weapons group, the armor in the armor group.
     * The researches are only created here, they get registered by {@link #setupResearches()}.
     *
     * @param plugin
     *            Our {@link Slimefun} instance
     * @param weaponsGroup
     *            The {@link ItemGroup} to register the swords in
     * @param armorGroup
     *            The {@link ItemGroup} to register the armor in
     */
    public static void setup(@Nonnull Slimefun plugin, @Nonnull ItemGroup weaponsGroup, @Nonnull ItemGroup armorGroup) {
        if (registered) {
            throw new UnsupportedOperationException("ExtraGear items can only be registered once!");
        }

        registered = true;

        if (Bukkit.getPluginManager().getPlugin("ExtraGear") != null) {
            plugin.getLogger()
                    .log(
                            Level.WARNING,
                            "Detected the standalone \"ExtraGear\" plugin, which has been merged into Slimefun already."
                                    + " Please remove it, otherwise its items will fail to register due to item id conflicts!");
        }

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "COPPER",
                SlimefunItems.COPPER_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.SMITE, 2)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.LEATHER,
                "COPPER",
                SlimefunItems.COPPER_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.BLAST_PROTECTION, 2)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "TIN",
                SlimefunItems.TIN_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.SHARPNESS, 1)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "TIN",
                SlimefunItems.TIN_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.BLAST_PROTECTION, 3)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "SILVER",
                SlimefunItems.SILVER_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.SHARPNESS, 2)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "SILVER",
                SlimefunItems.SILVER_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.PROTECTION, 2)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "ALUMINUM",
                SlimefunItems.ALUMINUM_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.BANE_OF_ARTHROPODS, 3)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "ALUMINUM",
                SlimefunItems.ALUMINUM_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.BLAST_PROTECTION, 2),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 2)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "LEAD",
                SlimefunItems.LEAD_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 3), new Pair<>(VersionedEnchantment.UNBREAKING, 8)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "LEAD",
                SlimefunItems.LEAD_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.PROTECTION, 3),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 8)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "ZINC",
                SlimefunItems.ZINC_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.SHARPNESS, 2)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "ZINC",
                SlimefunItems.ZINC_INGOT,
                Arrays.asList(new Pair<>(VersionedEnchantment.PROTECTION, 3)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "MAGNESIUM",
                SlimefunItems.MAGNESIUM_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 2), new Pair<>(VersionedEnchantment.UNBREAKING, 5)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "MAGNESIUM",
                SlimefunItems.MAGNESIUM_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.PROTECTION, 2),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 5)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "STEEL",
                SlimefunItems.STEEL_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 5), new Pair<>(VersionedEnchantment.UNBREAKING, 6)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "STEEL",
                SlimefunItems.STEEL_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.PROTECTION, 3),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 4)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "BRONZE",
                SlimefunItems.BRONZE_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 3), new Pair<>(VersionedEnchantment.UNBREAKING, 6)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "DURALUMIN",
                SlimefunItems.DURALUMIN_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 3), new Pair<>(VersionedEnchantment.UNBREAKING, 6)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "BILLON",
                SlimefunItems.BILLON_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 4), new Pair<>(VersionedEnchantment.UNBREAKING, 5)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "BRASS",
                SlimefunItems.BRASS_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SMITE, 4), new Pair<>(VersionedEnchantment.UNBREAKING, 6)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "ALUMINUM_BRASS",
                SlimefunItems.ALUMINUM_BRASS_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.BANE_OF_ARTHROPODS, 4),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 4)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "ALUMINUM_BRONZE",
                SlimefunItems.ALUMINUM_BRONZE_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.BANE_OF_ARTHROPODS, 4),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 5)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "CORINTHIAN_BRONZE",
                SlimefunItems.CORINTHIAN_BRONZE_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 5), new Pair<>(VersionedEnchantment.UNBREAKING, 5)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "SOLDER",
                SlimefunItems.SOLDER_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 4), new Pair<>(VersionedEnchantment.UNBREAKING, 6)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "DAMASCUS_STEEL",
                SlimefunItems.DAMASCUS_STEEL_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 6), new Pair<>(VersionedEnchantment.UNBREAKING, 7)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "HARDENED",
                SlimefunItems.HARDENED_METAL_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 7),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 10)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "REINFORCED",
                SlimefunItems.REINFORCED_ALLOY_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 8), new Pair<>(VersionedEnchantment.UNBREAKING, 8)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "FERROSILICON",
                SlimefunItems.FERROSILICON,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SMITE, 8), new Pair<>(VersionedEnchantment.UNBREAKING, 4)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.GOLDEN_SWORD,
                "GILDED_IRON",
                SlimefunItems.GILDED_IRON,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.BANE_OF_ARTHROPODS, 8),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 10)));
        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "NICKEL",
                SlimefunItems.NICKEL_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 6), new Pair<>(VersionedEnchantment.UNBREAKING, 5)));

        registerSword(
                plugin,
                weaponsGroup,
                Material.IRON_SWORD,
                "COBALT",
                SlimefunItems.COBALT_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.SHARPNESS, 7), new Pair<>(VersionedEnchantment.UNBREAKING, 7)));
        registerArmor(
                plugin,
                armorGroup,
                ArmorSet.IRON,
                "COBALT",
                SlimefunItems.COBALT_INGOT,
                Arrays.asList(
                        new Pair<>(VersionedEnchantment.PROTECTION, 7),
                        new Pair<>(VersionedEnchantment.UNBREAKING, 7)));
    }

    /**
     * This method registers all researches of "ExtraGear".
     * It has to be called after {@link #setup(Slimefun, ItemGroup, ItemGroup)}.
     */
    public static void setupResearches() {
        for (Research research : researches) {
            research.register();
        }

        researches.clear();
    }

    private static void registerSword(
            Slimefun plugin,
            ItemGroup itemGroup,
            Material type,
            String component,
            ItemStack item,
            List<Pair<Enchantment, Integer>> enchantments) {
        String humanizedComponent = humanize(component);
        SlimefunItemStack is = new SlimefunItemStack(component + "_SWORD", type, "&r" + humanizedComponent + "剑");

        for (Pair<Enchantment, Integer> enchantment : enchantments) {
            is.addUnsafeEnchantment(enchantment.getFirstValue(), enchantment.getSecondValue());
        }

        SlimefunItem slimefunItem =
                new SlimefunItem(itemGroup, is, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                    null, item, null, null, item, null, null, new ItemStack(Material.STICK), null
                });
        slimefunItem.register(plugin);

        researchId++;

        Research research = new Research(
                new NamespacedKey(NAMESPACE, component.toLowerCase(Locale.ROOT) + "_sword"),
                researchId,
                humanizedComponent + "剑",
                3);
        research.addItems(slimefunItem);
        researches.add(research);
    }

    private static void registerArmor(
            Slimefun plugin,
            ItemGroup itemGroup,
            ArmorSet armorset,
            String component,
            ItemStack item,
            List<Pair<Enchantment, Integer>> enchantments) {
        String humanizedComponent = humanize(component);
        SlimefunItemStack[] armor = {
            new SlimefunItemStack(component + "_HELMET", armorset.getHelmet(), "&f" + humanizedComponent + "头盔"),
            new SlimefunItemStack(
                    component + "_CHESTPLATE", armorset.getChestplate(), "&f" + humanizedComponent + "胸甲"),
            new SlimefunItemStack(component + "_LEGGINGS", armorset.getLeggings(), "&f" + humanizedComponent + "护腿"),
            new SlimefunItemStack(component + "_BOOTS", armorset.getBoots(), "&f" + humanizedComponent + "靴子")
        };

        for (Pair<Enchantment, Integer> enchantment : enchantments) {
            for (ItemStack is : armor) {
                is.addUnsafeEnchantment(enchantment.getFirstValue(), enchantment.getSecondValue());
            }
        }

        SlimefunItem helmet = new SlimefunItem(itemGroup, armor[0], RecipeType.ARMOR_FORGE, new ItemStack[] {
            item, item, item, item, null, item, null, null, null
        });
        helmet.register(plugin);

        SlimefunItem chestplate = new SlimefunItem(itemGroup, armor[1], RecipeType.ARMOR_FORGE, new ItemStack[] {
            item, null, item, item, item, item, item, item, item
        });
        chestplate.register(plugin);

        SlimefunItem leggings = new SlimefunItem(itemGroup, armor[2], RecipeType.ARMOR_FORGE, new ItemStack[] {
            item, item, item, item, null, item, item, null, item
        });
        leggings.register(plugin);

        SlimefunItem boots = new SlimefunItem(itemGroup, armor[3], RecipeType.ARMOR_FORGE, new ItemStack[] {
            null, null, null, item, null, item, item, null, item
        });
        boots.register(plugin);

        researchId++;

        Research research = new Research(
                new NamespacedKey(NAMESPACE, component.toLowerCase(Locale.ROOT) + "_armor"),
                researchId,
                humanizedComponent + "防具",
                5);
        research.addItems(helmet, chestplate, leggings, boots);
        researches.add(research);
    }

    /**
     * This method returns the Chinese name of the given metal component,
     * matching the names used in {@link SlimefunItems}.
     */
    private static String humanize(String component) {
        return switch (component) {
            case "COPPER" -> "铜";
            case "TIN" -> "锡";
            case "SILVER" -> "银";
            case "ALUMINUM" -> "铝";
            case "LEAD" -> "铅";
            case "ZINC" -> "锌";
            case "MAGNESIUM" -> "镁";
            case "STEEL" -> "钢";
            case "BRONZE" -> "青铜";
            case "DURALUMIN" -> "硬铝";
            case "BILLON" -> "银铜合金";
            case "BRASS" -> "黄铜";
            case "ALUMINUM_BRASS" -> "铝黄铜";
            case "ALUMINUM_BRONZE" -> "铝青铜";
            case "CORINTHIAN_BRONZE" -> "科林斯青铜";
            case "SOLDER" -> "焊锡";
            case "DAMASCUS_STEEL" -> "大马士革钢";
            case "HARDENED" -> "硬化金属";
            case "REINFORCED" -> "强化合金";
            case "FERROSILICON" -> "硅铁";
            case "GILDED_IRON" -> "镀金铁";
            case "NICKEL" -> "镍";
            case "COBALT" -> "钴";
            default -> component;
        };
    }
}
