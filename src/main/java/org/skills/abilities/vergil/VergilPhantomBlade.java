package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.utils.DisplayEntityUtil;
import org.skills.utils.LocationUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VergilPhantomBlade extends InstantActiveAbility {
    private static final Cache<UUID, Boolean> LAST_SHOULDER = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS).build();
    private static final Cache<UUID, Integer> COOLDOWN = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.SECONDS).build();

    /**
     * It seems like the right-click event fire rate is different for clicking on blocks vs air.
     * 100: Partially works when right-clicking on blocks, doesn't work at all for air.
     * 200: Works for blocks, but only partially for air.
     * 300: Works for both blocks and air.
     */
    private static final Cache<UUID, Integer> HOLD = CacheBuilder.newBuilder()
            .expireAfterWrite(300, TimeUnit.MILLISECONDS).build();

    private static final Map<Integer, ArmorStand> SWORDS = new ConcurrentHashMap<>();
    private static final ParticleDisplay SWORD_PARTICLE = ParticleDisplay.of(Particle.DRAGON_BREATH).withCount(5);//.offset(0.2);

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SkillsPro.get(), () -> {
            Iterator<ArmorStand> iter = SWORDS.values().iterator();
            while (iter.hasNext()) {
                ArmorStand sword = iter.next();
                if (!sword.isValid() || sword.isDead()) {
                    iter.remove();
                } else {
                    SWORD_PARTICLE.spawn(LocationUtils.getArmorStandHandLocation(sword));
                }
            }
        }, 100L, 5L);
    }

    public VergilPhantomBlade() {
        super("Vergil", "phantom_blade");
        setPvPBased(true);
    }

    @EventHandler
    public void onHoldActivation(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        if (XMaterial.matchXMaterial(player.getInventory().getItemInMainHand()) != XMaterial.STICK) return;

        Integer times = HOLD.getIfPresent(player.getUniqueId());

        if (times == null) {
            HOLD.put(player.getUniqueId(), 1);
            return;
        }

        SLogger.info("Called interact " + times);
        if (times >= 10) {
            guardingPhantomBlades(player);
            HOLD.invalidate(player.getUniqueId());
        } else {
            HOLD.put(player.getUniqueId(), times + 1);
        }
    }

    public static void guardingPhantomBlades(Player player) {

    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        Location leftShoulder = player.getEyeLocation().add(3, 0, 0);
        Location rightShoulder = player.getEyeLocation().add(-3, 0, 0);
        Location currentShoulder;

        Boolean lastShoulder = LAST_SHOULDER.getIfPresent(player.getUniqueId());
        if (lastShoulder == null) lastShoulder = true;
        if (lastShoulder) {
            currentShoulder = rightShoulder;
        } else {
            currentShoulder = leftShoulder;
        }
        LAST_SHOULDER.put(player.getUniqueId(), !lastShoulder);

        RayTraceResult trace = player.getWorld().rayTrace(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                50, FluidCollisionMode.ALWAYS, true, 0.5, e -> e != player);

        Location hit;
        if (trace != null) {
            if (trace.getHitEntity() != null) {
                hit = trace.getHitEntity().getLocation();
            } else if (trace.getHitBlock() != null) {
                hit = trace.getHitBlock().getLocation();
            } else {
                hit = trace.getHitPosition().toLocation(player.getWorld());
            }
        } else {
            return;
        }

        Location spawnLoc = player.getEyeLocation();

        COOLDOWN.put(player.getUniqueId(), 0);
        DisplayEntityUtil.spawnDisplay(player, spawnLoc);
    }

    // Armorstands would be honestly super buggy for this...
//    private static void AS() {
//        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
//        Location location = player.getEyeLocation();
////        armorStand.setInvisible(true);
//        //armorStand.setGravity(false);
//        armorStand.setArms(true);
//        armorStand.setCollidable(false);
////        armorStand.setMarker(true);
//        armorStand.getEquipment().setItem(EquipmentSlot.HAND, XMaterial.DIAMOND_SWORD.parseItem());
//
//        // float speed, spread
////        Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), player.getEyeLocation().getDirection(), 10, 0);
////        arrow.addPassenger(armorStand);
//
//        // Euler Angle class uses radians.
//        armorStand.setRightArmPose(new EulerAngle(Math.toRadians(location.getPitch() - 10), 0, 0));
//        //armorStand.setVelocity(player.getEyeLocation().getDirection().multiply(1));
//        SWORDS.put(armorStand.getEntityId(), armorStand);
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                double dist = LocationUtils.distanceSquared(armorStand.getLocation(), hit);
//                if (!armorStand.isValid() || armorStand.isDead() || dist <= 1) {
//                    armorStand.setMarker(true);
//                    cancel();
//                    return;
//                }
//
//                armorStand.setVelocity(hit.toVector().subtract(armorStand.getLocation().toVector()).normalize().multiply(1));
//            }
//        }.runTaskTimer(SkillsPro.get(), 0L, 1L);
//    }
}
