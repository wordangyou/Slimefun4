package io.github.thebusybiscuit.slimefun4.core.services.metrics;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bstats.bukkit.Metrics.AdvancedPie;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

class AddonsChart extends AdvancedPie {

    public AddonsChart() {
        super("installed_addons", () -> {
            Map<String, Integer> addons = new HashMap<>();

            for (Plugin plugin : SlimefunPlugin.getInstalledAddons()) {
                if (plugin.isEnabled()) {
                    addons.put(plugin.getName(), 1);
                }
            }

            return addons;
        });
    }

}