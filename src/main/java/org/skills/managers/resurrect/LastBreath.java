package org.skills.managers.resurrect;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.LocationUtils;
import org.spigotmc.event.entity.EntityMountEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import static com.cryptomorin.xseries.ReflectionUtils.*;

public class LastBreath implements Listener {
    protected static final int VIEW_DISTANCE = 100, ENTITY_POSE_REGISTRY = 6;
    private static final Object ENTITY_POSE_SWIMMING, ENTITY_POSE_STANDING, DATA_WATCHER_REGISTRY;
    private static final MethodHandle PACKET_PLAY_OUT_ENTITY_METADATA, CREATE_DATA_WATCHER, GET_DATA_WATCHER, DATA_WATCHER_SET;
    private static final Map<Integer, LastManStanding> LAST_MEN_STANDING = new HashMap<>(), REVIVERS = new HashMap<>();

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object entityPoseSwimming = null, entityPoseStanding = null, dataWatcherRegistry = null;
        MethodHandle packetPlayOutEntityMetadata = null, createDataWatcher = null, getDataWatcher = null, dataWatcherSet = null;

        Class<?> entityPose = getNMSClass("EntityPose");
        Class<?> dataWatcher = getNMSClass("DataWatcher");
        Class<?> entityPlayer = getNMSClass("EntityPlayer");
        Class<?> dataWatcherObjectClass = getNMSClass("DataWatcherObject");
        Class<?> dataWatcherRegistryClass = getNMSClass("DataWatcherRegistry");
        Class<?> dataWatcherSerializerClass = getNMSClass("DataWatcherSerializer");
        Class<?> packetPlayOutEntityMetadataClass = getNMSClass("PacketPlayOutEntityMetadata");

        try {
            dataWatcherRegistry = lookup.findStaticGetter(dataWatcherRegistryClass, "s", dataWatcherSerializerClass).invoke();
            entityPoseSwimming = entityPose.getDeclaredField("SWIMMING").get(null);
            entityPoseStanding = entityPose.getDeclaredField("STANDING").get(null);

            getDataWatcher = lookup.findVirtual(entityPlayer, "getDataWatcher", MethodType.methodType(dataWatcher));
            dataWatcherSet = lookup.findVirtual(dataWatcher, "set", MethodType.methodType(void.class, dataWatcherObjectClass, Object.class));
            createDataWatcher = lookup.findConstructor(dataWatcherObjectClass,
                    MethodType.methodType(void.class, int.class, dataWatcherSerializerClass));
            packetPlayOutEntityMetadata = lookup.findConstructor(packetPlayOutEntityMetadataClass,
                    MethodType.methodType(void.class, int.class, dataWatcher, boolean.class));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        ENTITY_POSE_SWIMMING = entityPoseSwimming;
        ENTITY_POSE_STANDING = entityPoseStanding;
        DATA_WATCHER_REGISTRY = dataWatcherRegistry;

        CREATE_DATA_WATCHER = createDataWatcher;
        DATA_WATCHER_SET = dataWatcherSet;
        GET_DATA_WATCHER = getDataWatcher;
        PACKET_PLAY_OUT_ENTITY_METADATA = packetPlayOutEntityMetadata;
    }

    static {
        Bukkit.getScheduler().runTaskTimer(SkillsPro.get(), () -> {
            for (LastManStanding lastStanding : LAST_MEN_STANDING.values()) {
                Location location = lastStanding.player.getLocation();
                Object metadata = registerDataWatcher(lastStanding.player, true);
                for (Player player : lastStanding.player.getWorld().getPlayers()) {
                    if (player != lastStanding.player && LocationUtils.distanceSquared(location, player.getLocation()) < VIEW_DISTANCE) {
                        sendPacket(player, metadata);
                    }
                }
            }
        }, 100L, 1L);
    }

    protected static Object registerDataWatcher(Player player, boolean swimming) {
        try {
            Object handle = getHandle(player);
            Object watcher = GET_DATA_WATCHER.invoke(handle);
            Object registry = CREATE_DATA_WATCHER.invoke(ENTITY_POSE_REGISTRY, DATA_WATCHER_REGISTRY);

            DATA_WATCHER_SET.invoke(watcher, registry, swimming ? ENTITY_POSE_SWIMMING : ENTITY_POSE_STANDING);
            return PACKET_PLAY_OUT_ENTITY_METADATA.invoke(player.getEntityId(), watcher, true);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    protected static void cover(Player player, Location location) {
        player.sendBlockChange(location, Material.BARRIER, (byte) 0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwim(EntityToggleSwimEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStruggle(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) return;
        if (!LAST_MEN_STANDING.containsKey(entity.getEntityId())) return;
        event.setDamage(SkillsConfig.LAST_BREATH_DAMAGE.getDouble());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeathOrDamage(EntityDamageEvent event) {
        switch (event.getCause()) {
            case SUICIDE:
            case SUFFOCATION:
            case STARVATION:
            case LAVA:
            case VOID:
                return;
        }

        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        double diff = event.getFinalDamage() - player.getHealth();
        if (diff < 0) return;
        if (LAST_MEN_STANDING.remove(player.getEntityId()) != null) return;
        if (diff > SkillsConfig.LAST_BREATH_INTENSITY_RESISTANCE.getDouble()) return;

        LastManStanding lastMan = new LastManStanding(player);
        LAST_MEN_STANDING.put(player.getEntityId(), lastMan);

        event.setDamage(player.getHealth() - 1.0);
        player.setSwimming(true);
        player.setSprinting(true);
        player.setGameMode(GameMode.ADVENTURE);
        player.setWalkSpeed(0.1f);

        Location location = player.getLocation().add(0, 1, 0);
        cover(player, location);

        Entity vehicle = player.getVehicle();
        if (vehicle != null) vehicle.eject();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getPlayer().getEntityId())) event.setCancelled(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        LastManStanding lastMan = LAST_MEN_STANDING.remove(player.getEntityId());
        if (lastMan != null) {
            player.setHealth(0);
            lastMan.end();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getPlayer().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMount(EntityMountEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && LAST_MEN_STANDING.containsKey(event.getTarget().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegenHP(EntityRegainHealthEvent event) {
        if (LAST_MEN_STANDING.containsKey(event.getEntity().getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (LAST_MEN_STANDING.remove(player.getEntityId()) != null) {
            player.setHealth(0);
            return;
        }
        LastManStanding reviver;
        if (!event.isSneaking() && (reviver = REVIVERS.remove(player.getEntityId())) != null) {
            reviver.resetProgress();
            ParticleDisplay.simple(player.getLocation(), Particle.SMOKE_LARGE).withCount(30).offset(0.5, 0.5, 0.5).spawn();
            return;
        }

        double lastDist = Double.MAX_VALUE;
        LastManStanding closest = null;
        double reviveDist = SkillsConfig.LAST_BREATH_REVIVE_DISTANCE.getDouble();
        Location loc = player.getLocation();
        for (Entity entity : player.getNearbyEntities(reviveDist, reviveDist, reviveDist)) {
            LastManStanding lastMan = LAST_MEN_STANDING.get(entity.getEntityId());
            if (lastMan != null) {
                double dist = loc.distanceSquared(entity.getLocation());
                if (dist < lastDist) {
                    closest = lastMan;
                    lastDist = dist;
                }
            }
        }

        if (closest == null) return;

        REVIVERS.put(player.getEntityId(), closest);
        closest.reviver = player;
        closest.progress++;
        LastManStanding finLastMan = closest;
        closest.reviveTask = new BukkitRunnable() {
            final ParticleDisplay display = ParticleDisplay.simple(null, Particle.VILLAGER_HAPPY).withCount(20).offset(1, 1, 1);
            final int maxProgress = SkillsConfig.LAST_BREATH_REVIVE_TIME.getInt();

            @Override
            public void run() {
                display.spawn(finLastMan.player.getLocation());
                if (finLastMan.progress() >= maxProgress) {
                    LAST_MEN_STANDING.remove(finLastMan.player.getEntityId());
                    finLastMan.recover();
                    cancel();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (!LocationUtils.hasMovedABlock(event.getFrom(), event.getTo())) return;

        Player player = event.getPlayer();
        if (!LAST_MEN_STANDING.containsKey(player.getEntityId())) return;

        Block to = event.getTo().getBlock();
        Block toBarrier = to.getRelative(BlockFace.UP);
        if (toBarrier.getType() == Material.AIR || !toBarrier.getType().isSolid()) {
            if (!to.isLiquid() && !toBarrier.isLiquid()) cover(player, toBarrier.getLocation());

            Block from = event.getFrom().getBlock().getRelative(BlockFace.UP);
            player.sendBlockChange(from.getLocation(), from.getBlockData());
        }
    }
}
