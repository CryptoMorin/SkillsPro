package org.skills.masteries.managers;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsMasteryConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;
import org.skills.types.Stat;
import org.skills.utils.MathUtils;

import java.util.Locale;
import java.util.Objects;

public abstract class Mastery implements Listener {
    private final String name;

    public Mastery(@NonNull String name) {
        this(name, false);
    }

    protected Mastery(String name, boolean isDefault) {
        this.name = name;
        if (isDefault && SkillsMasteryConfig.ENABLED.withProperties(getConfigName()).getBoolean()) MasteryManager.registerDefault(this);
    }

    public static boolean isPlaced(Block block) {
        return block.hasMetadata(MasteryManager.PLACED);
    }

    public void register(@NonNull JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "Plugin instance for mastery cannot be null");
        if (plugin instanceof SkillsPro)
            throw new IllegalArgumentException("Plugin instance for mastery should not be an instance of Skills");
        Validate.notEmpty(name, "Mastery name cannot be null or empty");
        MasteryManager.registerMastery(plugin, this);
    }

    public int getUpgradeCost(SkilledPlayer info) {
        return getCost(info, info.getMasteryLevel(this));
    }

    public int getDowngradeCost(SkilledPlayer info) {
        return getCost(info, info.getMasteryLevel(this) - 1);
    }

    private int getCost(SkilledPlayer info, int lvl) {
        String cost = SkillsMasteryConfig.COST.withProperties(getConfigName()).getString();
        return (int) getAbsoluteScaling(info, cost, "lvl", lvl);
    }

    public double getAbsoluteScaling(SkilledPlayer info, String scaling, Object... edits) {
        if (Strings.isNullOrEmpty(scaling)) return 0;
        return MathUtils.evaluateEquation(getTranslatedScaling(info, scaling, edits));
    }

    public int getRequiredLevel(SkilledPlayer info) {
        String scaling = getExtra("required-level").getString();
        if (Strings.isNullOrEmpty(scaling)) return 0;
        return (int) getAbsoluteScaling(info, scaling);
    }

    public SkillsMasteryConfig getExtra(String extra) {
        return SkillsMasteryConfig.EXTRA.withProperties(getConfigName(), extra);
    }

    public double getExtraScaling(SkilledPlayer info, String extra) {
        return getAbsoluteScaling(info, SkillsMasteryConfig.EXTRA.withProperties(getConfigName(), extra).getString());
    }

    public double getScaling(SkilledPlayer info, Object... edits) {
        return getAbsoluteScaling(info, getScalingEquation(info), edits);
    }

    public String getScalingEquation(SkilledPlayer info) {
        return SkillsMasteryConfig.SCALING.withProperties(getConfigName()).getString();
    }

    public String getTranslatedScaling(SkilledPlayer info, String scaling, Object... edits) {
        String equation = ServiceHandler.translatePlaceholders(info.getOfflinePlayer(), scaling);
        equation = MessageHandler.replaceVariables(equation, edits);
        equation = StringUtils.replace(equation, "lvl", String.valueOf(info.getMasteryLevel(this)));
        for (Stat stats : Stat.STATS.values()) {
            equation = equation.replace('%' + stats.getNode() + '%', String.valueOf(info.getStat(stats)));
        }

        return equation;
    }

    public SkilledPlayer checkup(Player player) {
        if (!player.hasPermission("skills.masteries.creative") && player.getGameMode() == GameMode.CREATIVE) return null;
        if (!player.hasPermission("skills.mastery." + getConfigName())) return null;
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return null;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        if (info.getMasteryLevel(this) < 1) return null;
        if (!hasChance(info)) return null;
        return info;
    }

    public String getConfigName() {
        return this.name.toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /**
     * A basic provided algorithm to determine whether this mastery's bonus should
     * even be given to the player by chance.<br>
     * If the player reached the max level, this will always return true.
     * @return true if a random number between 0 and the max lvl of this mastery is less than the player's mastery level.
     */
    public boolean hasChance(SkilledPlayer info) {
        int percent = (int) getAbsoluteScaling(info, SkillsMasteryConfig.CHANCE.withProperties(getConfigName()).getString());
        return MathUtils.hasChance(percent);
    }

    public void unregister() {
        MasteryManager.unregisterMastery(name);
    }

    public @NonNull
    String getName() {
        return name;
    }

    public String getDisplayName() {
        return SkillsMasteryConfig.NAME.withProperties(getConfigName()).getString();
    }

    public int getMaxLevel() {
        return SkillsMasteryConfig.MAX_LEVEL.withProperties(getConfigName()).getInt();
    }
}
