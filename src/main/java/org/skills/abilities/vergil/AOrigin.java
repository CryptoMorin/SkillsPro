package org.skills.abilities.vergil;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.main.SkillsPro;

public class AOrigin {

    public static void particleTutorial(Player player, Entity target) {
        // Player's eye location is the starting location for the particle
        Location startLoc = player.getEyeLocation();

        // We need to clone() this location, because we will add() to it later.
        Location particleLoc = startLoc.clone();

        World world = startLoc.getWorld(); // We need this later to show the particle

        // dir is the Vector direction (offset from 0,0,0) the player is facing in 3D space
        Vector dir = startLoc.getDirection();

        new BukkitRunnable() {
            double particleDistance = 1;
            final int maxBeamLength = 30; // Max beam length
            int beamLength = 0; // Current beam length
            final double targetHeight = 0.0; // Height of the target entity. Used for tracking to the exact center.

            final int ticks = 0; // Tick counter
            final int ticksPerParticle = 3; // How many ticks per particle


            final boolean fadeDown = false;
            final boolean fadeUp = false;
            final double length = 10;
            final double rate = 0.1;
            final double radius = 1;
            final int strings = 2;
            final double distanceBetweenEachCirclePoints = Particles.PII / strings;
            final double radiusDiv = radius / (length / rate);
            final double radiusDiv2 = fadeUp && fadeDown ? radiusDiv * 2 : radiusDiv;
            double dynamicRadius = fadeDown ? 0 : radius;
            boolean center = !fadeDown;
            final double rotationRate = distanceBetweenEachCirclePoints / 5;
            double rotation = 0;


            // The run() function runs every X number of ticks - see below
            public void run() {
                // vecOffset is used to determine where the next particle should appear
                Vector vecOffset = null;

                // Once the beam has traveled 3 blocks, start homing towards the closest entity
                // We have a target! Adjust vector to point to this entity
                if (beamLength >= 6 && target != null) {
                    // Target the center of the target entity
                    Location targetLoc = target.getLocation().clone().add(0, targetHeight / 2, 0);

                    // Get the current particle trajectory
                    Vector particleDirection = particleLoc.getDirection();
                    // Calc the vector half way between the projectile and the target.
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

                beamLength++; // This is the current number of particles in the beam.

                // Kill this task if the beam length is max
                if (beamLength >= maxBeamLength) {
                    world.spawnParticle(XParticle.FLASH.get(), particleLoc, 0);
                    this.cancel();
                    return;
                }

                // Now we add the direction vector offset to the particle's current location

                particleLoc.add(vecOffset);

                if (!center) {
                    dynamicRadius += radiusDiv2;
                    if (dynamicRadius >= radius) center = true;
                } else if (fadeUp) dynamicRadius -= radiusDiv2;

                for (double i = 0; i < strings; i++) {
                    // 2D cirlce points.
                    double angle = i * distanceBetweenEachCirclePoints + rotation;
                    double x = dynamicRadius * Math.cos(angle);
                    double z = dynamicRadius * Math.sin(angle);
                    Location facing = particleLoc.clone();
                    facing.setPitch(facing.getPitch() - 20);
                    facing.setYaw(facing.getYaw() + 20);
                    ParticleDisplay.of(XParticle.FLAME).withLocation(particleLoc.clone())
                            .face(facing).spawn(x, 0, z);
                }

                rotation += rotationRate;
            }
        }.runTaskTimer(SkillsPro.get(), 0, 1);
        // 0 is the delay in ticks before starting this task
        // 1 is the how often to repeat the run() function, in ticks (20 ticks are in one second)
    }
}
