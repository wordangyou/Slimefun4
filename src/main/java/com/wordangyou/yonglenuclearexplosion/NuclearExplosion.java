package com.wordangyou.yonglenuclearexplosion;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class NuclearExplosion {

    /**
     * Residence 反射调用句柄
     *
     * 不对 Residence 产生编译期依赖
     */
    private static Method residenceGetInstance;

    private static Method residenceGetResidenceManager;

    private static Method residenceManagerGetByLoc;

    private static Method claimedResidenceGetOwner;

    private static boolean residenceReflectionFailed = false;

    private final YongleNuclearExplosion plugin;

    public NuclearExplosion(YongleNuclearExplosion plugin) {
        this.plugin = plugin;
    }

    /**
     * 核爆掉落物
     */
    private record NuclearDrop(Location location, ItemStack item) {}

    /**
     * 执行核爆
     *
     * 如果服务器安装 WorldEdit，
     * 使用普通 WorldEdit EditSession 批量删除方块；
     *
     * 如果服务器未安装 WorldEdit，
     * 自动降级为逐个方块删除。
     *
     * 注意：
     * 如果服务器同时安装 FAWE，
     * WorldEdit 的 EditSession 仍可能被 FAWE 接管。
     */
    public void explode(Location center, UUID reactorOwner) {

        if (center == null) {
            return;
        }

        World world = center.getWorld();

        if (world == null) {
            return;
        }

        int radius = plugin.getConfig().getInt("explosion.radius", 15);

        if (radius <= 0) {
            return;
        }

        // ====================================================
        // 播放核爆效果
        // ====================================================

        playGlobalExplosionEffects(center);

        // ====================================================
        // 是否破坏方块
        // ====================================================

        boolean breakBlocks = plugin.getConfig().getBoolean("explosion.break-blocks", true);

        /*
         * 当前诊断版本暂时保留 drops，
         * 但是不会调用 collectDrops()。
         *
         * 这样可以完全排除：
         *
         * BlockStorage.retrieve(block)
         *
         * 对本次测试的影响。
         */
        List<NuclearDrop> drops = new ArrayList<>();

        List<int[]> destroyBlocks = new ArrayList<>();

        /*
         * 核爆快照。
         *
         * 必须在方块被删除之前 capture。
         */
        NuclearExplosionSnapshot snapshot = breakBlocks ? new NuclearExplosionSnapshot(UUID.randomUUID()) : null;

        int radiusSquared = radius * radius;

        int scannedBlocks = 0;
        int protectedBlocks = 0;
        int destroyedBlocks = 0;

        // ====================================================
        // 扫描核爆范围
        // ====================================================

        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    if ((x * x) + (y * y) + (z * z) > radiusSquared) {

                        continue;
                    }

                    Block block =
                            world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);

                    scannedBlocks++;

                    // ====================================================
                    // 空气
                    // ====================================================

                    if (block.getType().isAir()) {
                        continue;
                    }

                    // ====================================================
                    // 领地保护
                    // ====================================================

                    if (isProtected(block.getLocation(), reactorOwner)) {

                        protectedBlocks++;
                        continue;
                    }

                    // ====================================================
                    // 基岩永远不炸
                    // ====================================================

                    if (block.getType() == Material.BEDROCK) {

                        continue;
                    }

                    // ====================================================
                    // 保存核爆前方块状态
                    // ====================================================

                    if (breakBlocks && snapshot != null) {

                        snapshot.capture(block);
                    }

                    /*
                     * ====================================================
                     * 当前诊断版本不收集掉落
                     * ====================================================
                     *
                     * 原来的：
                     *
                     * collectDrops(block, drops);
                     *
                     * 当前暂时完全不执行。
                     *
                     * 因此：
                     *
                     * BlockStorage.retrieve(block)
                     *
                     * 不会由本类触发。
                     */

                    if (breakBlocks) {

                        destroyBlocks.add(new int[] {block.getX(), block.getY(), block.getZ()});

                        destroyedBlocks++;
                    }
                }
            }
        }

        // ====================================================
        // 输出诊断信息
        // ====================================================

        plugin.getLogger()
                .info("核爆诊断：扫描 "
                        + scannedBlocks
                        + " 个方块，"
                        + "受保护 "
                        + protectedBlocks
                        + " 个，"
                        + "准备删除 "
                        + destroyedBlocks
                        + " 个方块。");

        // ====================================================
        // 删除方块
        //
        // 优先使用 WorldEdit 批量删除，
        // 未安装 WorldEdit 时降级为逐个方块删除。
        // ====================================================

        if (breakBlocks && !destroyBlocks.isEmpty()) {

            boolean success = false;

            if (isWorldEditAvailable()) {

                success = executeWorldEdit(world, destroyBlocks);
            }

            if (!success) {

                plugin.getLogger().info("未使用 WorldEdit，普通方式删除 " + destroyBlocks.size() + " 个方块。");

                success = deleteBlocksWithoutWorldEdit(world, destroyBlocks);
            }

            if (success) {

                /*
                 * 使用 Snapshot 保存撤回数据。
                 *
                 * 不保存 EditSession。
                 */
                if (snapshot != null && snapshot.size() > 0) {

                    plugin.getNuclearHistory().add(snapshot);
                }
            }
        }

        /*
         * 当前诊断版本 drops 永远为空。
         *
         * 保留代码结构，但不会产生掉落物。
         */

        if (breakBlocks && !drops.isEmpty()) {

            spawnDrops(drops);
        }

        // ====================================================
        // 实体伤害
        // ====================================================

        damageEntities(center, radius);

        // ====================================================
        // 核爆后的火焰
        // ====================================================

        setFire(center, radius, reactorOwner);
    }

    // ========================================================
    // 核爆音效 / 全服提示
    // ========================================================

    private void playGlobalExplosionEffects(Location center) {

        if (!plugin.getConfig().getBoolean("effects.enabled", true)) {
            return;
        }

        // ====================================================
        // 全服提示
        // ====================================================

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {

            player.sendMessage(ChatColor.RED + "⚠ 一处发生了核爆炸！");

            player.sendMessage(ChatColor.YELLOW + "核反应堆发生严重事故，爆炸范围非常大！");
        }

        // ====================================================
        // 雷声
        // ====================================================

        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {

            player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 0.6f);
        }

        // ====================================================
        // 连续爆炸音效
        // ====================================================

        int tntExplosions = plugin.getConfig().getInt("effects.tnt-explosions", 8);

        long interval = plugin.getConfig().getLong("effects.tnt-interval", 4L);

        if (tntExplosions <= 0) {
            return;
        }

        for (int i = 0; i < tntExplosions; i++) {

            long delay = interval * (i + 1L);

            int index = i;

            Bukkit.getScheduler()
                    .runTaskLater(
                            plugin.getJavaPlugin(),
                            () -> {
                                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {

                                    player.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 8.0f, 0.45f + index * 0.02f);
                                }

                                if (index == tntExplosions - 1) {

                                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {

                                        player.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10.0f, 0.6f);
                                    }
                                }
                            },
                            delay);
        }
    }

    // ========================================================
    // 掉落物
    // ========================================================

    private void collectDrops(Block block, List<NuclearDrop> drops) {

        if (!plugin.getConfig().getBoolean("explosion.drops.enabled", true)) {
            return;
        }

        // ====================================================
        // Slimefun 方块
        // ====================================================

        if (isSlimefunBlock(block)) {

            ItemStack item = collectSlimefunDrop(block);

            if (item != null && !item.getType().isAir()) {

                drops.add(new NuclearDrop(block.getLocation().clone().add(0.5, 0.5, 0.5), item));
            }

            return;
        }

        // ====================================================
        // 普通 Minecraft 方块
        // ====================================================

        for (ItemStack item : block.getDrops()) {

            if (item == null || item.getType().isAir()) {

                continue;
            }

            drops.add(new NuclearDrop(block.getLocation().clone().add(0.5, 0.5, 0.5), item));
        }
    }

    // ========================================================
    // 判断 Slimefun 方块
    // ========================================================

    private boolean isSlimefunBlock(Block block) {

        try {

            return BlockStorage.hasBlockInfo(block);

        } catch (Throwable ignored) {

            return false;
        }
    }

    // ========================================================
    // 获取 Slimefun 掉落
    // ========================================================

    private ItemStack collectSlimefunDrop(Block block) {

        try {

            SlimefunItem slimefunItem = BlockStorage.check(block);

            // =================================================
            // 删除 Hologram
            // =================================================

            if (slimefunItem instanceof HologramOwner hologramOwner) {

                try {

                    hologramOwner.removeHologram(block);

                } catch (Throwable ignored) {
                }
            }

            // =================================================
            // 从 BlockStorage 取出物品
            // =================================================

            return BlockStorage.retrieve(block);

        } catch (Throwable ignored) {

            return null;
        }
    }

    // ========================================================
    // 生成掉落物
    // ========================================================

    private void spawnDrops(List<NuclearDrop> drops) {

        int maxItems = plugin.getConfig().getInt("explosion.drops.max-items", 5000);

        if (maxItems <= 0) {
            return;
        }

        int spawned = 0;

        for (NuclearDrop drop : drops) {

            if (spawned >= maxItems) {
                break;
            }

            ItemStack original = drop.item().clone();

            int maxStack = original.getMaxStackSize();

            int amount = original.getAmount();

            while (amount > 0 && spawned < maxItems) {

                int stackAmount = Math.min(amount, maxStack);

                ItemStack stack = original.clone();

                stack.setAmount(stackAmount);

                Location location = drop.location().clone();

                location.add((Math.random() - 0.5) * 0.8, Math.random() * 0.5, (Math.random() - 0.5) * 0.8);

                Item item = location.getWorld().dropItem(location, stack);

                Vector velocity =
                        new Vector((Math.random() - 0.5) * 0.5, 0.2 + Math.random() * 0.5, (Math.random() - 0.5) * 0.5);

                item.setVelocity(velocity);

                amount -= stackAmount;
                spawned++;
            }
        }
    }

    // ========================================================
    // 实体伤害
    // ========================================================

    private void damageEntities(Location center, int radius) {

        double maxDamage = plugin.getConfig().getDouble("explosion.max-damage", 120.0);

        if (maxDamage <= 0) {
            return;
        }

        double radiusDouble = radius;

        for (Entity entity : center.getWorld().getNearbyEntities(center, radiusDouble, radiusDouble, radiusDouble)) {

            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (entity.isDead()) {
                continue;
            }

            double distance = entity.getLocation().distance(center);

            if (distance > radiusDouble) {
                continue;
            }

            double factor = 1.0 - (distance / radiusDouble);

            factor = Math.max(0.0, Math.min(1.0, factor));

            double damage = maxDamage * factor * factor;

            if (damage <= 0) {
                continue;
            }

            try {

                living.damage(damage);

            } catch (Throwable ignored) {
            }

            Vector knockback = entity.getLocation().toVector().subtract(center.toVector());

            if (knockback.lengthSquared() > 0) {

                knockback.normalize();

                knockback.multiply(1.0 + factor * 3.0);

                knockback.setY(0.45 + factor * 1.2);

                try {

                    entity.setVelocity(knockback);

                } catch (Throwable ignored) {
                }
            }
        }
    }

    // ========================================================
    // 火焰
    // ========================================================

    private void setFire(Location center, int radius, UUID reactorOwner) {

        if (!plugin.getConfig().getBoolean("explosion.set-fire", true)) {
            return;
        }

        List<Location> fireLocations = new ArrayList<>();

        int maxFire = Math.min(2000, Math.max(100, radius * radius * 4));

        // ====================================================
        // 地面火焰
        // ====================================================

        for (int x = -radius; x <= radius; x++) {

            for (int z = -radius; z <= radius; z++) {

                if (fireLocations.size() >= maxFire) {

                    break;
                }

                if ((x * x) + (z * z) > radius * radius) {

                    continue;
                }

                if (Math.random() > 0.30) {
                    continue;
                }

                int worldX = center.getBlockX() + x;

                int worldZ = center.getBlockZ() + z;

                int groundY = center.getWorld().getHighestBlockYAt(worldX, worldZ);

                if (groundY <= 0) {
                    continue;
                }

                Block ground = center.getWorld().getBlockAt(worldX, groundY, worldZ);

                if (ground.getType().isAir()) {
                    continue;
                }

                if (ground.isLiquid()) {
                    continue;
                }

                Block fireBlock = ground.getRelative(BlockFace.UP);

                if (!fireBlock.getType().isAir()) {
                    continue;
                }

                Location fireLocation = fireBlock.getLocation();

                // =================================================
                // 领地保护
                // =================================================

                if (isProtected(fireLocation, reactorOwner)) {
                    continue;
                }

                fireBlock.setType(Material.FIRE);

                fireLocations.add(fireLocation.clone());
            }
        }

        // ====================================================
        // 空中随机火焰
        // ====================================================

        for (int i = 0; i < maxFire; i++) {

            if (fireLocations.size() >= maxFire) {

                break;
            }

            if (Math.random() > 0.55) {
                continue;
            }

            int x = (int) ((Math.random() * 2.0 - 1.0) * radius);

            int y = (int) ((Math.random() * 2.0 - 1.0) * radius);

            int z = (int) ((Math.random() * 2.0 - 1.0) * radius);

            if ((x * x) + (y * y) + (z * z) > radius * radius) {

                continue;
            }

            Block block = center.getWorld()
                    .getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);

            if (!block.getType().isAir()) {
                continue;
            }

            Block ground = block.getRelative(BlockFace.DOWN);

            if (ground.getType().isAir()) {
                continue;
            }

            if (ground.isLiquid()) {
                continue;
            }

            Location fireLocation = block.getLocation();

            if (isProtected(fireLocation, reactorOwner)) {
                continue;
            }

            block.setType(Material.FIRE);

            fireLocations.add(fireLocation.clone());
        }

        // ====================================================
        // 火焰自动消失
        // ====================================================

        long fireDuration = plugin.getConfig().getLong("explosion.fire-duration-ticks", 200L);

        if (fireDuration <= 0 || fireLocations.isEmpty()) {

            return;
        }

        Bukkit.getScheduler()
                .runTaskLater(
                        plugin.getJavaPlugin(),
                        () -> {
                            for (Location location : fireLocations) {

                                if (location == null) {
                                    continue;
                                }

                                if (location.getWorld() == null) {
                                    continue;
                                }

                                Block block = location.getBlock();

                                if (block.getType() == Material.FIRE) {

                                    block.setType(Material.AIR);
                                }
                            }
                        },
                        fireDuration);
    }

    // ========================================================
    // 普通 WorldEdit
    // ========================================================

    private boolean executeWorldEdit(World world, List<int[]> blocks) {

        if (blocks.isEmpty()) {
            return false;
        }

        try {

            plugin.getLogger().info("WorldEdit 核爆删除开始：" + blocks.size() + " 个方块");

            NuclearWorldEditBridge.deleteBlocks(world, blocks);

            plugin.getLogger().info("WorldEdit 核爆方块写入完成。");

            return true;

        } catch (Throwable throwable) {

            plugin.getLogger().warning("WorldEdit 核爆方块处理失败：" + throwable.getMessage());

            throwable.printStackTrace();

            return false;
        }
    }

    // ========================================================
    // WorldEdit 可用性检测
    //
    // 仅通过类名探测，
    // 不直接引用 WorldEdit 类型，
    // 避免未安装 WorldEdit 时类加载失败。
    // ========================================================

    private static boolean worldEditChecked = false;

    private static boolean worldEditAvailable = false;

    private static synchronized boolean isWorldEditAvailable() {

        if (!worldEditChecked) {

            worldEditChecked = true;

            try {

                Class.forName("com.sk89q.worldedit.WorldEdit");

                worldEditAvailable = true;

            } catch (Throwable ignored) {

                worldEditAvailable = false;
            }
        }

        return worldEditAvailable;
    }

    // ========================================================
    // 未安装 WorldEdit 时的降级删除
    // ========================================================

    private boolean deleteBlocksWithoutWorldEdit(World world, List<int[]> blocks) {

        try {

            for (int[] pos : blocks) {

                Block block = world.getBlockAt(pos[0], pos[1], pos[2]);

                /*
                 * 清理 Slimefun 方块数据
                 *
                 * 与 WorldEdit 路径下
                 * WorldEditIntegration 的自动清理保持一致。
                 */
                Location location = new Location(world, pos[0], pos[1], pos[2]);

                if (StorageCacheUtils.hasSlimefunBlock(location)) {

                    Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
                }

                if (!block.getType().isAir()) {

                    block.setType(Material.AIR, false);
                }
            }

            return true;

        } catch (Throwable throwable) {

            plugin.getLogger().warning("核爆方块删除失败：" + throwable.getMessage());

            throwable.printStackTrace();

            return false;
        }
    }

    // ========================================================
    // 总保护判断
    // ========================================================

    private boolean isProtected(Location location, UUID reactorOwner) {

        // ====================================================
        // PlotSquared
        // ====================================================

        if (plugin.getConfig().getBoolean("protection.plotsquared", true)) {

            if (Bukkit.getPluginManager().isPluginEnabled("PlotSquared")) {

                if (isPlotProtected(location, reactorOwner)) {

                    return true;
                }
            }
        }

        // ====================================================
        // Residence
        // ====================================================

        if (plugin.getConfig().getBoolean("protection.residence", true)) {

            if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {

                if (isResidenceProtected(location, reactorOwner)) {

                    return true;
                }
            }
        }

        return false;
    }

    // ========================================================
    // PlotSquared 保护
    // ========================================================

    private boolean isPlotProtected(Location location, UUID reactorOwner) {

        try {

            com.plotsquared.core.location.Location plotLocation =
                    com.plotsquared.bukkit.util.BukkitUtil.adapt(location);

            com.plotsquared.core.plot.Plot plot = plotLocation.getPlot();

            if (plot == null) {
                return false;
            }

            if (reactorOwner == null) {
                return true;
            }

            UUID plotOwner = plot.getOwner();

            if (plotOwner == null) {
                return true;
            }

            if (plotOwner.equals(reactorOwner)) {

                return false;
            }

            return true;

        } catch (Throwable ignored) {

            return true;
        }
    }

    // ========================================================
    // Residence 保护
    //
    // 通过反射调用 Residence API，
    // 避免对 Residence 产生编译期依赖。
    // ========================================================

    private boolean isResidenceProtected(Location location, UUID reactorOwner) {

        try {

            Object claim = getResidenceClaim(location);

            if (claim == null) {
                return false;
            }

            if (reactorOwner == null) {
                return true;
            }

            String residenceOwner = (String) claimedResidenceGetOwner.invoke(claim);

            if (residenceOwner == null || residenceOwner.isEmpty()) {

                return true;
            }

            if (residenceOwner.equalsIgnoreCase(reactorOwner.toString())) {

                return false;
            }

            org.bukkit.entity.Player player = plugin.getServer().getPlayer(reactorOwner);

            if (player != null) {

                if (residenceOwner.equalsIgnoreCase(player.getName())) {

                    return false;
                }
            }

            return true;

        } catch (Throwable ignored) {

            return true;
        }
    }

    // ========================================================
    // Residence 反射：获取领地质
    // ========================================================

    private Object getResidenceClaim(Location location) throws ReflectiveOperationException {

        if (residenceReflectionFailed) {

            throw new ClassNotFoundException("Residence reflection unavailable");
        }

        if (residenceGetInstance == null) {

            try {

                Class<?> residenceClass = Class.forName("com.bekvon.bukkit.residence.Residence");

                residenceGetInstance = residenceClass.getMethod("getInstance");

                residenceGetResidenceManager = residenceClass.getMethod("getResidenceManager");

                residenceManagerGetByLoc = Class.forName("com.bekvon.bukkit.residence.protection.ResidenceManager")
                        .getMethod("getByLoc", Location.class);

                claimedResidenceGetOwner = Class.forName("com.bekvon.bukkit.residence.protection.ClaimedResidence")
                        .getMethod("getOwner");

            } catch (Throwable throwable) {

                residenceReflectionFailed = true;

                throw throwable;
            }
        }

        Object residence = residenceGetInstance.invoke(null);

        Object manager = residenceGetResidenceManager.invoke(residence);

        return residenceManagerGetByLoc.invoke(manager, location);
    }
}
