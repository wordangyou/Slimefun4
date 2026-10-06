package com.wordangyou.yonglenuclearexplosion;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.UUID;

public final class NuclearExplosionHistory {

    private final YongleNuclearExplosion plugin;

    private final int maxHistory;

    private final File historyFolder;

    private final Deque<NuclearExplosionSnapshot> history = new ArrayDeque<>();

    public NuclearExplosionHistory(YongleNuclearExplosion plugin, int maxHistory) {

        this.plugin = plugin;

        this.maxHistory = Math.max(1, maxHistory);

        this.historyFolder = new File(plugin.getDataFolder(), "history");

        load();
    }

    private void load() {

        history.clear();

        if (!historyFolder.exists()) {

            historyFolder.mkdirs();

            return;
        }

        File[] files = historyFolder.listFiles((file, name) -> name.startsWith("explosion-") && name.endsWith(".yml"));

        if (files == null || files.length == 0) {

            return;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        for (File file : files) {

            NuclearExplosionSnapshot snapshot = NuclearExplosionSnapshot.load(file);

            if (snapshot == null) {

                plugin.getLogger().warning("无法加载核爆记录：" + file.getName());

                continue;
            }

            history.addLast(snapshot);
        }

        while (history.size() > maxHistory) {

            NuclearExplosionSnapshot old = history.removeFirst();

            deleteSnapshotFile(old);
        }

        plugin.getLogger().info("已加载 " + history.size() + " 条持久化核爆记录。");
    }

    public synchronized void add(NuclearExplosionSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        File file = getSnapshotFile(snapshot.getId());

        if (!snapshot.save(file)) {

            plugin.getLogger().warning("核爆记录保存失败：" + snapshot.getId());

            return;
        }

        history.addLast(snapshot);

        while (history.size() > maxHistory) {

            NuclearExplosionSnapshot old = history.removeFirst();

            deleteSnapshotFile(old);
        }
    }

    public synchronized NuclearExplosionSnapshot removeLast() {

        if (history.isEmpty()) {
            return null;
        }

        return history.removeLast();
    }

    public synchronized void delete(NuclearExplosionSnapshot snapshot) {

        if (snapshot == null) {
            return;
        }

        deleteSnapshotFile(snapshot);
    }

    public synchronized int size() {
        return history.size();
    }

    /**
     * 服务器关闭时只清理内存。
     *
     * 不删除磁盘历史。
     */
    public synchronized void clear() {
        history.clear();
    }

    private File getSnapshotFile(UUID id) {

        return new File(historyFolder, "explosion-" + id + ".yml");
    }

    private void deleteSnapshotFile(NuclearExplosionSnapshot snapshot) {

        File file = getSnapshotFile(snapshot.getId());

        if (!file.delete() && file.exists()) {

            plugin.getLogger().warning("无法删除旧核爆记录：" + file.getName());
        }
    }
}
