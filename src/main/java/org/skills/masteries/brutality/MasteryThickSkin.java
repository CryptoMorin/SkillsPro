package org.skills.masteries.brutality;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.masteries.managers.Mastery;

public class MasteryThickSkin extends Mastery {
    public MasteryThickSkin() {
        super("Thick_Skin", true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        event.setDamage(event.getDamage() - getScaling(info));
    }
}
