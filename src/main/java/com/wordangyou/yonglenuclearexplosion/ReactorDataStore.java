package com.wordangyou.yonglenuclearexplosion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ReactorDataStore {

    private final YongleNuclearExplosion plugin;

    private final File file;

    private final Map<String, ReactorData> reactors = new ConcurrentHashMap<>();

    public ReactorDataStore(YongleNuclearExplosion plugin) {
        this.plugin = plugin;

        this.file = new File(plugin.getDataFolder(), "reactors.yml");
    }

    public void load() {

        reactors.clear();

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("reactors");

        if (section == null) {
            return;
        }

        int loaded = 0;

        for (String key : section.getKeys(false)) {

            ConfigurationSection data = section.getConfigurationSection(key);

            if (data == null) {
                continue;
            }

            String worldString = data.getString("world");

            if (worldString == null) {
                continue;
            }

            UUID worldUuid;

            try {
                worldUuid = UUID.fromString(worldString);
            } catch (IllegalArgumentException e) {
                continue;
            }

            int x = data.getInt("x");
            int y = data.getInt("y");
            int z = data.getInt("z");

            UUID owner = null;

            String ownerString = data.getString("owner");

            if (ownerString != null && !ownerString.isBlank()) {

                try {
                    owner = UUID.fromString(ownerString);
                } catch (IllegalArgumentException ignored) {
                }
            }

            ReactorData reactor = new ReactorData(worldUuid, x, y, z, owner);

            reactors.put(getKey(worldUuid, x, y, z), reactor);

            loaded++;
        }

        plugin.getLogger().info("已加载 " + loaded + " 个核反应堆记录。");
    }

    public synchronized void save() {

        YamlConfiguration config = new YamlConfiguration();

        ConfigurationSection section = config.createSection("reactors");

        int index = 0;

        for (ReactorData data : reactors.values()) {

            ConfigurationSection reactor = section.createSection("reactor-" + index++);

            reactor.set("world", data.world().toString());

            reactor.set("x", data.x());

            reactor.set("y", data.y());

            reactor.set("z", data.z());

            if (data.owner() != null) {

                reactor.set("owner", data.owner().toString());
            }
        }

        try {

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            config.save(file);

        } catch (IOException e) {

            plugin.getLogger().severe("保存 reactors.yml 失败！");

            e.printStackTrace();
        }
    }

    public void register(Location location, UUID owner) {

        if (location == null || location.getWorld() == null) {

            return;
        }

        UUID world = location.getWorld().getUID();

        ReactorData data =
                new ReactorData(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), owner);

        reactors.put(getKey(world, data.x(), data.y(), data.z()), data);

        save();
    }

    public void remove(Location location) {

        if (location == null || location.getWorld() == null) {

            return;
        }

        UUID world = location.getWorld().getUID();

        reactors.remove(getKey(world, location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        save();
    }

    public UUID getOwner(Location location) {

        if (location == null || location.getWorld() == null) {

            return null;
        }

        ReactorData data = reactors.get(
                getKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));

        if (data == null) {
            return null;
        }

        return data.owner();
    }

    public List<ReactorData> getAll() {

        return new ArrayList<>(reactors.values());
    }

    public int size() {
        return reactors.size();
    }

    public Location toLocation(ReactorData data) {

        if (data == null) {
            return null;
        }

        World world = Bukkit.getWorld(data.world());

        if (world == null) {
            return null;
        }

        return new Location(world, data.x(), data.y(), data.z());
    }

    private String getKey(UUID world, int x, int y, int z) {

        return world + ":" + x + ":" + y + ":" + z;
    }

    public record ReactorData(UUID world, int x, int y, int z, UUID owner) {}
}
