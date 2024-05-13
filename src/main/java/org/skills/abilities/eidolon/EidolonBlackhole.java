package org.skills.abilities.eidolon;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.EntityUtil;

import java.util.List;
import java.util.Set;

public class EidolonBlackhole extends InstantActiveAbility {
    private static final String BLACKHOLE = "BLACKHOLE";

    public EidolonBlackhole() {
        super("Eidolon", "blackhole");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Location loc = player.getEyeLocation();
        ParticleDisplay particle = ParticleDisplay.of(XParticle.PORTAL).withLocation(loc).withCount(100).withExtra(3);
        ArmorStand blackhole = (ArmorStand) player.getWorld().spawnEntity(loc.clone().add(0, -1.5, 0), EntityType.ARMOR_STAND);
        blackhole.setVisible(false);
        blackhole.setMarker(true);
        blackhole.getEquipment().setHelmet(XMaterial.DRAGON_EGG.parseItem());
        addEntity(blackhole);

        playSound(player, info, "start");
        long quality = (long) getScaling(info, "quality");

        new BukkitRunnable() {
            final double range = getScaling(info, "range");
            final double gravity = getScaling(info, "gravity");
            final double distance = getScaling(info, "distance");
            final double damage = getScaling(info, "damage");
            final World world = loc.getWorld();
            final Set<EntityType> blacklisted = getEntityList(info, "blacklisted");
            long duration = (long) getScaling(info, "duration");
            int repeat = 0;
            double rotation = 0;

            @Override
            public void run() {
                for (Entity entity : world.getNearbyEntities(loc, range, range, range)) {
                    if (player == entity) continue;
                    if (!blacklisted.contains(entity.getType())) {
                        Vector direction = loc.toVector().subtract(entity.getLocation().toVector()).normalize();
                        entity.setVelocity(direction.multiply(gravity));
                        continue;
                    }
                    if (EntityUtil.filterEntity(player, entity)) continue;

                    if (entity.getLocation().distance(loc) < distance) {
                        DamageManager.damage((LivingEntity) entity, player, damage);
                        if (entity instanceof Player) applyEffects(info, (LivingEntity) entity);
                    }
                }

                particle.spawn();
                blackhole.setHeadPose(new EulerAngle(rotation, rotation * -1, 0));
                rotation += 0.1;
                if (rotation > 360) rotation = 0;

                repeat += quality;
                if (repeat >= 20) {
                    repeat = 0;
                    playSound(player, info, "blackhole");
                }
                duration -= quality;
                if (duration <= 0) {
                    cancel();
                    blackhole.remove();
                    removeEntity(blackhole);

                    TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, XEntityType.TNT.get());
                    tnt.setMetadata(BLACKHOLE, new FixedMetadataValue(SkillsPro.get(), player));
                    tnt.setYield((float) getScaling(info, "yield"));
                    tnt.setFuseTicks(0);
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, quality);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tntDmaage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Entity tnt = event.getDamager();
        List<MetadataValue> metadata = tnt.getMetadata(BLACKHOLE);
        if (metadata.isEmpty()) return;

        Player player = (Player) tnt.getMetadata(BLACKHOLE).get(0).value();
        if (player != null && ServiceHandler.areFriendly(event.getEntity(), player)) event.setCancelled(true);
    }
}
