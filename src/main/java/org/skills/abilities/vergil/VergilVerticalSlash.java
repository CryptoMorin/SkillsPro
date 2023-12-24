package org.skills.abilities.vergil;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.EntityUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VergilVerticalSlash extends ActiveAbility {
    private static final Cache<UUID, Integer> UPPER_DOWN = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS).build();

    public VergilVerticalSlash() {
        super("Vergil", "vertical_slash");
        setPvPBased(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;

        Player player = (Player) event.getDamager();
        LivingEntity mainVictim = (LivingEntity) event.getEntity();

        if (player.isSneaking() && UPPER_DOWN.getIfPresent(player.getUniqueId()) != null) {
            UPPER_DOWN.invalidate(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                double range = 1;
                for (Entity nearbyEntity : player.getWorld().getNearbyEntities(mainVictim.getLocation(), range, range, range)) {
                    if (EntityUtil.filterEntity(player, nearbyEntity)) continue;
                    nearbyEntity.setVelocity(new Vector(0, -4, 0));
                }
                if (player.isSneaking()) {
                    player.setVelocity(new Vector(0, -1, 0));
                    UPPER_DOWN.put(player.getUniqueId(), 0);
                }
            }, 1L);
        }

        SkilledPlayer info = checkup(player);
        if (info == null) return;

        ParticleDisplay display = ParticleDisplay.of(Particle.SWEEP_ATTACK).withLocation(player.getEyeLocation());
        XParticle.ellipse(
                0, Math.PI,
                Math.PI / 30,
                3, 4,
                display
        );

        boolean upwards;
        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getLocation(),
                new Vector(0, -1, 0), 5, FluidCollisionMode.SOURCE_ONLY, true);
        upwards = result != null && result.getHitBlock() != null;
        double intensity = 1;
        Vector direction = new Vector(0, (upwards ? intensity : -intensity), 0);

        // As I thought, the knockback generated from this method overrides our setVelocity.
        // https://www.spigotmc.org/threads/1-8-8-cant-propel-an-entity-into-the-air.125880/
        // https://bukkit.org/threads/player-setvelocity-not-launching-player-into-y-direction.208939/page-2
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            double range = 2;
            for (Entity nearbyEntity : player.getWorld().getNearbyEntities(mainVictim.getLocation(), range, range, range)) {
                if (EntityUtil.filterEntity(player, nearbyEntity)) continue;
                nearbyEntity.setVelocity(direction);
            }
            if (player.isSneaking()) {
                player.setVelocity(direction);
                UPPER_DOWN.put(player.getUniqueId(), 0);
            }
        }, 1L);
    }
}
