package org.skills.managers.resurrect;

import com.cryptomorin.xseries.NMSExtras;
import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.Cooldown;
import org.skills.utils.LocationUtils;
import org.skills.utils.MathUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cryptomorin.xseries.ReflectionUtils.*;

public final class LastBreath implements Listener {
    protected static final int VIEW_DISTANCE = 100, ENTITY_POSE_REGISTRY = 6;
    protected static final Map<Integer, LastManStanding> LAST_MEN_STANDING = new HashMap<>(), REVIVERS = new HashMap<>();
    private static final Object DATA_WATCHER_REGISTRY;
    private static final MethodHandle PACKET_PLAY_OUT_ENTITY_METADATA, CREATE_DATA_WATCHER, WATCHER_PACK;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object dataWatcherRegistry = null;
        MethodHandle packetPlayOutEntityMetadata = null, createDataWatcher = null, watcherPack = null;

        Class<?> dataWatcher = getNMSClass("network.syncher", "DataWatcher");
        Class<?> dataWatcherObjectClass = getNMSClass("network.syncher", "DataWatcherObject");
        Class<?> dataWatcherRegistryClass = getNMSClass("network.syncher", "DataWatcherRegistry");
        Class<?> dataWatcherSerializerClass = getNMSClass("network.syncher", "DataWatcherSerializer");
        Class<?> packetPlayOutEntityMetadataClass = getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");

        try {
            // public static final DataWatcherSerializer<NBTTagCompound> s;
            /*
             s = new DataWatcherSerializer<NBTTagCompound>() {
            public void a(PacketDataSerializer var0, NBTTagCompound var1) {
                var0.a(var1);
            }

            public NBTTagCompound b(PacketDataSerializer var0) {
                return var0.p();
            }

            public NBTTagCompound a(NBTTagCompound var0) {
                return var0.h();
            }
        };
             */
            dataWatcherRegistry = lookup.findStaticGetter(dataWatcherRegistryClass, ReflectionUtils.v(19, "s").v(13, "p").orElse("n"), dataWatcherSerializerClass).invoke();

            //     public DataWatcher al() {
            //        return this.Y;
            //    }
            createDataWatcher = lookup.findConstructor(dataWatcherObjectClass,
                    MethodType.methodType(void.class, int.class, dataWatcherSerializerClass));

            packetPlayOutEntityMetadata = lookup.findConstructor(packetPlayOutEntityMetadataClass,
                    v(19, MethodType.methodType(void.class, int.class, List.class))
                            .orElse(MethodType.methodType(void.class, int.class, dataWatcher, boolean.class)));

            if (ReflectionUtils.supports(19)) {
                // public @Nullable List<b<?>> b()
                watcherPack = lookup.findVirtual(dataWatcher, "b", MethodType.methodType(List.class));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        DATA_WATCHER_REGISTRY = dataWatcherRegistry;

        CREATE_DATA_WATCHER = createDataWatcher;
        PACKET_PLAY_OUT_ENTITY_METADATA = packetPlayOutEntityMetadata;
        WATCHER_PACK = watcherPack;
    }

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SkillsPro.get(), () -> {
            for (LastManStanding lastStanding : LAST_MEN_STANDING.values()) {
                Location location = lastStanding.player.getLocation();
                for (Player player : lastStanding.player.getWorld().getPlayers()) {
                    if (player != lastStanding.player && LocationUtils.distanceSquared(location, player.getLocation()) < VIEW_DISTANCE) {
                        sendPacketSync(player, lastStanding.dataWatcher);
                    }
                }
            }
        }, 100L, 1L);
    }

    protected static Object registerDataWatcher(Player player, boolean swimming) {
        try {
            Object handle = NMSExtras.getEntityHandle(player);
            Object watcher = NMSExtras.getDataWatcher(handle);
            Object registry = CREATE_DATA_WATCHER.invoke(ENTITY_POSE_REGISTRY, DATA_WATCHER_REGISTRY);

            Object pos = (swimming ? NMSExtras.EntityPose.SWIMMING : NMSExtras.EntityPose.STANDING).getEnumValue();
            NMSExtras.setData(watcher, registry, pos);

            if (ReflectionUtils.supports(19)) {
                return PACKET_PLAY_OUT_ENTITY_METADATA.invoke(player.getEntityId(), WATCHER_PACK.invoke(watcher));
            } else {
                return PACKET_PLAY_OUT_ENTITY_METADATA.invoke(player.getEntityId(), watcher, true);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    protected static void cover(Player player, Location location) {
        if (supports(13)) player.sendBlockChange(location, Material.BARRIER.createBlockData());
        else player.sendBlockChange(location, Material.BARRIER, (byte) 0);
    }

    private static void cancel(Cancellable event, Entity entity) {
        if (LAST_MEN_STANDING.containsKey(entity.getEntityId())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwim(EntityToggleSwimEvent event) {
        cancel(event, event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStruggle(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (!(entity instanceof Player)) return;
        if (!LAST_MEN_STANDING.containsKey(entity.getEntityId())) return;
        event.setDamage(SkillsConfig.LAST_BREATH_DAMAGE.getDouble());
    }

    /**
     * https://minecraft.fandom.com/wiki/Totem_of_Undying
     */
    public static void totemOfUndying(Player player) {
        // player.spawnParticle(XParticle.TOTEM, player.getEyeLocation(), 1); // Doesn't work
        // XSound.ITEM_TOTEM_USE.play(player); // Not needed, the line below takes care of that.
        player.playEffect(EntityEffect.TOTEM_RESURRECT);

        player.setHealth(player.getHealth() + 1);

        // player.getActivePotionEffects().clear(); // Doesn't work, it's a copy
        player.getActivePotionEffects().forEach(x -> player.removePotionEffect(x.getType()));

        player.addPotionEffect(XPotion.REGENERATION.buildPotionEffect(45 * 20, 2));
        player.addPotionEffect(XPotion.FIRE_RESISTANCE.buildPotionEffect(40 * 20, 1));
        player.addPotionEffect(XPotion.ABSORPTION.buildPotionEffect(5 * 20, 1));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeathOrDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Can't save them again if they die.
        LastManStanding lastMan = LAST_MEN_STANDING.remove(player.getEntityId());
        if (lastMan != null) {
            lastMan.resetState();
            return;
        }

        switch (event.getCause()) {
            case SUICIDE:
            case SUFFOCATION:
            case LAVA:
            case VOID:
            case DROWNING:
            case CUSTOM:
                return;
            case STARVATION:
                cancel(event, event.getEntity());
                break;
        }

        double hpLeft = player.getHealth() - event.getFinalDamage();
        if (hpLeft > 0) return; // They'll be alive

        if (ReflectionUtils.supports(11)) {
            boolean mainTotem = XMaterial.matchXMaterial(player.getInventory().getItemInMainHand()) == XMaterial.TOTEM_OF_UNDYING;
            boolean offTotem = XMaterial.matchXMaterial(player.getInventory().getItemInOffHand()) == XMaterial.TOTEM_OF_UNDYING;
            if (mainTotem || offTotem) {
                totemOfUndying(player);

                if (mainTotem) player.getInventory().setItemInMainHand(null);
                else player.getInventory().setItemInOffHand(null);

                event.setCancelled(true);
                return;
            }
        }

        if (Cooldown.isInCooldown(player.getUniqueId(), "LASTBREATH")) return;
        if (hpLeft > SkillsConfig.LAST_BREATH_INTENSITY_RESISTANCE.getDouble()) return;

        Entity vehicle = player.getVehicle();
        if (vehicle != null) vehicle.eject();

        lastMan = new LastManStanding(player);
        LAST_MEN_STANDING.put(player.getEntityId(), lastMan);

        event.setDamage(Math.max(player.getHealth() - 1.0, 0.01)); // Keep them alive
        cover(player, player.getLocation().add(0, 1, 0));
        new Cooldown(player.getUniqueId(), "LASTBREATH", SkillsConfig.LAST_BREATH_COOLDOWN.getTimeMillis());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSprint(PlayerToggleSprintEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        LastManStanding lastMan = LAST_MEN_STANDING.remove(player.getEntityId());
        if (lastMan != null) {
            player.setHealth(0);
            lastMan.resetState();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMount(EntityMountEvent event) {
        cancel(event, event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGlide(EntityToggleGlideEvent event) {
        cancel(event, event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() == null) return;
        if (!LAST_MEN_STANDING.containsKey(event.getTarget().getEntityId())) return;
        if (!SkillsConfig.LAST_BREATH_MOBS_IGNORE.getBoolean()) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegenHP(EntityRegainHealthEvent event) {
        cancel(event, event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsumeFood(PlayerItemConsumeEvent event) {
        cancel(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        LastManStanding lastManSuicidal = LAST_MEN_STANDING.get(player.getEntityId()); // removed in die()
        if (lastManSuicidal != null) {
            lastManSuicidal.die();
            return;
        }

        LastManStanding reviver;
        if (!event.isSneaking()) {
            reviver = REVIVERS.remove(player.getEntityId());
            if (reviver != null) {
                reviver.resetProgress();
                ParticleDisplay.of(XParticle.LARGE_SMOKE).withLocation(player.getLocation()).withCount(30).offset(0.5).spawn();
            }
            return;
        }

        double lastDist = Double.MAX_VALUE;
        LastManStanding closest = null;
        double reviveDist = SkillsConfig.LAST_BREATH_REVIVE_DISTANCE.getDouble();
        Location loc = player.getLocation();
        for (Entity entity : player.getNearbyEntities(reviveDist, reviveDist, reviveDist)) {
            if (!(entity instanceof Player)) continue;
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
        if (closest.reviver != null) return;

        REVIVERS.put(player.getEntityId(), closest);
        closest.reviver = player;
        closest.progress++;
        LastManStanding finLastMan = closest;
        closest.reviveTask = new BukkitRunnable() {
            final ParticleDisplay display = ParticleDisplay.of(XParticle.HAPPY_VILLAGER).withCount(20).offset(1);
            final int maxProgress = SkillsConfig.LAST_BREATH_REVIVE_TIME.getInt();

            @Override
            public void run() {
                display.spawn(finLastMan.player.getLocation());

                double progressPercent = MathUtils.getPercent(finLastMan.progress(), maxProgress);
                finLastMan.player.setFoodLevel((int) MathUtils.percentOfAmount(progressPercent, 20));

                if (finLastMan.progress >= maxProgress) {
                    LAST_MEN_STANDING.remove(finLastMan.player.getEntityId());
                    REVIVERS.remove(player.getEntityId());
                    finLastMan.revive();
                    cancel();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 20L);
    }

    public static boolean isLastBreaths(Player player) {
        return LAST_MEN_STANDING.containsKey(player.getEntityId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (!LocationUtils.hasMovedABlock(event.getFrom(), event.getTo())) return;

        Player player = event.getPlayer();
        if (!isLastBreaths(player)) return;

        Block to = event.getTo().getBlock();
        Block toBarrier = to.getRelative(BlockFace.UP);
        if (toBarrier.getType() == Material.AIR || !toBarrier.getType().isSolid()) {
            if (!to.isLiquid() && !toBarrier.isLiquid()) cover(player, toBarrier.getLocation());

            Block from = event.getFrom().getBlock().getRelative(BlockFace.UP);
            player.sendBlockChange(from.getLocation(), from.getBlockData());
        }
    }
}
