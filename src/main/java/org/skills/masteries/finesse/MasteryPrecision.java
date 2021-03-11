package org.skills.masteries.finesse;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.masteries.managers.Mastery;

public class MasteryPrecision extends Mastery {
    public MasteryPrecision() {
        super("Precision", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getDamager() instanceof Arrow) {
            Arrow a = (Arrow) event.getDamager();
            if (a.getShooter() instanceof Player) {
                Player p = (Player) a.getShooter();
                SkilledPlayer info = this.checkup(p);
                if (info == null) return;
                event.setDamage(event.getDamage() + getScaling(info));
            }
        }
    }
}
