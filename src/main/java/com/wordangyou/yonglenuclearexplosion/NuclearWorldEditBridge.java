package com.wordangyou.yonglenuclearexplosion;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.List;

/**
 * WorldEdit 桥接类
 *
 * 这是整个核爆系统中唯一直接引用 WorldEdit 的类。
 *
 * 服务器未安装 WorldEdit 时，
 * 本类不会被加载，
 * 因此不会触发 NoClassDefFoundError。
 *
 * 调用方必须先通过探测确认 WorldEdit 存在，
 * 才会执行到这里。
 */
final class NuclearWorldEditBridge {

    private NuclearWorldEditBridge() {}

    /**
     * 使用 WorldEdit EditSession 删除方块。
     *
     * 注意：
     * 如果服务器同时安装 FAWE，
     * WorldEdit 的 EditSession 仍可能被 FAWE 接管。
     *
     * 内部异常由调用方处理。
     */
    static void deleteBlocks(org.bukkit.World world, List<int[]> blocks) throws Exception {

        EditSession editSession = null;

        try {

            com.sk89q.worldedit.world.World editWorld = BukkitAdapter.adapt(world);

            editSession = WorldEdit.getInstance().newEditSession(editWorld);

            /*
             * WorldEdit 7.3.16 中：
             *
             * setBlocks(Set<BlockVector3>, Pattern)
             *
             * 不是公开 API。
             *
             * 因此这里逐个使用公开的 setBlock()。
             */
            for (int[] pos : blocks) {

                editSession.setBlock(BlockVector3.at(pos[0], pos[1], pos[2]), BlockTypes.AIR.getDefaultState());
            }

        } finally {

            if (editSession != null) {

                try {

                    editSession.close();

                } catch (Throwable ignored) {

                    // 关闭失败不影响删除结果
                }
            }
        }
    }
}
