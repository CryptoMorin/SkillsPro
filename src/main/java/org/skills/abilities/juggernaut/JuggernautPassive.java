package org.skills.abilities.juggernaut;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.MathUtils;

public class JuggernautPassive extends Ability {
    public JuggernautPassive() {
        super("Juggernaut", "passive");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJuggernautDefend(EntityDamageByEntityEvent event) {
        if (commonDamageCheckupReverse(event)) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (!MathUtils.hasChance((int) getScaling(info, "chance", event))) return;

        double damage = this.getScaling(info, "shield", event);
        event.setDamage(Math.max(0, event.getDamage() - damage));
    }
}
