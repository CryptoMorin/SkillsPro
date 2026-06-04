package org.skills.data.managers;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.abilities.Ability;
import org.skills.abilities.KeyBinding;
import org.skills.data.database.DataContainer;
import org.skills.main.SkillsConfig;
import org.skills.types.Skill;
import org.skills.types.SkillManager;
import org.skills.types.Stat;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerSkill extends DataContainer {
    public static final String NONE = "none";
    public static final boolean
            SHARED_LEVELS = SkillsConfig.SKILLS_SHARED_DATA_LEVELS.getBoolean(),
            SHARED_SOULS = SkillsConfig.SKILLS_SHARED_DATA_SOULS.getBoolean(),
                    SHARED_STATS = SkillsConfig.SKILLS_SHARED_DATA_STATS.getBoolean(),
                    SHARED_PRESTIGE = SkillsConfig.SKILLS_SHARED_DATA_PRESTIGE.getBoolean();

    protected final String skill;
    protected int level;
    protected int prestige;
    protected double xp;
    protected long souls;
    protected boolean showReadyMessage = true;
    protected Map<String, PlayerAbilityData> abilities = new HashMap<>();
    protected Map<String, Integer> stats = new HashMap<>();

    public PlayerSkill(String skill) {
        this.skill = skill;
    }

    @Override
    public @NonNull
    String getCompressedData() {
        return level + prestige + xp + souls + compressBoolean(showReadyMessage)
                + compressCollecton(abilities.values(), x -> compressBoolean(x.isDisabled()) + x.getLevel() +
                (x.getKeyBinding() == null ? "" : KeyBinding.toString(x.getKeyBinding())))
                + compressCollecton(stats.values(), x -> x);
    }

    public static String getPrestigeDisplay(int prestige) {
        if (prestige <= 0)
            return "";
        if (prestige <= 4) {
            StringBuilder stars = new StringBuilder(prestige);
            for (int i = 0; i < prestige; i++)
                stars.append('\u2605');
            return stars.toString();
        }
        return toRoman(prestige);
    }

    private static String toRoman(int number) {
        if (number <= 0)
            return "";

        int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] numerals = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

        StringBuilder roman = new StringBuilder();
        int remaining = number;
        for (int i = 0; i < values.length; i++) {
            while (remaining >= values[i]) {
                roman.append(numerals[i]);
                remaining -= values[i];
            }
        }

        return roman.toString();
    }

    public boolean showReadyMessage() {
        return showReadyMessage;
    }

    public void setShowReadyMessage(boolean showReadyMessage) {
        this.showReadyMessage = showReadyMessage;
    }

    public @Nullable
    Skill getSkill() {
        return SkillManager.getSkill(this.skill);
    }

    public int getLevel() {
        return level;
    }

    public int getPrestige() {
        return prestige;
    }

    public void setPrestige(int prestige) {
        this.prestige = Math.max(0, prestige);
    }

    public void addPrestige(int amount) {
        setPrestige(this.prestige + amount);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getXP() {
        return xp;
    }

    public void setAbsoluteXP(double xp) {
        this.xp = xp;
    }

    public void addSouls(int souls) {
        this.souls += souls;
    }

    public String getSkillName() {
        return skill;
    }

    public long getSouls() {
        return souls;
    }

    public void setSouls(long souls) {
        this.souls = souls;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("PlayerSkill doesn't have a hashcode");
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("PlayerSkill equals() should not be called");
    }

    @Override
    public String toString() {
        return skill + abilities + souls + xp + level + stats + showReadyMessage;
    }

    public int getStat(@NonNull String type) {
        return stats.getOrDefault(type.toUpperCase(Locale.ENGLISH), 0);
    }

    public int getStat(@Nonnull Stat stat) {
        return getStat(stat.getDataNode());
    }

    public int getPoints() {
        return getStat(Stat.POINTS.getDataNode());
    }

    public void addStat(@NonNull String type, int amount) {
        setStat(type, getStat(type) + amount);
    }

    public void addStat(Stat type, int amount) {
        addStat(type.getDataNode(), amount);
    }

    public void setStat(@NonNull String type, int amount) {
        stats.put(type, amount);
    }

    public void setStat(@Nonnull Stat stat, int amount) {
        stats.put(stat.getDataNode(), amount);
    }

    public int resetStats() {
        int total = 0;
        for (Map.Entry<String, Integer> stat : stats.entrySet()) {
            total += stat.getValue();
            setStat(stat.getKey(), 0);
        }

        setStat(Stat.POINTS, total);
        return total;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public void setStats(Map<String, Integer> stats) {
        Objects.requireNonNull(stats, "Player stats cannot be null");
        this.stats = stats;
    }

    public Map<String, PlayerAbilityData> getAbilities() {
        return abilities;
    }

    public void setAbilities(@NonNull Map<String, PlayerAbilityData> abilities) {
        this.abilities = abilities;
    }

    public void setAbilityLevel(@NonNull Ability ability, int level) {
        PlayerAbilityData data = abilities.get(ability.getName());
        if (data == null) data = getDataOrDefault(ability);
        data.setLevel(level);
        abilities.put(ability.getName(), data);
    }

    public void addAbilityLevel(@NonNull Ability ability, int level) {
        PlayerAbilityData data = abilities.get(ability.getName());
        if (data == null) data = getDataOrDefault(ability);
        data.addLevel(level);

        abilities.put(ability.getName(), data);
    }

    public PlayerAbilityData getAbilityData(@NonNull Ability ability) {
        PlayerAbilityData data = abilities.get(ability.getName());
        if (data == null) data = getDataOrDefault(ability);
        return data;
    }

    public PlayerAbilityData getDataOrDefault(Ability ability) {
        PlayerAbilityData data = null;
        if (getSkill().hasAbility(ability)) abilities.put(ability.getName(), data = new PlayerAbilityData());
        return data;
    }

    public int getAbilityLevel(@NonNull Ability ability) {
        PlayerAbilityData data = getAbilityData(ability);
        return data == null ? 0 : data.getLevel();
    }

    @Override
    public @NonNull
    String getKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIdentifier(@NonNull String identifier) {
        throw new UnsupportedOperationException();
    }
}
