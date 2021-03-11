package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;

public class SkillImproveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability improvement;

    public SkillImproveEvent(Player player, Ability improvement) {
        this.player = player;
        this.improvement = improvement;
    }

    public static HandlerList getHandlerList() {
        return SkillImproveEvent.handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return SkillImproveEvent.handlers;
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
