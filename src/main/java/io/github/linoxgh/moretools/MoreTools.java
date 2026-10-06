package io.github.linoxgh.moretools;

import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.linoxgh.moretools.items.CrescentHammer;
import io.github.linoxgh.moretools.listeners.PlayerListener;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 更多工具的合并入口。
 *
 * 合并后本模块不再作为独立插件注册，
 * 而是由 {@link io.github.linoxgh.moretools.MoreToolsSetup} 挂载到 Slimefun 主体中。
 *
 * @author Linox
 */
public class MoreTools implements SlimefunAddon {

    /**
     * 本附属所有 NamespacedKey 的命名空间。与合并前的独立插件保持一致，保证物品组、研究等数据兼容。
     */
    private static final String NAMESPACE = "moretools";

    private static MoreTools instance;

    /**
     * Slimefun 主插件实例（合并后本模块的宿主）
     */
    private static Slimefun plugin;

    /**
     * 本模块的配置文件：plugins/Slimefun/moretools.yml
     */
    private final File configFile;

    private YamlConfiguration config;

    private ItemGroup moreToolsItemGroup;
    private boolean debug = true;

    public MoreTools(Slimefun plugin) {
        MoreTools.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "moretools.yml");
    }

    /**
     * 启动本模块。
     */
    public void start() {
        instance = this;

        loadConfig();
        debug = config.getBoolean("options.debugging", true);

        if (debug) plugin.getLogger().info("正在注册监听器...");
        new PlayerListener();

        setupCategories();
        setupItems();
        setupResearches();
    }

    /**
     * 关闭本模块。
     */
    public void stop() {
        instance = null;
    }

    /**
     * 加载配置：文件不存在时从主体 jar 释放默认配置，并补齐缺失的配置项
     * （等价于原版独立插件的配置升级逻辑）。
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            saveEmbeddedResource();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        YamlConfiguration defaults = loadEmbeddedDefaults();
        if (defaults == null) {
            return;
        }

        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (!config.contains(key, true)) {
                if (config.getBoolean("options.debugging", true)) {
                    plugin.getLogger().info("设置 \"" + key + "\" 为 \"" + defaults.get(key) + "\"。");
                }
                config.set(key, defaults.get(key));
                changed = true;
            }
        }

        if (changed) {
            try {
                config.save(configFile);
                plugin.getLogger().info("已补齐 moretools.yml 中缺失的配置项。");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "保存 moretools.yml 文件失败。", e);
            }
        }
    }

    /**
     * 释放主体 jar 内的默认配置到指定文件。
     */
    private void saveEmbeddedResource() {
        try (InputStream input = plugin.getResource("moretools.yml")) {
            if (input == null) {
                return;
            }
            Files.copy(input, configFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法释放默认配置 moretools.yml", e);
        }
    }

    /**
     * 读取主体 jar 内的默认配置，用于补齐缺失的配置项。
     */
    private YamlConfiguration loadEmbeddedDefaults() {
        try (InputStream input = plugin.getResource("moretools.yml")) {
            if (input == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法读取默认配置 moretools.yml", e);
            return null;
        }
    }

    private void setupCategories() {
        if (debug) plugin.getLogger().info("正在初始化分类...");

        moreToolsItemGroup = new ItemGroup(
                new NamespacedKey(NAMESPACE, "more_tools_category"),
                new CustomItemStack(Items.CRESCENT_HAMMER, "&3更多工具"),
                4);
    }

    private void setupItems() {
        if (debug) plugin.getLogger().info("正在初始化物品...");

        new CrescentHammer(
                        moreToolsItemGroup, Items.CRESCENT_HAMMER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                            SlimefunItems.TIN_INGOT,
                            null,
                            SlimefunItems.TIN_INGOT,
                            null,
                            SlimefunItems.COPPER_INGOT,
                            null,
                            null,
                            SlimefunItems.TIN_INGOT,
                            null
                        })
                .register(this);
    }

    private void setupResearches() {
        if (debug) plugin.getLogger().info("正在初始化研究...");

        registerResearch("crescent_hammer", 7501, "是个锤子", 15, Items.CRESCENT_HAMMER);
    }

    private void registerResearch(String key, int id, String name, int defaultCost, ItemStack... items) {
        Research research = new Research(new NamespacedKey(NAMESPACE, key), id, name, defaultCost);

        for (ItemStack item : items) {
            SlimefunItem sfItem = SlimefunItem.getByItem(item);
            if (sfItem != null) {
                research.addItems(sfItem);
            }
        }
        research.register();
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunGuguProject/MoreTools/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return plugin;
    }

    public static MoreTools getInstance() {
        return instance;
    }

    /**
     * 获取 Slimefun 主插件实例。
     */
    public static Slimefun getPlugin() {
        return plugin;
    }

    /**
     * 本模块的配置。
     */
    public YamlConfiguration getConfig() {
        return config;
    }

    public boolean debug() {
        return debug;
    }
}
