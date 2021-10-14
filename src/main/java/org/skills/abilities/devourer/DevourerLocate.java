package org.skills.abilities.devourer;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.LocationUtils;

public class DevourerLocate extends InstantActiveAbility {
    public DevourerLocate() {
        super("Devourer", "locate");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        Entity found = null;
        double lastDistance = Integer.MAX_VALUE;
        double range = getScaling(info, "range");
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player)) continue;
            double currentDistance = LocationUtils.distance(entity.getLocation(), player.getLocation());
            if (currentDistance < lastDistance) {
                lastDistance = currentDistance;
                found = entity;
            }
        }

        if (found == null) {
            SkillsLang.ABILITY_LOCATE_NOT_FOUND.sendMessage(player);
            return;
        }

        LivingEntity entity = (LivingEntity) found;
        new BukkitRunnable() {
            static final double rate = Math.PI / 10;
            final Location lastLocation = player.getLocation();
            double theta = 0;
            int times = 100;

            @Override
            public void run() {
                lastLocation.add(Math.sin(theta), 0, 0);
                Vector direction = entity.getEyeLocation().toVector().subtract(lastLocation.toVector()).normalize();
                lastLocation.add(direction);

                theta += rate;
                if (theta > Math.PI) theta = -Math.PI;

                ParticleDisplay.display(lastLocation, Particle.CLOUD);
                if (--times == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 2L);
    }
}
