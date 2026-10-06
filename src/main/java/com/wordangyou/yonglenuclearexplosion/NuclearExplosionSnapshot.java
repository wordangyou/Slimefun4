package com.wordangyou.yonglenuclearexplosion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class NuclearExplosionSnapshot {

    private final UUID id;

    private final List<BlockSnapshot> blocks = new ArrayList<>();

    public NuclearExplosionSnapshot(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void capture(Block block) {

        if (block == null) {
            return;
        }

        World world = block.getWorld();

        if (world == null) {
            return;
        }

        BlockData blockData = block.getBlockData();

        blocks.add(new BlockSnapshot(
                world.getUID(), block.getX(), block.getY(), block.getZ(), block.getType(), blockData.getAsString()));
    }

    public int size() {
        return blocks.size();
    }

    public boolean save(File file) {

        YamlConfiguration config = new YamlConfiguration();

        config.set("id", id.toString());

        config.set("version", 1);

        ConfigurationSection section = config.createSection("blocks");

        int index = 0;

        for (BlockSnapshot block : blocks) {

            ConfigurationSection data = section.createSection(String.valueOf(index++));

            data.set("world", block.world().toString());

            data.set("x", block.x());

            data.set("y", block.y());

            data.set("z", block.z());

            data.set("material", block.material().name());

            data.set("block-data", block.blockData());
        }

        try {

            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {

                parent.mkdirs();
            }

            config.save(file);

            return true;

        } catch (IOException e) {

            e.printStackTrace();

            return false;
        }
    }

    public static NuclearExplosionSnapshot load(File file) {

        if (file == null || !file.exists()) {

            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String idString = config.getString("id");

        if (idString == null) {
            return null;
        }

        UUID id;

        try {

            id = UUID.fromString(idString);

        } catch (IllegalArgumentException e) {

            return null;
        }

        NuclearExplosionSnapshot snapshot = new NuclearExplosionSnapshot(id);

        ConfigurationSection section = config.getConfigurationSection("blocks");

        if (section == null) {
            return snapshot;
        }

        for (String key : section.getKeys(false)) {

            ConfigurationSection data = section.getConfigurationSection(key);

            if (data == null) {
                continue;
            }

            String worldString = data.getString("world");

            String materialString = data.getString("material");

            String blockDataString = data.getString("block-data");

            if (worldString == null || materialString == null || blockDataString == null) {

                continue;
            }

            UUID worldUuid;

            try {

                worldUuid = UUID.fromString(worldString);

            } catch (IllegalArgumentException e) {

                continue;
            }

            Material material = Material.matchMaterial(materialString);

            if (material == null) {
                continue;
            }

            snapshot.blocks.add(new BlockSnapshot(
                    worldUuid, data.getInt("x"), data.getInt("y"), data.getInt("z"), material, blockDataString));
        }

        return snapshot;
    }

    public int restore() {

        int restored = 0;

        for (BlockSnapshot snapshot : blocks) {

            World world = Bukkit.getWorld(snapshot.world());

            if (world == null) {
                continue;
            }

            Block block = world.getBlockAt(snapshot.x(), snapshot.y(), snapshot.z());

            try {

                block.setType(snapshot.material(), false);

                try {

                    BlockData blockData = Bukkit.createBlockData(snapshot.blockData());

                    block.setBlockData(blockData, false);

                } catch (Throwable ignored) {
                }

                restored++;

            } catch (Throwable ignored) {
            }
        }

        return restored;
    }

    private record BlockSnapshot(UUID world, int x, int y, int z, Material material, String blockData) {}
}
