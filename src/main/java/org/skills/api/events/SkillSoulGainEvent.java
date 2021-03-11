package org.skills.api.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.data.managers.SkilledPlayer;

public class SkillSoulGainEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Entity killed;
    private int gained;
    private boolean cancelled;

    public SkillSoulGainEvent(Player player, Entity killed, int gained) {
        this.player = player;
        this.killed = killed;
        this.gained = gained;
    }

    public static HandlerList getHandlerList() {
        return SkillSoulGainEvent.handlers;
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
        return SkillSoulGainEvent.handlers;
    }

    public int getGained() {
        return this.gained;
    }

    public void setGained(int gained) {
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