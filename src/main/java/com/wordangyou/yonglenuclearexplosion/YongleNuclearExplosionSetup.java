package com.wordangyou.yonglenuclearexplosion;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 核爆系统挂载入口。
 *
 * 负责启动 / 关闭核爆系统，
 * 以及从旧版独立附属插件的数据目录迁移数据。
 *
 * @author wordangyou
 */
public final class YongleNuclearExplosionSetup {

    /**
     * 旧版独立附属插件的数据目录。
     */
    private static final String LEGACY_FOLDER = "plugins/YongleNuclearExplosion";

    private static YongleNuclearExplosion nuclearExplosion;

    private YongleNuclearExplosionSetup() {}

    /**
     * 启动核爆系统。
     */
    public static void setup(Slimefun plugin) {
        migrateLegacyData(plugin);

        nuclearExplosion = new YongleNuclearExplosion(plugin);
        nuclearExplosion.start();
    }

    /**
     * 关闭核爆系统。
     */
    public static void shutdown() {
        if (nuclearExplosion != null) {
            nuclearExplosion.stop();
            nuclearExplosion = null;
        }
    }

    /**
     * 把旧版独立插件的数据迁移到
     *
     * plugins/Slimefun/nuclear-explosion/
     *
     * 不删除任何旧数据。
     */
    private static void migrateLegacyData(Slimefun plugin) {
        File legacyFolder = new File(LEGACY_FOLDER);

        if (!legacyFolder.isDirectory()) {
            return;
        }

        File dataFolder = new File(plugin.getDataFolder(), "nuclear-explosion");

        // ====================================================
        // reactors.yml
        // ====================================================

        File legacyReactors = new File(legacyFolder, "reactors.yml");
        File newReactors = new File(dataFolder, "reactors.yml");

        if (legacyReactors.isFile() && !newReactors.exists()) {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            try {
                Files.copy(legacyReactors.toPath(), newReactors.toPath());
                plugin.getLogger().info("已迁移旧核反应堆数据: reactors.yml");
            } catch (IOException e) {
                plugin.getLogger().warning("迁移 reactors.yml 失败: " + e.getMessage());
            }
        }

        // ====================================================
        // history/
        // ====================================================

        File legacyHistory = new File(legacyFolder, "history");
        File newHistory = new File(dataFolder, "history");

        if (legacyHistory.isDirectory() && !newHistory.exists()) {
            try {
                copyDirectory(legacyHistory.toPath(), newHistory.toPath());
                plugin.getLogger().info("已迁移旧核爆历史记录: history/");
            } catch (IOException e) {
                plugin.getLogger().warning("迁移核爆历史记录失败: " + e.getMessage());
            }
        }
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path path : stream.toList()) {
                Path destination = target.resolve(source.relativize(path));

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(path, destination);
                }
            }
        }
    }
}
