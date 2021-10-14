package org.skills.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.data.managers.SkilledPlayer;
import org.skills.types.Skill;

public class ClassChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final SkilledPlayer info;
    private Skill newSkill;
    private boolean cancelled;

    public ClassChangeEvent(SkilledPlayer info, Skill newClass) {
        this.info = info;
        this.newSkill = newClass;
    }

    public static HandlerList getHandlerList() {
        return ClassChangeEvent.handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return ClassChangeEvent.handlers;
    }

    public SkilledPlayer getInfo() {
        return info;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Skill getNewSkill() {
        return newSkill;
    }

    public void setNewSkill(Skill newSkill) {
        this.newSkill = newSkill;
    }
}
