package io.github.thebusybiscuit.slimefun4.core.services.metrics;

import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;

/**
 * This Class represents a Metrics Service that sends data to https://bstats.org/
 * This data is used to analyse the usage of this {@link Plugin}.
 * <p>
 * You can find more info in the README file of this Project on GitHub.
 *
 * @author TheBusyBiscuit
 */
public class MetricsService {

    private final SlimefunPlugin plugin;

    public MetricsService(SlimefunPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Metrics metrics = new Metrics(plugin, 4574);

        if (SlimefunPlugin.getUpdater().getBranch().isOfficial()) {
            // We really do not need this data if it is an unofficially modified build...
            metrics.addCustomChart(new AutoUpdaterChart());
        }

        metrics.addCustomChart(new ResourcePackChart());
        metrics.addCustomChart(new BranchChart());
        metrics.addCustomChart(new ServerLanguageChart());
        metrics.addCustomChart(new PlayerLanguageChart());
        metrics.addCustomChart(new ResearchesEnabledChart());
        metrics.addCustomChart(new GuideLayoutChart());
        metrics.addCustomChart(new AddonsChart());
        metrics.addCustomChart(new CommandChart());
    }

}