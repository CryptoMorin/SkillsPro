package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

public class SkillToggleAbilityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Ability ability;
    private boolean disabled;

    public SkillToggleAbilityEvent(Player player, Ability ability, boolean disabled) {
        this.player = player;
        this.ability = ability;
        this.disabled = disabled;
    }

    public static HandlerList getHandlerList() {
        return SkillToggleAbilityEvent.handlers;
    }

    public Ability getAbility() {
        return this.ability;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return SkillToggleAbilityEvent.handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SkilledPlayer getInfo() {
        return SkilledPlayer.getSkilledPlayer(player);
    }
}
