package com.wordangyou.yonglenuclearexplosion;

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockBreakEvent;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunBlockPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.reactors.Reactor;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NuclearReactorWither implements Listener {

    private final JavaPlugin plugin;
    private final ReactorDataStore dataStore;

    /**
     * 当前已知的反应堆。
     *
     * Key：
     * 世界 UUID + 坐标
     *
     * Value：
     * Location
     */
    private final Map<String, Location> reactors = new ConcurrentHashMap<>();

    /**
     * 启动后等待 Slimefun 恢复 BlockStorage 的时间。
     *
     * 100 ticks = 5 秒
     */
    private static final long INITIAL_LOAD_DELAY = 100L;

    public NuclearReactorWither(JavaPlugin plugin, ReactorDataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
    }

    /**
     * 启动反应堆凋零检测。
     */
    public void start() {

        if (!plugin.getConfig().getBoolean("reactor-wither.enabled", true)) {

            plugin.getLogger().info("核反应堆凋零效果已关闭。");

            return;
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);

        long interval = plugin.getConfig().getLong("reactor-wither.check-interval-ticks", 10L);

        if (interval <= 0) {
            interval = 10L;
        }

        /*
         * =====================================================
         * 重要：
         *
         * 不再启动后立即检查 Reactor。
         *
         * Slimefun 的 BlockStorage 可能还没有恢复完成。
         *
         * 所以先等待 5 秒。
         * =====================================================
         */
        Bukkit.getScheduler().runTaskLater(plugin, this::loadPersistedReactors, INITIAL_LOAD_DELAY);

        /*
         * =====================================================
         * 定时检查
         *
         * 但是同样不会在最开始立刻执行。
         *
         * 第一次检查由 loadPersistedReactors()
         * 完成。
         *
         * 后面的检查由这个定时任务负责。
         * =====================================================
         */
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, INITIAL_LOAD_DELAY + interval, interval);

        plugin.getLogger().info("核反应堆凋零效果已启动。");

        plugin.getLogger().info("等待 Slimefun 恢复反应堆数据后开始检测。");
    }

    /**
     * 从 reactors.yml 恢复所有反应堆。
     *
     * 注意：
     *
     * 这里绝对不能因为暂时检查不到 Reactor
     * 就直接删除持久化记录。
     */
    private void loadPersistedReactors() {

        List<ReactorDataStore.ReactorData> dataList = dataStore.getAll();

        reactors.clear();

        int loaded = 0;

        for (ReactorDataStore.ReactorData data : dataList) {

            Location location = dataStore.toLocation(data);

            if (location == null) {
                /*
                 * 世界当前还没有加载。
                 *
                 * 不删除数据。
                 */
                continue;
            }

            String key = getLocationKey(location);

            reactors.put(key, location.clone());

            loaded++;
        }

        plugin.getLogger().info("已恢复 " + loaded + " 个核反应堆凋零检测记录。");

        /*
         * 第一次加载完成后立即检查一次。
         */
        tick();
    }

    // ========================================================
    // 反应堆放置
    // ========================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReactorPlace(SlimefunBlockPlaceEvent event) {

        if (!(event.getSlimefunItem() instanceof Reactor)) {

            return;
        }

        Location location = event.getBlockPlaced().getLocation().clone();

        UUID owner = event.getPlayer().getUniqueId();

        String key = getLocationKey(location);

        reactors.put(key, location);

        /*
         * 保存：
         *
         * 世界
         * X/Y/Z
         * Reactor Owner
         */
        dataStore.register(location, owner);

        plugin.getLogger()
                .info("已记录核反应堆：" + location + " | Owner: " + event.getPlayer().getName());
    }

    // ========================================================
    // 反应堆拆除
    // ========================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onReactorBreak(SlimefunBlockBreakEvent event) {

        if (!(event.getSlimefunItem() instanceof Reactor)) {

            return;
        }

        Location location = event.getBlockBroken().getLocation();

        String key = getLocationKey(location);

        reactors.remove(key);

        /*
         * 只有真正收到 Slimefun 的拆除事件时，
         * 才删除持久化记录。
         */
        dataStore.remove(location);

        plugin.getLogger().info("已移除核反应堆记录：" + location);
    }

    // ========================================================
    // 定时检查
    // ========================================================

    private void tick() {

        if (!plugin.getConfig().getBoolean("reactor-wither.enabled", true)) {

            return;
        }

        Location[] reactorLocations = reactors.values().toArray(new Location[0]);

        for (Location location : reactorLocations) {

            if (location == null) {
                continue;
            }

            World world = location.getWorld();

            /*
             * 世界还没加载：
             *
             * 不删除。
             */
            if (world == null) {
                continue;
            }

            /*
             * =================================================
             * 关键修改
             *
             * 如果这里暂时检查不到 Reactor，
             * 不再直接从 reactors 中删除。
             *
             * 因为：
             *
             * 服务器重启时 Slimefun BlockStorage
             * 可能比本插件晚恢复。
             * =================================================
             */

            if (!isReactor(location)) {
                continue;
            }

            /*
             * Reactor 存在，但是没有正在运行的机器操作。
             *
             * 不施加凋零。
             */
            if (!isWorking(location)) {
                continue;
            }

            /*
             * Reactor 正在工作。
             *
             * 施加凋零。
             */
            applyWither(location);
        }
    }

    // ========================================================
    // 判断是否为 Reactor
    // ========================================================

    private boolean isReactor(Location location) {

        try {

            SlimefunItem item = BlockStorage.check(location);

            return item instanceof Reactor;

        } catch (Throwable ignored) {

            /*
             * API 暂时不可用时：
             *
             * 不删除记录。
             */
            return false;
        }
    }

    // ========================================================
    // 判断 Reactor 是否正在工作
    // ========================================================

    private boolean isWorking(Location location) {

        try {

            SlimefunItem item = BlockStorage.check(location);

            if (!(item instanceof Reactor reactor)) {
                return false;
            }

            return reactor.getMachineProcessor().getOperation(location) != null;

        } catch (Throwable ignored) {

            return false;
        }
    }

    // ========================================================
    // 应用凋零
    // ========================================================

    private void applyWither(Location reactorLocation) {

        World world = reactorLocation.getWorld();

        if (world == null) {
            return;
        }

        double radius = plugin.getConfig().getDouble("reactor-wither.radius", 10.0);

        if (radius <= 0) {
            return;
        }

        int duration = plugin.getConfig().getInt("reactor-wither.duration-ticks", 40);

        if (duration <= 0) {
            return;
        }

        int amplifier = plugin.getConfig().getInt("reactor-wither.amplifier", 0);

        if (amplifier < 0) {
            amplifier = 0;
        }

        double radiusSquared = radius * radius;

        for (LivingEntity entity : world.getNearbyEntities(reactorLocation, radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .toList()) {

            if (entity.isDead()) {
                continue;
            }

            double distanceSquared = entity.getLocation().distanceSquared(reactorLocation);

            if (distanceSquared > radiusSquared) {

                continue;
            }

            entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, amplifier, true, true, true));
        }
    }

    // ========================================================
    // Location Key
    // ========================================================

    private String getLocationKey(Location location) {

        World world = location.getWorld();

        if (world == null) {

            return "unknown:" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        }

        return world.getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    // ========================================================
    // 清理内存
    // ========================================================

    public void clear() {
        reactors.clear();
    }
}
