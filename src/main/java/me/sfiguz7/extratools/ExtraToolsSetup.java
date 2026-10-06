package me.sfiguz7.extratools;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.sfiguz7.extratools.implementation.machines.CobblestoneGenerator;
import me.sfiguz7.extratools.implementation.machines.ConcreteFactory;
import me.sfiguz7.extratools.implementation.machines.ElectricComposter;
import me.sfiguz7.extratools.implementation.machines.GoldTransmuter;
import me.sfiguz7.extratools.implementation.machines.Pulverizer;
import me.sfiguz7.extratools.implementation.machines.Vaporizer;
import me.sfiguz7.extratools.implementation.tools.Hammer;
import me.sfiguz7.extratools.lists.ETItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

/**
 * This class holds and registers all items and researches of "ExtraTools".
 *
 * <p>"ExtraTools" was originally a standalone addon created by Sfiguz7, with a Chinese
 * translation by the SlimefunGuguProject. Both projects are licensed under GPL-3.0, and the
 * addon has been merged into this project so its items are available without installing an
 * extra plugin.
 *
 * @author Sfiguz7
 * @author SlimefunGuguProject
 *
 * @see <a href="https://github.com/SlimefunGuguProject/ExtraTools">SlimefunGuguProject/ExtraTools</a>
 */
public final class ExtraToolsSetup {

    /**
     * The original {@link NamespacedKey} namespace of the addon. It has to be kept as-is,
     * otherwise the research progress of existing players could be lost.
     */
    private static final String NAMESPACE = "extratools";

    private static final List<Research> researches = new ArrayList<>();

    private static int researchId = 4100;
    private static boolean registered = false;

    private ExtraToolsSetup() {}

    /**
     * This method registers all items of "ExtraTools" into the given {@link ItemGroup}s.
     * The hammer is registered in the tools group, the machines in the machines group.
     * The researches are only created here, they get registered by {@link #setupResearches()}.
     *
     * @param plugin
     *            Our {@link Slimefun} instance
     * @param toolsGroup
     *            The {@link ItemGroup} to register the hammer in
     * @param machinesGroup
     *            The {@link ItemGroup} to register the machines in
     */
    public static void setup(
            @Nonnull Slimefun plugin, @Nonnull ItemGroup toolsGroup, @Nonnull ItemGroup machinesGroup) {
        if (registered) {
            throw new UnsupportedOperationException("ExtraTools items can only be registered once!");
        }

        registered = true;

        if (Bukkit.getPluginManager().getPlugin("ExtraTools") != null) {
            plugin.getLogger()
                    .log(
                            Level.WARNING,
                            "Detected the standalone \"ExtraTools\" plugin, which has been merged into Slimefun already."
                                    + " Please remove it, otherwise its items will fail to register due to item id conflicts!");
        }

        // Tools
        new Hammer(toolsGroup).register(plugin);
        addResearch("hammer", "锤子", 3, ETItems.HAMMER);

        // Machines
        new GoldTransmuter(machinesGroup).register(plugin);
        addResearch("gold_transmuter", "黄金转化器", 12, ETItems.GOLD_TRANSMUTER);

        new ElectricComposter(machinesGroup, ElectricComposter.Tier.ONE) {

            @Override
            public int getEnergyConsumption() {
                return 9;
            }

            @Override
            public int getSpeed() {
                return 1;
            }
        }.register(plugin);
        addResearch("electric_composter", "电动堆肥机", 18, ETItems.ELECTRIC_COMPOSTER);

        new ElectricComposter(machinesGroup, ElectricComposter.Tier.TWO) {

            @Override
            public int getEnergyConsumption() {
                return 25;
            }

            @Override
            public int getSpeed() {
                return 4;
            }
        }.register(plugin);
        addResearch("electric_composter_2", "电动堆肥机 II", 18, ETItems.ELECTRIC_COMPOSTER_2);

        new CobblestoneGenerator(machinesGroup).register(plugin);
        addResearch("cobblestone_generator", "圆石生成器", 40, ETItems.COBBLESTONE_GENERATOR);

        new Vaporizer(machinesGroup).register(plugin);
        addResearch("vaporizer", "蒸馏器", 18, ETItems.VAPORIZER);

        new ConcreteFactory(machinesGroup).register(plugin);
        addResearch("concrete_factory", "混凝土搅拌机", 12, ETItems.CONCRETE_FACTORY);

        new Pulverizer(machinesGroup).register(plugin);
        addResearch("pulverizer", "方块过筛机", 18, ETItems.PULVERIZER);
    }

    /**
     * This method registers all researches of "ExtraTools".
     * It has to be called after {@link #setup(Slimefun, ItemGroup, ItemGroup)}.
     */
    public static void setupResearches() {
        for (Research research : researches) {
            research.register();
        }

        researches.clear();
    }

    private static void addResearch(String key, String name, int cost, ItemStack... items) {
        researchId++;

        Research research = new Research(new NamespacedKey(NAMESPACE, key), researchId, name, cost);
        research.addItems(items);
        researches.add(research);
    }
}
