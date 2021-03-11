package org.skills.masteries.brutality;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.masteries.managers.Mastery;

public class MasteryPower extends Mastery {
    public MasteryPower() {
        super("Power", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            event.setDamage(event.getDamage() + getScaling(info));
        }
    }
}