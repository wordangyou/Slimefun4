package io.github.linoxgh.moretools;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 更多工具挂载入口。
 *
 * 负责启动 / 关闭本模块，
 * 以及从旧版独立附属插件的数据目录迁移数据。
 */
public final class MoreToolsSetup {

    /**
     * 旧版独立附属插件的数据目录。
     */
    private static final String LEGACY_FOLDER = "plugins/MoreTools";

    private static MoreTools moreTools;

    private MoreToolsSetup() {}

    /**
     * 启动更多工具。
     */
    public static void setup(Slimefun plugin) {
        migrateLegacyData(plugin);

        moreTools = new MoreTools(plugin);
        moreTools.start();
    }

    /**
     * 关闭更多工具。
     */
    public static void shutdown() {
        if (moreTools != null) {
            moreTools.stop();
            moreTools = null;
        }
    }

    /**
     * 把旧版独立插件的配置迁移到
     *
     * plugins/Slimefun/moretools.yml
     *
     * 不删除任何旧数据。
     */
    private static void migrateLegacyData(Slimefun plugin) {
        File legacyFolder = new File(LEGACY_FOLDER);

        if (!legacyFolder.isDirectory()) {
            return;
        }

        File target = new File(plugin.getDataFolder(), "moretools.yml");
        File legacyConfig = new File(legacyFolder, "config.yml");

        if (!legacyConfig.isFile() || target.exists()) {
            return;
        }

        try {
            Files.copy(legacyConfig.toPath(), target.toPath());
            plugin.getLogger().info("已迁移旧数据: MoreTools/config.yml -> Slimefun/moretools.yml");
        } catch (IOException e) {
            plugin.getLogger().warning("迁移 config.yml 失败: " + e.getMessage());
        }
    }
}
