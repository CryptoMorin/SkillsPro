package org.skills.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skills.abilities.eidolon.EidolonForm;
import org.skills.data.managers.SkilledPlayer;

public class EidolonImbalanceChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private EidolonForm newForm;

    public EidolonImbalanceChangeEvent(Player player, EidolonForm newForm) {
        super(true);
        this.player = player;
        this.newForm = newForm;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public EidolonForm getNewForm() {
        return this.newForm;
    }

    public void setNewForm(EidolonForm newForm) {
        this.newForm = newForm;
    }

    public SkilledPlayer getPlayerInfo() {
        return SkilledPlayer.getSkilledPlayer(player);
    }
}
