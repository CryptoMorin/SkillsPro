package org.skills.abilities.firemage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

public class FireMagePhoenixEssence extends Ability {
    public FireMagePhoenixEssence() {
        super("FireMage", "phoenix_essence");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireMageAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player p = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        int fire = event.getEntity().getFireTicks();
        double fireMod = getExtraScaling(info, "fire-mod", "%fire%", fire / 20);
        double scaling = this.getScaling(info);
        event.setDamage(event.getDamage() + (scaling * fireMod));
    }
}
