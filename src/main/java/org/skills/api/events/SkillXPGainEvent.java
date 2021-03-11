package org.skills.api.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.data.managers.SkilledPlayer;

public class SkillXPGainEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity killed;
    private double gained;
    private boolean cancelled;

    public SkillXPGainEvent(Player player, Entity killed, double gained) {
        this.player = player;
        this.killed = killed;
        this.gained = gained;
    }

    public static HandlerList getHandlerList() {
        return SkillXPGainEvent.handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return SkillXPGainEvent.handlers;
    }

    public double getGained() {
        return this.gained;
    }

    public void setGained(double gained) {
        this.gained = gained;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getKilled() {
        return this.killed;
    }

    public boolean willLevelUp() {
        return getPlayerInfo().willLevelUp(this.gained);
    }

    public SkilledPlayer getPlayerInfo() {
        return SkilledPlayer.getSkilledPlayer(player);
    }
}