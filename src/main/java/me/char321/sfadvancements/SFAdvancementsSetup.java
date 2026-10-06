package me.char321.sfadvancements;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 粘液进度挂载入口。
 *
 * 负责启动 / 关闭本模块，
 * 以及从旧版独立附属插件的数据目录迁移数据。
 */
public final class SFAdvancementsSetup {

    /**
     * 旧版独立附属插件的数据目录。
     */
    private static final String LEGACY_FOLDER = "plugins/SlimefunAdvancements";

    private static SFAdvancements sfAdvancements;

    private SFAdvancementsSetup() {}

    /**
     * 启动粘液进度。
     */
    public static void setup(Slimefun plugin) {
        migrateLegacyData(plugin);

        sfAdvancements = new SFAdvancements(plugin);
        sfAdvancements.start();
    }

    /**
     * 关闭粘液进度。
     */
    public static void shutdown() {
        if (sfAdvancements != null) {
            sfAdvancements.stop();
            sfAdvancements = null;
        }
    }

    /**
     * 把旧版独立插件的数据迁移到
     *
     * plugins/Slimefun/sfadvancements/
     *
     * 不删除任何旧数据。
     */
    private static void migrateLegacyData(Slimefun plugin) {
        File legacyFolder = new File(LEGACY_FOLDER);

        if (!legacyFolder.isDirectory()) {
            return;
        }

        File dataFolder = new File(plugin.getDataFolder(), "sfadvancements");
        File dataFile = new File(plugin.getDataFolder(), "sfadvancements.yml");

        // 主配置
        File legacyConfig = new File(legacyFolder, "config.yml");
        if (legacyConfig.isFile() && !dataFile.exists()) {
            copyFile(legacyConfig, dataFile, plugin, "config.yml");
        }

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // groups.yml / advancements.yml
        copyFile(new File(legacyFolder, "groups.yml"), new File(dataFolder, "groups.yml"), plugin, "groups.yml");
        copyFile(
                new File(legacyFolder, "advancements.yml"),
                new File(dataFolder, "advancements.yml"),
                plugin,
                "advancements.yml");

        // advancements/ 玩家进度
        File legacyProgress = new File(legacyFolder, "advancements");
        File newProgress = new File(dataFolder, "advancements");
        if (legacyProgress.isDirectory() && !newProgress.exists()) {
            try {
                copyDirectory(legacyProgress.toPath(), newProgress.toPath());
                plugin.getLogger().info("已迁移旧玩家进度: advancements/");
            } catch (IOException e) {
                plugin.getLogger().warning("迁移旧玩家进度失败: " + e.getMessage());
            }
        }

        // backups/ 导入备份
        File legacyBackups = new File(legacyFolder, "backups");
        File newBackups = new File(dataFolder, "backups");
        if (legacyBackups.isDirectory() && !newBackups.exists()) {
            try {
                copyDirectory(legacyBackups.toPath(), newBackups.toPath());
                plugin.getLogger().info("已迁移旧备份: backups/");
            } catch (IOException e) {
                plugin.getLogger().warning("迁移旧备份失败: " + e.getMessage());
            }
        }
    }

    private static void copyFile(File source, File target, Slimefun plugin, String name) {
        if (!source.isFile() || target.exists()) {
            return;
        }

        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try {
            Files.copy(source.toPath(), target.toPath());
            plugin.getLogger().info("已迁移旧数据: " + name);
        } catch (IOException e) {
            plugin.getLogger().warning("迁移 " + name + " 失败: " + e.getMessage());
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
