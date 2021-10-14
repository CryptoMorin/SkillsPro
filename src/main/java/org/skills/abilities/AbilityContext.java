package org.skills.abilities;

import org.bukkit.entity.Player;
import org.skills.data.managers.SkilledPlayer;

public class AbilityContext {
    private final Player player;
    private final SkilledPlayer info;

    public AbilityContext(Player player, SkilledPlayer info) {
        this.player = player;
        this.info = info;
    }

    public Player getPlayer() {
        return player;
    }

    public SkilledPlayer getInfo() {
        return info;
    }
}
