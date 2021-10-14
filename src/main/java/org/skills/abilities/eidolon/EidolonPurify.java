package org.skills.abilities.eidolon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;

public class EidolonPurify extends Ability {
    public EidolonPurify() {
        super("Eidolon", "purify");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        double damage = this.getScaling(info, "damage", event) * getScaling(info, "hp", event);
        double max = getScaling(info, "max-damage", event);
        if (damage > max) damage = max;

        event.setDamage(event.getDamage() + damage);
        info.setEnergy(info.getEnergy() + getScaling(info, "energy", event));
    }
}
