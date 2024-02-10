package org.skills.abilities;

import org.bukkit.entity.Player;
import org.skills.data.managers.SkilledPlayer;

public class AbilityContext {
    private final Player player;
    private final SkilledPlayer info;
    private final InstantActiveAbility ability;

    public AbilityContext(Player player, SkilledPlayer info, InstantActiveAbility ability) {
        this.player = player;
        this.info = info;
        this.ability = ability;
    }

    public Player getPlayer() {
        return player;
    }

    public SkilledPlayer getInfo() {
        return info;
    }

    public double getScaling(String option) {
        return ability.getScaling(info, option);
    }

    public boolean hasAbilityLevel(int lvl) {
        return getAbilityLevel() >= lvl;
    }

    public int getAbilityLevel() {
        return info.getAbilityLevel(ability);
    }
}
