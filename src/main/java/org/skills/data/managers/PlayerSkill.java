package org.skills.data.managers;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.abilities.Ability;
import org.skills.data.database.DataContainer;
import org.skills.types.Skill;
import org.skills.types.SkillManager;
import org.skills.types.Stat;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerSkill extends DataContainer {
    public static final String NONE = "none";
    protected final String skill;
    protected int level;
    protected double xp;
    protected long souls;
    protected boolean showReadyMessage = true;
    protected Set<String> disabledAbilities = new HashSet<>();
    protected Map<String, Map<String, Integer>> abilities = new HashMap<>();
    protected Map<String, Integer> stats = new HashMap<>();

    public PlayerSkill(String skill) {
        this.skill = skill;
    }

    @Override
    public @NonNull
    String getCompressedData() {
        return level + xp + souls + compressBoolean(showReadyMessage)
                + compressCollecton(disabledAbilities, x -> x)
                + compressCollecton(abilities.values(), x -> x)
                + compressCollecton(stats.values(), x -> x);
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

    public @NonNull
    Set<String> getDisabledAbilities() {
        return disabledAbilities;
    }

    public void setDisabledAbilities(Set<String> disabledAbilities) {
        this.disabledAbilities = disabledAbilities;
    }

    public boolean isAbilityDisabled(Ability ability) {
        return disabledAbilities.contains(ability.getName());
    }

    @Override
    public int hashCode() {
        return skill.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerSkill)) return false;
        PlayerSkill skill = (PlayerSkill) obj;
        return this.skill.equals(skill.skill);
    }

    @Override
    public String toString() {
        return skill + abilities + souls + xp + level + stats + showReadyMessage + disabledAbilities;
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

    public @NonNull
    Map<String, Map<String, Integer>> getImprovements() {
        return abilities;
    }

    public void setImprovements(@NonNull Map<String, Map<String, Integer>> improvements) {
        this.abilities = improvements;
    }

    public void setImprovement(@NonNull Ability ability, int level) {
        String type = skill.toLowerCase(Locale.ENGLISH);
        Map<String, Integer> abilities = this.abilities.get(type);

        if (abilities == null) {
            Map<String, Integer> newAbilities = new HashMap<>();
            newAbilities.put(ability.getName().toLowerCase(), level);
            this.abilities.put(type, newAbilities);
            return;
        }

        abilities.put(ability.getName().toLowerCase(), level);
        this.abilities.put(type, abilities);
    }

    public void addImprovementLevel(@NonNull Ability ability, int level) {
        if (level == 0) return;
        String type = skill.toLowerCase(Locale.ENGLISH);
        String abilityStr = ability.getName().toLowerCase();
        Map<String, Integer> abilities = this.abilities.get(type);

        if (abilities == null) {
            Map<String, Integer> newAbilities = new HashMap<>();
            newAbilities.put(abilityStr, level);
            this.abilities.put(type, newAbilities);
            return;
        }

        Integer num = abilities.putIfAbsent(abilityStr, level);
        if (num == null) return;

        abilities.put(abilityStr, num + level);
        this.abilities.put(type, abilities);
    }

    public int getImprovementLevel(@NonNull Ability ability) {
        String type = skill.toLowerCase(Locale.ENGLISH);
        Map<String, Integer> imp = this.abilities.get(type);
        return imp != null ? imp.getOrDefault(ability.getName().toLowerCase(), 0) : 0;
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
