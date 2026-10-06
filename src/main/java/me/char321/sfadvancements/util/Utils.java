package me.char321.sfadvancements.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.Advancement;
import net.guizhanss.guizhanlib.minecraft.utils.compatibility.EnchantmentX;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@SuppressWarnings("unused")
public class Utils {
    /**
     * 本模块所有 NamespacedKey 的命名空间。
     *
     * 与合并前的独立插件保持一致，保证玩家进度数据和原版进度键兼容。
     */
    public static final String NAMESPACE = "slimefunadvancements";

    private Utils() {}

    public static ItemStack makeShiny(ItemStack item) {
        item = item.clone();
        ItemMeta im = item.getItemMeta();
        //noinspection DataFlowIssue
        im.addEnchant(EnchantmentX.UNBREAKING, 1, false);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(im);
        return item;
    }

    public static void makeShiny(ItemMeta im) {
        im.addEnchant(EnchantmentX.UNBREAKING, 1, false);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public static boolean keyIsSFA(NamespacedKey key) {
        return key.getNamespace().equals(NAMESPACE);
    }

    public static NamespacedKey keyOf(String value) {
        return new NamespacedKey(NAMESPACE, value.toLowerCase(Locale.ROOT));
    }

    /**
     * 解析配置中的 key：带命名空间时按原样解析，否则套用本模块命名空间。
     */
    public static NamespacedKey keyOfAllowNamespace(String value) {
        if (value.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(value);
            if (key != null) {
                return key;
            }
        }
        return keyOf(value);
    }

    public static Advancement fromKey(String value) {
        return SFAdvancements.getRegistry().getAdvancement(keyOf(value));
    }

    public static Advancement fromKey(NamespacedKey value) {
        return SFAdvancements.getRegistry().getAdvancement(value);
    }

    public static boolean isValidAdvancement(NamespacedKey key) {
        return SFAdvancements.getRegistry().getAdvancements().containsKey(key);
    }

    public static void listen(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, SFAdvancements.getPlugin());
    }

    public static Map<ItemStack, Integer> getContents(Inventory inv) {
        Map<ItemStack, Integer> contents = new HashMap<>();
        for (ItemStack item : inv) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            ItemStack clone = item.clone();
            clone.setAmount(1);
            contents.merge(clone, item.getAmount(), Integer::sum);
        }
        return contents;
    }

    public static TranslatableComponent getItemName(ItemStack item) {
        return getItemName(item.getType());
    }

    public static TranslatableComponent getItemName(Material type) {
        if (type.isBlock()) {
            return new TranslatableComponent("block.minecraft." + type.getKey().getKey());
        } else {
            return new TranslatableComponent("item.minecraft." + type.getKey().getKey());
        }
    }

    public static void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(SFAdvancements.getPlugin(), runnable);
    }

    public static void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(SFAdvancements.getPlugin(), runnable, delay);
    }
}
