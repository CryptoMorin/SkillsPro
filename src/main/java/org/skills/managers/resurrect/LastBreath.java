package org.skills.managers.resurrect;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.ReflectiveNamespace;
import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftConnection;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftMapping;
import com.cryptomorin.xseries.reflection.minecraft.MinecraftPackage;
import com.cryptomorin.xseries.reflection.minecraft.capabilities.MinecraftCapabilities;
import com.cryptomorin.xseries.reflection.minecraft.capabilities.MinecraftEntityCapability;
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
import java.util.HashMap;
import java.util.Map;

import static com.cryptomorin.xseries.reflection.XReflection.*;

public final class LastBreath implements Listener {
    static final int VIEW_DISTANCE = 100, ENTITY_POSE_REGISTRY = 6;
    static final Map<Integer, LastManStanding> LAST_MEN_STANDING = new HashMap<>(), REVIVERS = new HashMap<>();
    private static final Object EntityDataSerializers_POSE;
    private static final MethodHandle ClientboundSetEntityDataPacket_ctor, EntityDataAccessor_ctor, SynchedEntityData_get;
    private static final MinecraftEntityCapability ENTITY_CAPABILITY =
            MinecraftCapabilities.getCapability(MinecraftEntityCapability.class);

    static {
        ReflectiveNamespace ns = namespaced();

        Class<?> SynchedEntityData = ns.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.syncher")
                .map(MinecraftMapping.MOJANG, "SynchedEntityData")
                .map(MinecraftMapping.SPIGOT, "DataWatcher").unreflect();

        MinecraftClassHandle EntityDataAccessor = ns.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.syncher")
                .map(MinecraftMapping.MOJANG, "EntityDataAccessor")
                .map(MinecraftMapping.SPIGOT, "DataWatcherObject");

        Class<?> EntityDataSerializers = ns.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.syncher")
                .map(MinecraftMapping.MOJANG, "EntityDataSerializers")
                .map(MinecraftMapping.SPIGOT, "DataWatcherRegistry")
                .unreflect();

        Class<?> EntityDataSerializer = ns.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.syncher")
                .map(MinecraftMapping.MOJANG, "EntityDataSerializer")
                .map(MinecraftMapping.SPIGOT, "DataWatcherSerializer")
                .unreflect();

        MinecraftClassHandle ClientboundSetEntityDataPacket = ns.ofMinecraft()
                .inPackage(MinecraftPackage.NMS, "network.protocol.game")
                .map(MinecraftMapping.MOJANG, "ClientboundSetEntityDataPacket")
                .map(MinecraftMapping.SPIGOT, "PacketPlayOutEntityMetadata");

        EntityDataSerializers_POSE = ns.of(EntityDataSerializers)
                .field("public static final EntityDataSerializer<Pose> POSE")
                .map(MinecraftMapping.SPIGOT, XReflection.v(1, 19, "s").v(1, 13, "p").orElse("n"))
                .get(null);

        EntityDataAccessor_ctor = EntityDataAccessor.constructor("public EntityDataAccessor(int id, EntityDataSerializer<T> dataType)")
                .unreflect();

        ClientboundSetEntityDataPacket_ctor =
                v(1, 19, 3, ClientboundSetEntityDataPacket.constructor("public ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems)"))
                        .orElse(ClientboundSetEntityDataPacket.constructor("public ClientboundSetEntityDataPacket(int id, SynchedEntityData tracker, boolean forceUpdateAll)"))
                        .unreflect();


        if (XReflection.supports(1, 19)) {
            SynchedEntityData_get = ns.of(SynchedEntityData)
                    .method("public List<SynchedEntityData.DataValue> packDirty()")
                    .map(MinecraftMapping.SPIGOT, "b")
                    .unreflect();
        } else {
            SynchedEntityData_get = null;
        }
    }

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SkillsPro.get(), () -> {
            for (LastManStanding lastStanding : LAST_MEN_STANDING.values()) {
                Location location = lastStanding.player.getLocation();
                for (Player player : lastStanding.player.getWorld().getPlayers()) {
                    if (player != lastStanding.player && LocationUtils.distanceSquared(location, player.getLocation()) < VIEW_DISTANCE) {
                        MinecraftConnection.sendPacket(player, lastStanding.dataWatcher);
                    }
                }
            }
        }, 100L, 1L);
    }

    static Object registerDataWatcher(Player player, boolean swimming) {
        try {
            Object handle = ENTITY_CAPABILITY.getEntityHandle(player);
            Object watcher = ENTITY_CAPABILITY.getDataWatcher(handle);
            Object registry = EntityDataAccessor_ctor.invoke(ENTITY_POSE_REGISTRY, EntityDataSerializers_POSE);

            Object pos = (swimming ? MinecraftEntityCapability.EntityPose.SWIMMING : MinecraftEntityCapability.EntityPose.STANDING).getEnumValue();
            ENTITY_CAPABILITY.setData(watcher, registry, pos);

            if (XReflection.supports(1, 19)) {
                return ClientboundSetEntityDataPacket_ctor.invoke(player.getEntityId(), SynchedEntityData_get.invoke(watcher));
            } else {
                return ClientboundSetEntityDataPacket_ctor.invoke(player.getEntityId(), watcher, true);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static void cover(Player player, Location location) {
        if (supports(1, 13)) player.sendBlockChange(location, Material.BARRIER.createBlockData());
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

        if (XReflection.supports(1, 11)) {
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




