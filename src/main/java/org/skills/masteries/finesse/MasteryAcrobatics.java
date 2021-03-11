package org.skills.masteries.finesse;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.masteries.managers.Mastery;

public class MasteryAcrobatics extends Mastery {
    public MasteryAcrobatics() {
        super("Acrobatics", true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = checkup(player);

        if (info == null) return;
        event.setDamage(Math.max(0, event.getDamage() - getScaling(info)));
    }
}
