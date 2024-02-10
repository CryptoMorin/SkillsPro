package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.utils.EntityUtil;
import org.skills.utils.LocationUtils;
import org.skills.utils.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class VergilJudgementCutEnd extends InstantActiveAbility {
    private static final Map<UUID, Location> PERFORMING = new HashMap<>();

    public VergilJudgementCutEnd() {
        super("Vergil", "judgement_cut_end");
        setPvPBased(true);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();

        double radius = 10;
        Location startLocation = player.getLocation();
        player.setInvulnerable(true);
        ParticleDisplay trial = ParticleDisplay.of(Particle.TRIAL_SPAWNER_DETECTION).withCount(1000).offset(radius).withLocation(player.getEyeLocation());
        ParticleDisplay spore = ParticleDisplay.of(Particle.WARPED_SPORE).withCount(1000).offset(radius).withLocation(player.getEyeLocation().add(0, 3, 0));
        ParticleDisplay display = ParticleDisplay.of(Particle.FLAME).withLocation(player.getEyeLocation());

        new BukkitRunnable() {
            int times = 10;

            @Override
            public void run() {
                trial.spawn();
                spore.spawn();
                XParticle.sphere(radius, 30, display);
                if (--times == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 20L);
        PERFORMING.put(player.getUniqueId(), startLocation);

        ParticleDisplay lineDisplay = ParticleDisplay.of(Particle.FLAME).withLocation(player.getEyeLocation());

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            List<Pair<LivingEntity, Runnable>> targetedEntities = new ArrayList<>();
            for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                if (EntityUtil.filterEntity(player, nearby)) continue;
                LivingEntity living = (LivingEntity) nearby;

                if (nearby instanceof Player) {
                    Player nearbyPlayer = (Player) nearby;
                    float walkSpeed = nearbyPlayer.getWalkSpeed();
                    float flySpeed = nearbyPlayer.getFlySpeed();

                    nearbyPlayer.setWalkSpeed(-100);
                    nearbyPlayer.setFlySpeed(-100);
                    nearby.setGravity(false);

                    targetedEntities.add(Pair.of(nearbyPlayer, () -> {
                        nearbyPlayer.setWalkSpeed(walkSpeed);
                        nearbyPlayer.setFlySpeed(flySpeed);
                        nearbyPlayer.setGravity(true);
                    }));
                } else {
                    living.setAI(false);
                    nearby.setGravity(false);
                    targetedEntities.add(Pair.of(living, () -> {
                        living.setAI(true);
                        nearby.setGravity(true);
                    }));
                }
            }

            new BukkitRunnable() {
                final int slashes = 20;
                final ThreadLocalRandom rand = ThreadLocalRandom.current();
                static final int randomRange = 3;
                final Iterator<LivingEntity> iteratedTargets = targetedEntities.stream().map(Pair::getKey).iterator();

                @Override
                public void run() {
                    //slashes--;
//                    player.setVelocity(new Vector(
//                            rand.nextDouble(-randomRange, randomRange),
//                            rand.nextDouble(-randomRange, randomRange),
//                            rand.nextDouble(-randomRange, randomRange)));

                    if (!iteratedTargets.hasNext()) {
                        cancel();
                        player.setInvulnerable(false);
                        player.teleport(startLocation);
                        PERFORMING.remove(player.getUniqueId());

                        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                            ParticleDisplay damageDisplay = ParticleDisplay.of(Particle.SWEEP_ATTACK).withCount(20).offset(2);
                            for (Pair<LivingEntity, Runnable> targetEntry : targetedEntities) {
                                targetEntry.getValue().run();
                                LivingEntity target = targetEntry.getKey();
                                DamageManager.damage(target, null, 10);
                                XSound.ENTITY_PLAYER_ATTACK_SWEEP.record().soundPlayer().atLocation(target.getLocation()).play();
                                damageDisplay.spawn(target.getLocation());
                                target.setVelocity(target.getLocation().toVector()
                                        .subtract(player.getLocation().toVector()).normalize().multiply(3));
                            }
                        }, 20L * 3L);
                    } else {
                        Location nextLoc = iteratedTargets.next().getLocation();
                        player.setVelocity(nextLoc.toVector().subtract(player.getLocation().toVector()).normalize().multiply(3));
                    }
                }
            }.runTaskTimer(SkillsPro.get(), 0L, 5L);
        }, 20L * 7L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onOutOfBounds(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location startLoc = PERFORMING.get(player.getUniqueId());
        if (startLoc != null) {
            if (LocationUtils.distanceSquared(startLoc, player.getLocation()) >= 15) {
                player.setVelocity(player.getVelocity().multiply(-2));
            }
        }
    }
}
