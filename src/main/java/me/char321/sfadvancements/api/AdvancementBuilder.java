package me.char321.sfadvancements.api;

import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import me.char321.sfadvancements.SFAdvancements;
import me.char321.sfadvancements.api.criteria.CriteriaTypes;
import me.char321.sfadvancements.api.criteria.Criterion;
import me.char321.sfadvancements.api.criteria.ResearchCriterion;
import me.char321.sfadvancements.api.reward.Reward;
import me.char321.sfadvancements.util.ConfigUtils;
import me.char321.sfadvancements.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class AdvancementBuilder {
    private NamespacedKey key;
    private NamespacedKey parent;
    private AdvancementGroup group;
    private ItemStack display;
    private String frame;
    private String name;
    private boolean hidden;
    private List<Criterion> criteria = new ArrayList<>();
    private List<Reward> rewards = new ArrayList<>();

    public static AdvancementBuilder loadFromConfig(String key, ConfigurationSection config) {
        AdvancementBuilder builder = new AdvancementBuilder();

        builder.key(Utils.keyOf(key));

        String groupName = config.getString("group");
        AdvancementGroup group = getGroup(groupName);
        if (group == null) {
            SFAdvancements.warn(key + "进度的进度组 " + groupName + " 不存在!");
            return null;
        }
        builder.group(group);

        String parent = config.getString("parent");
        if (parent == null) {
            parent = groupName;
        }
        builder.parent(Utils.keyOfAllowNamespace(parent));

        ItemStack display = ConfigUtils.getItem(config, "display");
        if (display == null) {
            SFAdvancements.warn("进度 " + key + " 的展示物品无效");
            return null;
        }
        builder.display(display);

        String frame = config.getString("frame_type");
        if (frame == null) {
            frame = "TASK";
        }
        builder.frame(frame);

        String advname = config.getString("name");
        if (advname == null) {
            advname = key;
        }
        builder.name(advname);

        boolean hidden = config.getBoolean("hidden");
        if (!hidden) {
            String hiddenRaw = config.getString("hidden");
            if (hiddenRaw != null && hiddenRaw.equalsIgnoreCase("true")) {
                hidden = true;
            }
        }
        if (!hidden) {
            hidden = config.getBoolean("display.hidden");
            if (!hidden) {
                String hiddenRaw = config.getString("display.hidden");
                if (hiddenRaw != null && hiddenRaw.equalsIgnoreCase("true")) {
                    hidden = true;
                }
            }
        }
        builder.hidden(hidden);

        ConfigurationSection cripath = config.getConfigurationSection("criteria");
        if (cripath == null) {
            SFAdvancements.warn("进度 " + key + " 必须指定完成条件");
            return null;
        }
        List<Criterion> criteria = new ArrayList<>();
        boolean invalidResearch = false;
        for (String id : cripath.getKeys(false)) {
            Criterion criterion = CriteriaTypes.loadFromConfig(id, cripath.getConfigurationSection(id));
            if (criterion != null) {
                if (criterion instanceof ResearchCriterion
                        && Research.getResearch(((ResearchCriterion) criterion).getResearch()) == null) {
                    invalidResearch = true;
                    SFAdvancements.warn(
                            "无效的研究: " + ((ResearchCriterion) criterion).getResearch() + " (位于进度 " + key + ")");
                }
                criteria.add(criterion);
            } else {
                return null; // criterion failed to load, don't load the advancement
            }
        }
        builder.criteria(criteria);

        if (invalidResearch) {
            ItemStack oldDisplay = builder.display;
            ItemStack bedrock = new ItemStack(org.bukkit.Material.STRUCTURE_VOID);
            org.bukkit.inventory.meta.ItemMeta meta = bedrock.getItemMeta();
            if (meta != null) {
                if (oldDisplay != null
                        && oldDisplay.getItemMeta() != null
                        && oldDisplay.getItemMeta().hasDisplayName()) {
                    meta.setDisplayName(oldDisplay.getItemMeta().getDisplayName());
                } else if (builder.name != null) {
                    meta.setDisplayName(ConfigUtils.translate(builder.name));
                } else {
                    meta.setDisplayName(ConfigUtils.translate("&c无效的研究"));
                }

                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ConfigUtils.translate("&c此进度所属的研究无效,可能服务器暂未安装相关附属"));

                if (oldDisplay != null
                        && oldDisplay.getItemMeta() != null
                        && oldDisplay.getItemMeta().hasLore()) {
                    lore.addAll(oldDisplay.getItemMeta().getLore());
                }

                meta.setLore(lore);
                bedrock.setItemMeta(meta);
            }
            builder.display(bedrock);
        }

        List<Reward> rewards = new ArrayList<>();
        ConfigurationSection rewardSection = config.getConfigurationSection("rewards");
        if (rewardSection != null) {
            for (String command : rewardSection.getStringList("commands")) {
                rewards.add(p -> {
                    Utils.runSync(() -> {
                        Bukkit.getServer()
                                .dispatchCommand(
                                        Bukkit.getServer().getConsoleSender(), command.replace("%p%", p.getName()));
                    });
                });
            }
        }
        builder.rewards(rewards);

        return builder;
    }

    /**
     * Gets an AdvancementGroup given a name.
     *
     * @param name the name of the advancementgroup
     * @return the group with the given name, null otherwise
     */
    @Nullable public static AdvancementGroup getGroup(String name) {
        for (AdvancementGroup advgroup : SFAdvancements.getRegistry().getAdvancementGroups()) {
            if (advgroup.getId().equals(name)) {
                return advgroup;
            }
        }
        return null;
    }

    public AdvancementBuilder key(NamespacedKey key) {
        this.key = key;
        return this;
    }

    public AdvancementBuilder parent(NamespacedKey parent) {
        this.parent = parent;
        return this;
    }

    public AdvancementBuilder group(String group) {
        this.group = getGroup(group);
        if (this.group == null) {
            SFAdvancements.warn("unknown group: " + group);
        }
        return this;
    }

    public AdvancementBuilder group(AdvancementGroup group) {
        this.group = group;
        return this;
    }

    public AdvancementBuilder display(ItemStack display) {
        this.display = display;
        return this;
    }

    public AdvancementBuilder frame(String frame) {
        this.frame = frame;
        return this;
    }

    public AdvancementBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AdvancementBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public AdvancementBuilder criteria(List<Criterion> criteria) {
        this.criteria.addAll(criteria);
        return this;
    }

    public AdvancementBuilder rewards(List<Reward> rewards) {
        this.rewards.addAll(rewards);
        return this;
    }

    public void register() {
        for (Criterion criterion : criteria) {
            criterion.setAdvancement(key);
            criterion.register();
        }
        Advancement adv = new Advancement(
                key,
                parent,
                group,
                display,
                frame,
                name,
                hidden,
                criteria.toArray(new Criterion[0]),
                rewards.toArray(new Reward[0]));
        adv.register();
    }
}
