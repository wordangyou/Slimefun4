package com.wordangyou.yonglenuclearexplosion;

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.reactors.Reactor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class YongleNuclearExplosion implements Listener {

    /**
     * Slimefun 主体实例
     */
    private final Slimefun plugin;

    /**
     * 本系统的数据文件夹
     *
     * plugins/Slimefun/nuclear-explosion
     */
    private final File dataFolder;

    /**
     * 本系统的配置
     *
     * plugins/Slimefun/nuclear-explosion.yml
     */
    private YamlConfiguration config;

    /**
     * Reactor 持久化数据
     */
    private ReactorDataStore reactorDataStore;

    /**
     * 核爆历史记录
     */
    private NuclearExplosionHistory nuclearHistory;

    /**
     * 核反应堆凋零效果
     */
    private NuclearReactorWither nuclearReactorWither;

    public YongleNuclearExplosion(Slimefun plugin) {
        this.plugin = plugin;

        this.dataFolder = new File(plugin.getDataFolder(), "nuclear-explosion");
    }

    public void start() {

        // ====================================================
        // 加载配置
        // ====================================================

        saveDefaultConfig();

        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "nuclear-explosion.yml"));

        // ====================================================
        // 初始化 Reactor 持久化
        // ====================================================

        reactorDataStore = new ReactorDataStore(this);

        reactorDataStore.load();

        // ====================================================
        // 初始化核爆历史
        // ====================================================

        int maxHistory = getConfig().getInt("undo.max-history", 5);

        nuclearHistory = new NuclearExplosionHistory(this, maxHistory);

        // ====================================================
        // 注册核爆监听器
        // ====================================================

        getServer().getPluginManager().registerEvents(new NuclearExplosionListener(this), plugin);

        // ====================================================
        // 注册核反应堆警告
        // ====================================================

        getServer().getPluginManager().registerEvents(new NuclearReactorWarning(plugin), plugin);

        // ====================================================
        // 注册本类事件
        //
        // 用于记录 Reactor 所有者
        // ====================================================

        getServer().getPluginManager().registerEvents(this, plugin);

        // ====================================================
        // 启动核反应堆凋零系统
        // ====================================================

        nuclearReactorWither = new NuclearReactorWither(plugin, reactorDataStore);

        nuclearReactorWither.start();

        // ====================================================
        // 注册 /ynundo
        // ====================================================

        if (plugin.getCommand("ynundo") != null) {

            plugin.getCommand("ynundo").setExecutor(new NuclearUndoCommand(this));

        } else {

            getLogger().warning("无法注册 /ynundo！");
        }

        // ====================================================
        // 启动完成
        // ====================================================

        getLogger().info("========================================");

        getLogger().info("YongleNuclearExplosion 已启动！");

        getLogger().info("已加载 Reactor：" + reactorDataStore.size());

        getLogger().info("已加载核爆记录：" + nuclearHistory.size());

        getLogger().info("核爆撤回记录上限：" + maxHistory);

        getLogger().info("核反应堆凋零效果：" + (getConfig().getBoolean("reactor-wither.enabled", true) ? "已启用" : "已关闭"));

        getLogger().info("========================================");
    }

    public void stop() {

        // ====================================================
        // 保存 Reactor
        // ====================================================

        if (reactorDataStore != null) {

            reactorDataStore.save();
        }

        // ====================================================
        // 清理凋零系统内存
        //
        // 不删除 reactors.yml
        // ====================================================

        if (nuclearReactorWither != null) {

            nuclearReactorWither.clear();
        }

        // ====================================================
        // 清理核爆历史内存
        //
        // 不删除 history/*.yml
        // ====================================================

        if (nuclearHistory != null) {

            nuclearHistory.clear();
        }

        getLogger().info("YongleNuclearExplosion 已关闭！");
    }

    /**
     * Reactor 放置事件。
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSlimefunBlockPlace(SlimefunBlockPlaceEvent event) {

        if (!(event.getSlimefunItem() instanceof Reactor)) {

            return;
        }

        Location location = event.getBlockPlaced().getLocation();

        if (location == null) {
            return;
        }

        UUID player = event.getPlayer().getUniqueId();

        if (player == null) {
            return;
        }

        reactorDataStore.register(location, player);

        getLogger().info("记录核反应堆所有者：" + event.getPlayer().getName() + " | " + location);
    }

    /**
     * 获取 Reactor 所有者。
     */
    public UUID getReactorOwner(Location location) {

        if (location == null || reactorDataStore == null) {

            return null;
        }

        return reactorDataStore.getOwner(location);
    }

    /**
     * 删除 Reactor 所有者记录。
     */
    public void removeReactorOwner(Location location) {

        if (location == null || reactorDataStore == null) {

            return;
        }

        reactorDataStore.remove(location);
    }

    public ReactorDataStore getReactorDataStore() {

        return reactorDataStore;
    }

    public NuclearExplosionHistory getNuclearHistory() {

        return nuclearHistory;
    }

    public NuclearReactorWither getNuclearReactorWither() {

        return nuclearReactorWither;
    }

    /**
     * 获取 Slimefun 主插件实例。
     */
    public JavaPlugin getJavaPlugin() {

        return plugin;
    }

    /**
     * 获取本系统的数据文件夹。
     */
    public File getDataFolder() {

        return dataFolder;
    }

    /**
     * 获取本系统的配置。
     */
    public YamlConfiguration getConfig() {

        return config;
    }

    /**
     * 将本系统的默认配置文件释放到磁盘。
     */
    public void saveDefaultConfig() {

        File file = new File(plugin.getDataFolder(), "nuclear-explosion.yml");

        if (file.exists()) {
            return;
        }

        try (InputStream input = plugin.getResource("nuclear-explosion.yml")) {

            if (input != null) {

                Files.copy(input, file.toPath());
            }

        } catch (IOException e) {

            getLogger().warning("无法释放默认配置 nuclear-explosion.yml：" + e.getMessage());
        }
    }

    /**
     * 获取 Slimefun 的 Logger。
     */
    public Logger getLogger() {

        return plugin.getLogger();
    }

    /**
     * 获取服务器实例。
     */
    public Server getServer() {

        return plugin.getServer();
    }
}
