package org.skills.abilities.priest;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.EntityUtil;

import java.util.List;

public class PriestBarrier extends ActiveAbility {
    public PriestBarrier() {
        super("Priest", "barrier", true);
    }

    @Override
    protected void useSkill(Player player) {
        SkilledPlayer info = activeCheckup(player);
        if (info == null) return;

        double duration = getExtraScaling(info, "duration");
        int frequency = (int) getExtraScaling(info, "frequency");
        double decreamentFactor = 1D / (20D / frequency);

        double radius = getExtraScaling(info, "radius");
        float soundRadius = (float) (3 + radius);

        XSound.BLOCK_BEACON_ACTIVATE.play(player.getLocation(), (float) radius, XSound.DEFAULT_PITCH);
        new BukkitRunnable() {
            final Location location = player.getLocation();
            final ParticleDisplay barrier = ParticleDisplay.simple(location, Particle.PORTAL);
            final double count = Math.min(radius * 5, 50);
            double repeat = duration;

            @Override
            public void run() {
                XParticle.sphere(radius, count, barrier);
                XSound.BLOCK_PORTAL_AMBIENT.play(location, (float) radius, 1.0f);
                if ((repeat -= decreamentFactor) <= 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 10L);

        new BukkitRunnable() {
            final List<PotionEffect> effects = getEffects(info, "effects");
            final List<PotionEffect> friendlyEffects = getEffects(info, "friendly-effects");
            final double kb = getExtraScaling(info, "knockback");
            final double damage = getExtraScaling(info, "damage");
            final Vector velocity = player.getVelocity();
            double repeat = duration;

            @Override
            public void run() {
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (EntityUtil.isInvalidEntity(entity)) continue;
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (ServiceHandler.areFriendly(player, entity)) {
                        livingEntity.addPotionEffects(friendlyEffects);
                        continue;
                    }

                    if (damage != 0) livingEntity.damage(damage, player);
                    livingEntity.addPotionEffects(effects);
                    if (kb >= 0) livingEntity.setVelocity(livingEntity.getVelocity().subtract(velocity).multiply(kb));
                    XSound.ENTITY_ENDERMAN_SCREAM.play(livingEntity.getLocation(), soundRadius, 1f);
                }
                if ((repeat -= decreamentFactor) <= 0) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0L, frequency);
    }
}
