package org.skills.abilities.swordsman;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
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
import org.skills.utils.EntityUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SwordsmanDash extends InstantActiveAbility {
    public SwordsmanDash() {
        super("Swordsman", "dash");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        new BukkitRunnable() {
            final ParticleDisplay display = ParticleDisplay.simple(null, Particle.CLOUD).withCount(10).offset(0.5, 0.2, 0.5);
            int repeat = 20;

            @Override
            public void run() {
                display.spawn(player.getLocation());
                if (--repeat == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0, 1);

        int lvl = info.getAbilityLevel(SwordsmanDash.this);
        if (lvl >= 3) {
            new BukkitRunnable() {
                final List<PotionEffect> effects = getEffects(info, "effects");
                final double range = getScaling(info, "range");
                final double kb = getScaling(info, "knockback");
                final double damage = getScaling(info, "damage");
                final ParticleDisplay masterDisplay = ParticleDisplay.simple(null, Particle.SWEEP_ATTACK).withCount(10).offset(1);
                final Set<Integer> targets = new HashSet<>();
                int repeat = 20;

                @Override
                public void run() {
                    for (Entity entity : player.getNearbyEntities(range, range, range)) {
                        if (EntityUtil.filterEntity(player, entity)) continue;
                        LivingEntity livingEntity = (LivingEntity) entity;
                        if (!targets.add(entity.getEntityId())) continue;

                        livingEntity.addPotionEffects(effects);
                        livingEntity.setVelocity(livingEntity.getVelocity().subtract(player.getVelocity()).multiply(kb));
                        livingEntity.damage(damage, player);

                        Location loc = livingEntity.getLocation();
                        XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(loc, 3f, 1.0f);
                        masterDisplay.spawn(loc);
                    }
                    if ((repeat -= 2) == 0) cancel();
                }
            }.runTaskTimer(SkillsPro.get(), 0, 2);
        }

        int verticalSlashLvl = (int) getScaling(info, "vertical-slash-level");
        Vector dir = player.getLocation().getDirection();
        Vector direction = new Vector(dir.getX(), (lvl >= verticalSlashLvl ? dir.getY() : 0), dir.getZ()).normalize();
        double mod = getScaling(info, "velocity");
        player.setVelocity(direction.multiply(mod));
    }
}