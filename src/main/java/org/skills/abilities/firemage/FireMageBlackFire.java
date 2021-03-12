package org.skills.abilities.firemage;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.EntityUtil;

public class FireMageBlackFire extends Ability {
    public FireMageBlackFire() {
        super("FireMage", "black_fire");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK && event.getCause() != EntityDamageEvent.DamageCause.FIRE) return;

        for (Entity entity : event.getEntity().getNearbyEntities(5, 5, 5)) {
            if (EntityUtil.isInvalidEntity(entity)) continue;
            if (!(entity instanceof Player)) continue;

            Player player = (Player) entity;
            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            double chance = this.getScaling(info);
            event.setDamage(event.getDamage() + chance);
            Location location = event.getEntity().getLocation();
            player.getWorld().playEffect(location, Effect.STEP_SOUND, Material.COAL_BLOCK);
        }
    }
}
