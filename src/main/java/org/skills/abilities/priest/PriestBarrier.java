package org.skills.abilities.priest;

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
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.EntityUtil;

import java.util.List;

public class PriestBarrier extends InstantActiveAbility {
    public PriestBarrier() {
        super("Priest", "barrier");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();
        info.setActiveAbilitiy(this, true);

        double duration = getScaling(info, "duration");
        int frequency = (int) getScaling(info, "frequency");
        double decreamentFactor = 1D / (20D / frequency);

        double radius = getScaling(info, "radius");
        playSound(player, info, "start");

        new BukkitRunnable() {
            final Location location = player.getLocation();
            final ParticleDisplay barrier = ParticleDisplay.simple(location, Particle.PORTAL);
            final double count = Math.min(radius * 5, 50);
            double repeat = duration;

            @Override
            public void run() {
                XParticle.sphere(radius, count, barrier);
                playSound(player, info, "end");
                if ((repeat -= decreamentFactor) <= 0) {
                    cancel();
                    info.setActiveAbilitiy(PriestBarrier.this, false);
                }
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 10L);

        new BukkitRunnable() {
            final List<PotionEffect> effects = getEffects(info, "effects");
            final List<PotionEffect> friendlyEffects = getEffects(info, "friendly-effects");
            final double kb = getScaling(info, "knockback");
            final double damage = getScaling(info, "damage");
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
                    if (kb >= 0) livingEntity.setVelocity(EntityUtil.validateExcessiveVelocity(livingEntity.getVelocity().subtract(velocity).multiply(kb)));
                    playSound(player, info, "barrier");
                }
                if ((repeat -= decreamentFactor) <= 0) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0L, frequency);
    }
}
