package io.github.thebusybiscuit.slimefun4.core.services.metrics;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bstats.bukkit.Metrics.SimplePie;

class BranchChart extends SimplePie {

    public BranchChart() {
        super("branch", SlimefunPlugin.getUpdater().getBranch()::getName);
    }

}