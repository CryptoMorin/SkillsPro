package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

public class SkillActiveStateChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability ability;
    private final boolean isReady;
    private boolean cancelled;

    public SkillActiveStateChangeEvent(Player player, Ability ability, boolean isReady) {
        this.player = player;
        this.ability = ability;
        this.isReady = isReady;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SkilledPlayer getPlayerInfo() {
        return SkilledPlayer.getSkilledPlayer(player);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Ability getAbility() {
        return ability;
    }

    public boolean isReady() {
        return isReady;
    }
}