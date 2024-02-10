package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.utils.EntityUtil;
import org.skills.utils.LocationUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class VergilJudgementCut extends InstantActiveAbility {
    private static final Cache<UUID, Integer> PERFECT_JUDGEMENT_CUTS = CacheBuilder.newBuilder()
            .expireAfterAccess(500, TimeUnit.MILLISECONDS).build();

    public VergilJudgementCut() {
        super("Vergil", "judgement_cut");
        setPvPBased(true);
    }

    public static BukkitTask helix(Plugin plugin, int strings, double radius, double rate, double extension, int height, int speed,
                                   boolean fadeUp, boolean fadeDown, ParticleDisplay display, Entity target) {
        BooleanSupplier helix = helix(strings, radius, rate, extension, height, speed, fadeUp, fadeDown, display, target);
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (!helix.getAsBoolean()) cancel();
            }
        }.runTaskTimerAsynchronously(plugin, 0, speed);
    }

    public static BooleanSupplier helix(int strings, double radius, double rate, double extension, int length, int speed,
                                        boolean fadeUp, boolean fadeDown, ParticleDisplay display, Entity target) {
        // Player's eye location is the starting location for the particle
        Location startLoc = display.getLocation();

        // We need to clone() this location, because we will add() to it later.
        Location particleLoc = startLoc.clone();

        World world = startLoc.getWorld(); // We need this later to show the particle

        // dir is the Vector direction (offset from 0,0,0) the player is facing in 3D space
        Vector dir = startLoc.getDirection();

        return new BooleanSupplier() {
            double particleDistance = 1;
            final int maxBeamLength = 30; // Max beam length
            final int beamLength = 0; // Current beam length
            final double targetHeight = 0.0; // Height of the target entity. Used for tracking to the exact center.

            final int ticks = 0; // Tick counter
            final int ticksPerParticle = 3; // How many ticks per particle


            final boolean fadeDown = false;
            final boolean fadeUp = false;
            final double length = 10;
            final double rate = 0.1;
            final double radius = 1;
            final int strings = 2;
            final double distanceBetweenEachCirclePoints = XParticle.PII / strings;
            final double radiusDiv = radius / (length / rate);
            final double radiusDiv2 = fadeUp && fadeDown ? radiusDiv * 2 : radiusDiv;
            double dynamicRadius = fadeDown ? 0 : radius;
            boolean center = !fadeDown;
            final double rotationRate = distanceBetweenEachCirclePoints / 7;
            double rotation = 0;


            // The run() function runs every X number of ticks - see below
            public boolean getAsBoolean() {
                // vecOffset is used to determine where the next particle should appear
                Vector vecOffset = null;

                // Once the beam has traveled 3 blocks, start homing towards the closest entity
                // We have a target! Adjust vector to point to this entity
                if (beamLength >= 6 && target != null) {
                    // Target the center of the target entity
                    Location targetLoc = target.getLocation().clone().add(0, targetHeight / 2, 0);

                    // Get the current particle trajectory
                    Vector particleDirection = particleLoc.getDirection();
                    // Calc the vector halfway between the projectile and the target.
                    Vector inBetween = targetLoc.clone().subtract(particleLoc).toVector().normalize();

                    double accuracy = 0.5;

                    // If the distance between the particle and the target is 5 or less, tighten the curve towards the target
                    //  and speed up the particle slightly
                    double distance = particleLoc.distance(targetLoc);
                    // Maths FTW! This creates a nice effect where the closer it gets to the target, the tighter the curve
                    // Returns a nice percentage number between 0.06 and .90 that we then multiply by 0.5 and add that to 0.5
                    accuracy = accuracy * Math.pow(0.6, distance) + 0.5;

                    // Now adjust the distance between particles to prevent circling of targets
                    particleDistance = 0.5 - (0.5 * accuracy);

                    // Add the now multiplied "in between" vector to the projectile's direction vector and then normalize it
                    inBetween.multiply(accuracy);
                    particleDirection.add(inBetween).normalize();
                    vecOffset = particleDirection.clone();
                    // Need to set the new direction, otherwise direction resumes to before tracking direction
                    particleLoc.setDirection(particleDirection);
                } else {
                    // No target. Continue moving in the previous direction
                    vecOffset = particleLoc.getDirection().clone().multiply(particleDistance);
                }

                // Now we add the direction vector offset to the particle's current location

                particleLoc.add(vecOffset);

                if (!center) {
                    dynamicRadius += radiusDiv2;
                    if (dynamicRadius >= radius) center = true;
                } else if (fadeUp) dynamicRadius -= radiusDiv2;

                for (double i = 0; i < strings; i++) {
                    // 2D cirlce points.
                    double angle = (i * distanceBetweenEachCirclePoints) + rotation;
                    double x = 2 * Math.cos(angle);
                    double z = 2 * Math.sin(angle);
                    Location facing = particleLoc.clone();
                    facing.setPitch(facing.getPitch() - 20);
                    facing.setYaw(facing.getYaw() + 20);
                    ParticleDisplay.of(Particle.FLAME).withLocation(particleLoc.clone())
                            .face(facing).spawn(x, 0, z);
                }

                rotation += rotationRate;
                return true;
            }
        };
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SLogger.info("passed 1");
        World world = player.getWorld();

        SLogger.info("passed 2");
        Entity target;
        EntityDamageByEntityEvent lastDamage = DamageManager.getLastDamager(player, true);
        if (lastDamage == null) {
            EntityDamageByEntityEvent lastHit = DamageManager.getLastHitEntity(player);
            if (lastHit != null) target = lastHit.getEntity();
            else return;
        } else {
            target = lastDamage.getDamager();
        }

        SLogger.info("passed 3");
        RayTraceResult trace = world.rayTrace(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                50, FluidCollisionMode.ALWAYS, true, 2, null);

        if (trace != null) {
            SLogger.info("ray: " + trace.getHitBlock() + ' ' + trace.getHitEntity());
            //return;
        }

        SLogger.info("passed 4");
        //if (checkup(player) == null) return;

        Location loc = target.getLocation();
//        Plugin plugin, int strings, double radius, double rate, double extension, int height, int speed,
//        boolean fadeUp, boolean fadeDown, ParticleDisplay display
        dustToDust(player, target, loc, 1);
    }

    public void dustToDust(Player player, Entity target, Location loc, int levels) {
        XSound.ENTITY_WARDEN_SONIC_BOOM.play(player.getEyeLocation());
        if (player.isSneaking()) {
            AAAA.particleTutorial(player, target);
            return;
        }

        World world = player.getWorld();
        XSound.ENTITY_WARDEN_DIG.play(player.getEyeLocation());
        double distance = LocationUtils.distance(player.getLocation(), loc);
        ParticleDisplay lineDisplay = ParticleDisplay.of(Particle.FLAME).withCount(1).withLocation(player.getEyeLocation());
        ParticleDisplay helixDipslay = ParticleDisplay.of(Particle.FLAME).withCount(1).withLocation(player.getEyeLocation()).face(player);
        BukkitTask stop = helix(SkillsPro.get(), 2, 1, 0.7, 1, 100, 1, false, false, helixDipslay, target);

        // Plugin plugin, int strings, double radius, double rate,
        //                                   double extension, double height, double speed, double rotationRate,
        //                                   boolean fadeUp, boolean fadeDown, ParticleDisplay display
        //XParticle.helix(SkillsPro.get(), 2, 1, 0.7, 1, 100, 1, 5, false, false, helixDipslay.clone().withParticle(Particle.END_ROD).withLocation(player.getLocation()));
        XParticle.helix(SkillsPro.get(), 2, 2, 0.1, 1, 5, 0.1, 20, false, false,
                ParticleDisplay.of(Particle.DRAGON_BREATH).withLocation(player.getLocation()).withDirection(new Vector(0, 1, 0)));

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {

            SLogger.info("passed 5");
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            for (int i = 0; i < 10; i++) {
                double offset = 4;
                Location randomStart = loc.clone().add(rand.nextDouble(-offset, offset), rand.nextDouble(-offset, offset), rand.nextDouble(-offset, offset));
                Location randomEnd = loc.clone().add(rand.nextDouble(-offset, offset), rand.nextDouble(-offset, offset), rand.nextDouble(-offset, offset));
                XParticle.line(randomStart, randomEnd, 0.3, lineDisplay);
            }

            double damageRadius = 5;
            for (Entity entity : world.getNearbyEntities(loc, damageRadius, damageRadius, damageRadius)) {
                if (!EntityUtil.filterEntity(player, entity)) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    livingEntity.damage(1);
                }
            }

            XSound.ENTITY_WARDEN_AGITATED.play(loc);
            stop.cancel();
            //XParticle.sphere(3, Math.PI / 50, ParticleDisplay.of(Particle.FLAME).withLocation(loc.clone()).directional());
        }, 20L * 3L);

        if (levels < 4) {
            Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                ParticleDisplay.of(Particle.FLAME)
                        .withCount(30).offset(0.1)
                        .spawn(LocationUtils.getHandLocation(player, false));
            }, 20L);
        }
    }

    @EventHandler
    public void onSkillActivate(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Integer levels = PERFECT_JUDGEMENT_CUTS.getIfPresent(player.getUniqueId());
        if (levels == null) return;

        if (levels == 4) { // Jackpot!
            XSound.ENTITY_WARDEN_EMERGE.play(player.getLocation());
            PERFECT_JUDGEMENT_CUTS.invalidate(player.getUniqueId());
        } else {
            PERFECT_JUDGEMENT_CUTS.put(player.getUniqueId(), levels + 1);
        }
    }
}
