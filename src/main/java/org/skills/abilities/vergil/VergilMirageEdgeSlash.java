package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.utils.EntityUtil;
import org.skills.utils.ParticleUtil;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class VergilMirageEdgeSlash extends InstantActiveAbility {
    public VergilMirageEdgeSlash() {
        super("Vergil", "mirage_edge_slash");
        setPvPBased(true);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        Vector direction = player.getEyeLocation().getDirection().normalize();
        Vector perpendV = ParticleUtil.getPerpendicularVector(direction).normalize();

        Cache<Integer, Boolean> hitCooldown = CacheBuilder.newBuilder().expireAfterWrite(100, TimeUnit.MILLISECONDS).build();
        double beamDistance = context.getScaling("distance");
        double beamSize = context.getScaling("size");
        double damage = context.getScaling("damage");
        double knockback = context.getScaling("knockback");
        double range = context.getScaling("range");

        ParticleDisplay display = ParticleDisplay.of(XParticle.SOUL_FIRE_FLAME)
                //.withColor(Color.CYAN, 2)
                .withLocation(player.getEyeLocation())
                .face(player)
                .withExtra(0.1)
                .rotate(ParticleDisplay.Rotation.of(-(Math.PI / 2), perpendV))
                .postCalculation(ctx -> {
                    Location finalLoc1 = ctx.getLocation();
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                        for (Entity entity : finalLoc1.getWorld().getNearbyEntities(finalLoc1, range, range, range)) {
                            if (EntityUtil.filterEntity(player, entity)) continue;
                            if (hitCooldown.getIfPresent(entity.getEntityId()) == null) {
                                LivingEntity living = (LivingEntity) entity;
                                DamageManager.damage(living, player, damage);
                                EntityUtil.knockBack(living, direction, knockback);
                                XSound.ENTITY_WARDEN_ATTACK_IMPACT.record().withPitch(0.5f).soundPlayer().atLocation(living.getEyeLocation()).play();
                                hitCooldown.put(entity.getEntityId(), false);
                            }
                        }
                    });
                });

        AtomicDouble size = new AtomicDouble(beamSize);
        Particles.slash(SkillsPro.get(), beamDistance, true, () -> size.getAndAdd(-0.01), () -> 0.3, display);
        if (context.hasAbilityLevel(3)) {
            Particles.slash(SkillsPro.get(), beamDistance, true, size::get, () -> 0.3,
                    display.clone().withParticle(XParticle.DUST).withColor(Color.RED, 1f));
        }
        XSound.ENTITY_WARDEN_DEATH.record().withPitch(0).soundPlayer().atLocation(player.getEyeLocation()).play();
    }
}
