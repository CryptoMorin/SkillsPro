package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;

/**
 * This event is only alled when a skill is upgraded from the {@code /skill imrpove} command, not
 * when it's changed by admin commands like {@code /skill user improve} or for starter ability levels.
 */
public class SkillImproveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability improvement;
    private final int oldLevel, newLevel;
    private final long soulsCost;

    public SkillImproveEvent(Player player, Ability improvement, int oldLevel, int newLevel, long soulsCost) {
        this.player = player;
        this.improvement = improvement;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.soulsCost = soulsCost;
    }

    public static HandlerList getHandlerList() {
        return SkillImproveEvent.handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return SkillImproveEvent.handlers;
    }

    public long getSoulsCost() {
        return soulsCost;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Ability getImprovement() {
        return this.improvement;
    }

    public Ability getAbility() {
        return this.improvement;
    }
}
