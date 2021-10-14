package org.skills.abilities.eidolon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;

public class EidolonShapeShifter extends Ability {
    public EidolonShapeShifter() {
        super("Eidolon", "shape_shifter");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageShif(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;

        Player player;
        SkilledPlayer info;
        // Defense - Light
        if (event.getEntity() instanceof Player) {
            player = (Player) event.getEntity();
            info = this.checkup(player);
            if (info != null) {
                // 0-15
                int lightLvl = player.getLocation().getBlock().getLightLevel();
                if (lightLvl <= 7 && !getOptions(info, "reversed").getBoolean()) return;
                if (lightLvl > 7) lightLvl -= 7;
                else lightLvl = 7 - lightLvl;

                event.setDamage(event.getDamage() - getScaling(info, "damage-shield",
                        "damage", event.getDamage(), "light", String.valueOf(lightLvl)));
            }
        }

        // Damage - Dark
        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
            info = this.checkup(player);
            if (info == null) return;
            // 0-15
            int lightLvl = player.getLocation().getBlock().getLightLevel();
            if (lightLvl >= 7 && !getOptions(info, "reversed").getBoolean()) return;
            if (lightLvl > 7) lightLvl -= 7;
            else lightLvl = 7 - lightLvl;

            event.setDamage(event.getDamage() + getScaling(info, "damage-shield",
                    "damage", event.getDamage(), "light", String.valueOf(lightLvl)));
        }
    }
}
