package me.char321.sfadvancements.util;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.List;
import me.char321.sfadvancements.SFAdvancements;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfigUtils {
    private ConfigUtils() {}

    public static String translate(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static ItemStack getItem(ConfigurationSection config, String path) {
        if (config.isItemStack(path)) {
            return config.getItemStack(path);
        }

        ConfigurationSection itemSection = config.getConfigurationSection(path);
        if (itemSection == null) {
            String itemName = config.getString(path);
            if (itemName != null) {
                try {
                    return getTemplate(itemName).clone();
                } catch (IllegalArgumentException x) {
                    SFAdvancements.warn("无效的物品类型 " + itemName + "，已自动替换 (可能是这个附属没装?)");
                    ItemStack errItem = new ItemStack(Material.STRUCTURE_VOID);
                    ItemMeta meta = errItem.getItemMeta();
                    if (meta != null) {
                        List<String> lore = new java.util.ArrayList<>();
                        lore.add(ConfigUtils.translate("&c此进度所属的物品无效,可能服务器暂未安装相关附属"));
                        meta.setLore(lore);
                        errItem.setItemMeta(meta);
                    }
                    return errItem;
                }
            }
            return null;
        }

        String type = itemSection.getString("type");
        ItemStack item;
        boolean error = false;
        try {
            item = getTemplate(type).clone();
        } catch (IllegalArgumentException x) {
            SFAdvancements.warn("无效的物品类型 " + type + "，已自动替换。 (可能是这个附属没装?)");
            item = new ItemStack(Material.STRUCTURE_VOID);
            error = true;
        }

        ItemMeta im = item.getItemMeta();

        String name = itemSection.getString("name");
        if (name != null) {
            im.setDisplayName(translate(name));
        }

        List<String> lore = itemSection.getStringList("lore");
        lore.replaceAll(ConfigUtils::translate);
        if (error) {
            lore.add(0, ConfigUtils.translate("&c此进度所属的物品无效,可能服务器暂未安装相关附属"));
        }
        im.setLore(lore);

        item.setItemMeta(im);
        return item;
    }

    public static ItemStack getTemplate(String id) {
        if (id == null || id.equals("AIR") || id.equals("null")) {
            return new ItemStack(Material.AIR);
        }

        SlimefunItem item = SlimefunItem.getById(id);
        if (item != null) {
            return item.getItem();
        }

        Material material = Material.getMaterial(id);
        if (material != null) {
            return new ItemStack(material);
        }

        throw new IllegalArgumentException();
    }
}
