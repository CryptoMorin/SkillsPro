package org.skills.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.skills.main.SkillsPro;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {
    public static final BlockFace[] AXIS = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static final BlockFace[] RADIAL = {BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};
    private static final double ROUND_SCALING = Math.pow(10, 1);

    public static void faceOther(Entity entity, Entity other) {
        entity.getLocation().setDirection(entity.getLocation().toVector().subtract(other.getLocation().toVector()));
    }

    private static double roundToDigits(double value) {
        return Math.round(value * ROUND_SCALING) / ROUND_SCALING;
    }

    public static Location roundLocationPrecision(Location location) {
        return new Location(location.getWorld(),
                roundToDigits(location.getX()), roundToDigits(location.getY()), roundToDigits(location.getZ()),
                (float) roundToDigits(location.getYaw()), (float) roundToDigits(location.getPitch()));
    }

    public static double distanceSquared(Location start, Location end) {
        return Math.sqrt(
                NumberConversions.square(start.getX() - end.getX()) +
                        NumberConversions.square(start.getY() - end.getY()) +
                        NumberConversions.square(start.getZ() - end.getZ())
        );
    }

    public static String toReadableLocation(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public static String toReadableWorldLocation(Location location) {
        return location.getWorld().getName() + ", " + toReadableLocation(location);
    }

    public static void whoooosh(Entity entity, Location location, double xMod, double yMod, double zMod) {
        Location loc = entity.getLocation();
        loc.setY(loc.getY() + 0.5);
        //entity.teleport(loc);

        double gravity = -0.1D;
        double dist = location.distance(loc);
        double x = (1 + xMod * dist) * (location.getX() - loc.getX()) / dist;
        double y = (1 + yMod * dist) * (location.getY() - loc.getY()) / dist - 0.5 * gravity * dist;
        double z = (1 + zMod * dist) * (location.getZ() - loc.getZ()) / dist;

        Vector vector = new Vector(x, y, z);
        entity.setVelocity(vector);
    }

    /**
     * https://www.spigotmc.org/threads/getting-location-of-players-right-hand.185095/#post-1946347
     *
     * @param entity  The entity to get the hand location from.
     * @param offhand whether the location should be the player's offhand or main hand.
     *
     * @return location of the entity's hand.
     */
    public static Location getHandLocation(LivingEntity entity, boolean offhand) {
        Location location = entity.getLocation();
        double hand = offhand ? -1 : 1;
        double yawHandDirection = Math.toRadians(hand * location.getYaw() - 45);

        double x = 0.5 * Math.sin(yawHandDirection) + location.getX();
        double y = location.getY() + 0.7;
        double z = 0.5 * Math.cos(yawHandDirection) + location.getZ();

        return new Location(entity.getWorld(), x, y, z);
    }

    public static BukkitTask rotate(Entity entity, float rate, boolean yaw, boolean pitch, int repeat) {
        return new BukkitRunnable() {
            int times = repeat;
            float yawF = entity.getLocation().getYaw();
            float pitchF = entity.getLocation().getPitch();

            @Override
            public void run() {
                Location loc = entity.getLocation();
                times--;
                if (yaw) {
                    yawF += rate;
                    loc.setYaw(yawF);
                }
                if (pitch) {
                    pitchF += rate;
                    loc.setPitch(pitchF);
                }

                entity.teleport(loc);
                if (times <= 0) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 1L);
    }

    public static Location centerAxis(Location location) {
        Location loc = location.clone();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);
        return loc;
    }

    public static Location centerView(Location location) {
        Location loc = location.clone();
        loc.setYaw(centerYaw(loc.getYaw()));
        loc.setPitch(0);
        return loc;
    }

    public static Location cleanLocation(Location location) {
        Location loc = location.clone();
        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);

        loc.setYaw(centerYaw(loc.getYaw()));
        loc.setPitch(0);
        //new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, centerYaw(loc.getYaw()), 0);
        return loc;
    }

    public static Location getSafeLocation(Location location) {
        Location loc = location.clone();
        loc = centerAxis(loc);

        loc.setYaw((float) MathUtils.roundToDigits(loc.getYaw(), 1));
        loc.setPitch((float) MathUtils.roundToDigits(loc.getPitch(), 1));
        return loc;
    }

    public static boolean hasMoved(Location from, Location to) {
        return (from.getX() != to.getX()) || (from.getY() != to.getY()) || (from.getZ() != to.getZ());
    }

    public static boolean hasMovedABlock(Location from, Location to) {
        return (from.getBlockX() != to.getBlockX()) ||
                (from.getBlockY() != to.getBlockY()) ||
                (from.getBlockZ() != to.getBlockZ());
    }

    public static BlockFace yawToFaceRadial(float yaw) {
        return RADIAL[Math.round(yaw / 45.0f) & 0x7];
    }

    public static BlockFace yawToFace(float yaw) {
        return AXIS[Math.round(yaw / 90.0f) & 0x3];
    }

    public static float centerYaw(float yaw) {
        return faceToYaw(yawToFace(yaw));
    }

    public static float faceToYaw(BlockFace face) {
        switch (face) {
            case NORTH_EAST:
                return 45;
            case EAST:
                return 90;
            case SOUTH_EAST:
                return 135;
            case SOUTH:
                return 180;
            case SOUTH_WEST:
                return 225;
            case WEST:
                return 270;
            case NORTH_WEST:
                return 315;
            default:
                return 0; // NORTH
        }
    }

    public static BlockFace pitchToFace(float pitch) {
        return pitch < -25 ? BlockFace.UP : pitch > 25 ? BlockFace.DOWN : null;
    }

    public static float compareDirection(Location first, Location second) {
        return (float) Math.toDegrees(Math.atan2(first.getBlockX() - second.getX(), second.getZ() - first.getBlockZ()));
    }

    /**
     * Gets the BlockFace of the block the player is currently targeting.
     *
     * @param entity the entity's whos targeted blocks BlockFace is to be checked.
     *
     * @return the BlockFace of the targeted block, or null if the targeted block is non-occluding.
     */
    public static BlockFace getEntityBlockFace(LivingEntity entity) {
        List<Block> lastTwoTargetBlocks = entity.getLastTwoTargetBlocks(null, 6);
        // !lastTwoTargetBlocks.get(1).getType().isOccluding()
        if (lastTwoTargetBlocks.size() != 2) return null;

        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    public static BlockFace getyawToFace(float yaw) {
        yaw %= 360;
        if (yaw < 0) yaw += 360;
        yaw = Math.round(yaw / 45);

        switch ((int) yaw) {
            case 1:
                return BlockFace.NORTH_WEST;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.NORTH_EAST;
            case 4:
                return BlockFace.EAST;
            case 5:
                return BlockFace.SOUTH_EAST;
            case 6:
                return BlockFace.SOUTH;
            case 7:
                return BlockFace.SOUTH_WEST;
            default:
                return BlockFace.WEST;
        }
    }

    public static List<Chunk> getChunksAround(Location location, int radius) {
        World world = location.getWorld();
        int baseX = location.getChunk().getX();
        int baseZ = location.getChunk().getZ();

        List<Chunk> chunks = new ArrayList<>();
        for (int x = baseX - radius; x < baseX + radius; x++) {
            for (int z = baseZ - radius; z < baseZ + radius; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }
}
