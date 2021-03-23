package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.data.managers.SkilledPlayer;
import org.skills.managers.LevelUp;

public class SkillLevelUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final SkilledPlayer info;
    private final Player player;
    private int level;
    private boolean cancelled;

    public SkillLevelUpEvent(SkilledPlayer info, Player player, int level) {
        this.info = info;
        this.player = player;
        this.level = level;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the sum of added level and the player's level.
     *
     * @return the new level after leveling up.
     */
    public int getNewLevel() {
        return info.getLevel() + level;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAddedLevel() {
        return level;
    }

    public void setAddedLevel(int level) {
        this.level = level;
    }

    public LevelUp getLevelProperties() {
        return LevelUp.getProperties(getNewLevel());
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public SkilledPlayer getInfo() {
        return info;
    }
}
