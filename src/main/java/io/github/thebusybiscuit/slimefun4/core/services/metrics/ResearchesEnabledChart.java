package io.github.thebusybiscuit.slimefun4.core.services.metrics;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bstats.bukkit.Metrics.SimplePie;

class ResearchesEnabledChart extends SimplePie {

    public ResearchesEnabledChart() {
        super("servers_with_researches_enabled", () -> {
            boolean enabled = SlimefunPlugin.getSettings().researchesEnabled;
            return enabled ? "enabled" : "disabled";
        });
    }

}