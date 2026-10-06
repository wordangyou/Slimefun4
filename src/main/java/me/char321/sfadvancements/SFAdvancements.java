package me.char321.sfadvancements;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.char321.sfadvancements.api.AdvancementBuilder;
import me.char321.sfadvancements.api.AdvancementGroup;
import me.char321.sfadvancements.api.criteria.CriteriaTypes;
import me.char321.sfadvancements.core.AdvManager;
import me.char321.sfadvancements.core.AdvancementsItemGroup;
import me.char321.sfadvancements.core.command.SFACommand;
import me.char321.sfadvancements.core.criteria.completer.CriterionCompleter;
import me.char321.sfadvancements.core.criteria.completer.DefaultCompleters;
import me.char321.sfadvancements.core.gui.AdvGUIManager;
import me.char321.sfadvancements.core.registry.AdvancementsRegistry;
import me.char321.sfadvancements.core.tasks.AutoSaveTask;
import me.char321.sfadvancements.util.ConfigUtils;
import me.char321.sfadvancements.util.Utils;
import me.char321.sfadvancements.vanilla.VanillaHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 粘液进度（SlimefunAdvancements）。
 *
 * 合并进 Slimefun 主体后本模块不再作为独立插件注册，
 * 由 {@link SFAdvancementsSetup} 在主插件启动 / 关闭时挂载。
 */
public final class SFAdvancements implements SlimefunAddon {
    private static SFAdvancements instance;

    /** Slimefun 主插件实例（合并后本模块的宿主） */
    private static Slimefun plugin;

    private final AdvManager advManager = new AdvManager();
    private final AdvGUIManager guiManager = new AdvGUIManager();
    private final AdvancementsRegistry registry = new AdvancementsRegistry();
    private final VanillaHook vanillaHook = new VanillaHook();

    /** 本模块的数据目录：plugins/Slimefun/sfadvancements */
    private final File dataFolder;

    private Config config;
    private YamlConfiguration advancementConfig;
    private YamlConfiguration groupConfig;

    private boolean multiBlockCraftEvent = false;

    public SFAdvancements(Slimefun plugin) {
        SFAdvancements.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "sfadvancements");
    }

    /**
     * 启动本模块（合并前为 onEnable）。
     */
    public void start() {
        instance = this;

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // 首次安装时释放默认配置（已有配置一律保留）
        saveEmbeddedResource("sfadvancements.yml", new File(plugin.getDataFolder(), "sfadvancements.yml"));
        config = new Config(new File(plugin.getDataFolder(), "sfadvancements.yml"));

        detectCapabilities();

        PluginCommand command = plugin.getCommand("sfadvancements");
        if (command != null) {
            command.setExecutor(new SFACommand());
        } else {
            getLogger().warning("无法注册 /sfadvancements 命令！");
        }

        // init gui
        Bukkit.getPluginManager().registerEvents(guiManager, plugin);

        // init sf
        AdvancementsItemGroup.init(this);

        // init core
        DefaultCompleters.registerDefaultCompleters();
        CriteriaTypes.loadDefaultCriteria();

        info("启动自动保存任务...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new AutoSaveTask(), 6000L, 6000L);

        // allow other plugins to register their criteria completers
        info("等待服务器启动中...");
        Utils.runLater(
                () -> {
                    info("正在从配置文件中加载进度组...");
                    loadGroups();
                    info("正在从配置文件中加载进度...");
                    loadAdvancements();

                    if (config.getBoolean("use-advancements-api")) {
                        vanillaHook.init();
                    }
                },
                0L);
    }

    /**
     * 关闭本模块（合并前为 onDisable）。
     *
     * 注意：定时任务由 Slimefun 主体统一取消，这里只负责保存数据。
     */
    public void stop() {
        try {
            advManager.save();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e, () -> "无法保存进度");
        }
    }

    private void detectCapabilities() {
        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.api.events.MultiBlockCraftEvent");
            multiBlockCraftEvent = true;
        } catch (ClassNotFoundException e) {
            multiBlockCraftEvent = false;
        }
    }

    public void reload() {
        config.reload();
        advManager.getPlayerMap().clear();
        registry.getAdvancements().clear();
        registry.getAdvancementGroups().clear();
        registry.getCompleters().values().forEach(CriterionCompleter::reload);

        loadGroups();
        loadAdvancements();

        if (config.getBoolean("use-advancements-api")) {
            vanillaHook.reload();
        }
    }

    public void loadGroups() {
        File groupFile = new File(dataFolder, "groups.yml");
        if (!groupFile.exists()) {
            saveEmbeddedResource("sfadvancements/groups.yml", groupFile);
        }
        groupConfig = YamlConfiguration.loadConfiguration(groupFile);
        for (String key : groupConfig.getKeys(false)) {
            String background = groupConfig.getString(key + ".background", "SLIME_BLOCK");
            ItemStack display = ConfigUtils.getItem(groupConfig, key + ".display");
            String frameType = groupConfig.getString(key + ".frame_type", "GOAL");
            AdvancementGroup group = new AdvancementGroup(key, display, frameType, background);
            group.register();
        }
    }

    public void loadAdvancements() {
        File advancementsFile = new File(dataFolder, "advancements.yml");
        if (!advancementsFile.exists()) {
            saveEmbeddedResource("sfadvancements/advancements.yml", advancementsFile);
        }
        advancementConfig = YamlConfiguration.loadConfiguration(advancementsFile);
        for (String key : advancementConfig.getKeys(false)) {
            AdvancementBuilder builder =
                    AdvancementBuilder.loadFromConfig(key, advancementConfig.getConfigurationSection(key));
            if (builder != null) {
                builder.register();
            }
        }
    }

    /**
     * 释放主体 jar 内的默认资源到指定文件（目标已存在则跳过）。
     */
    private void saveEmbeddedResource(String resourcePath, File target) {
        if (target.exists()) {
            return;
        }

        try (InputStream input = plugin.getResource(resourcePath)) {
            if (input == null) {
                return;
            }
            File parent = target.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.copy(input, target.toPath());
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "无法释放默认配置 " + resourcePath, e);
        }
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return plugin;
    }

    @Nullable @Override
    public String getBugTrackerURL() {
        return null;
    }

    public static SFAdvancements instance() {
        return instance;
    }

    /**
     * 获取 Slimefun 主插件实例。
     */
    public static Slimefun getPlugin() {
        return plugin;
    }

    public static AdvManager getAdvManager() {
        return instance.advManager;
    }

    public static AdvGUIManager getGuiManager() {
        return instance.guiManager;
    }

    public static AdvancementsRegistry getRegistry() {
        return instance.registry;
    }

    public static VanillaHook getVanillaHook() {
        return instance.vanillaHook;
    }

    public static Config getMainConfig() {
        return instance.config;
    }

    public YamlConfiguration getAdvancementConfig() {
        return advancementConfig;
    }

    public YamlConfiguration getGroupsConfig() {
        return groupConfig;
    }

    public boolean isMultiBlockCraftEvent() {
        return multiBlockCraftEvent;
    }

    /**
     * 本模块的数据目录：plugins/Slimefun/sfadvancements
     */
    public File getDataFolder() {
        return dataFolder;
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public static Logger logger() {
        return plugin.getLogger();
    }

    public static void info(String msg) {
        plugin.getLogger().info(msg);
    }

    public static void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

    public static void error(String msg) {
        plugin.getLogger().severe(msg);
    }
}
