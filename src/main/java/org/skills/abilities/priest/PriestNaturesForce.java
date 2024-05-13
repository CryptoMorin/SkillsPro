package org.skills.abilities.priest;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.BeeUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PriestNaturesForce extends ActiveAbility {
    private static final String NATURES_FORCE = "NATURES_FORCE", BEEHIVE = "BEEHIVE", NATURES_FORCE_TARGET = "NATURES_FORCE_TARGET";
    private static final ParticleDisplay DEATH = ParticleDisplay.of(XParticle.FLAME).withLocation(null).withCount(100).offset(0.5, 0.5, 0.5);

    public PriestNaturesForce() {
        super("Priest", "natures_force");
    }

    @Override
    public boolean isSupported() {
        try {
            if (BeeUtils.SUPPORTS_BEES) return true;
        } catch (Throwable ignored) {
        }

        MessageHandler.sendConsolePluginMessage("&eServer doesn't support " + getClass().getSimpleName() + " ability.");
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHit(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = checkup(player);
        if (info == null) return;

        Location loc = player.getEyeLocation();
        LivingEntity target = (LivingEntity) event.getEntity();

        Block beehive = target.getLocation().add(0, 2, 0).getBlock();
        Material material = beehive.getType();
        beehive.setType(Material.BEEHIVE);
        Particles.spikeSphere(1, 20, 5, 1, 2, ParticleDisplay.of(XParticle.WITCH).withLocation(beehive.getLocation()));

        ParticleDisplay particle = ParticleDisplay.of(XParticle.LARGE_SMOKE).withLocation(beehive.getLocation());
        particle.count = 100;
        particle.offset(0.5, 0.5, 0.5);

        XSound.ENTITY_BEE_LOOP.play(loc, 3.0f, 0);
        long interval = (long) getScaling(info, "interval");
        int id = new BukkitRunnable() {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final double damage = getScaling(info, "damage");
            final Location hiveLoc = beehive.getLocation();
            long duration = (long) getScaling(info, "duration");
            int repeat = 0;
            double rotation = 0;

            @Override
            public void run() {
                Location beeLoc = hiveLoc.clone().add(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1));
                LivingEntity bee = BeeUtils.spawn(beeLoc, target);
                bee.setMetadata(NATURES_FORCE, new FixedMetadataValue(SkillsPro.get(), damage));
                bee.setMetadata(NATURES_FORCE_TARGET, new FixedMetadataValue(SkillsPro.get(), target));
                XSound.ENTITY_BEE_LOOP_AGGRESSIVE.play(loc, 3.0f, 0);
                particle.spawn(beeLoc);

                Bukkit.getScheduler().scheduleSyncDelayedTask(SkillsPro.get(), () -> {
                    if (bee.isValid()) {
                        DEATH.spawn(bee.getLocation());
                        bee.setHealth(0);
                    }
                }, duration * 20L);

                rotation += 0.1;
                if (rotation > 360) rotation = 0;

                repeat += interval;
                if (repeat >= 20) {
                    repeat = 0;
                    XSound.BLOCK_BEEHIVE_WORK.play(loc, 0.1f, 2f);
                }
                duration -= interval;
                if (duration <= 0) {
                    cancel();
                    beehive.setType(material);
                    beehive.removeMetadata(BEEHIVE, SkillsPro.get());
                    particle.spawn();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 0L, interval * 20L).getTaskId();
        beehive.setMetadata(BEEHIVE, new FixedMetadataValue(SkillsPro.get(), id));
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.hasMetadata(NATURES_FORCE)) DEATH.spawn(entity.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBeehiveBreak(BlockBreakEvent event) {
        Block beehive = event.getBlock();
        List<MetadataValue> metadata = beehive.getMetadata(BEEHIVE);
        if (metadata.isEmpty()) return;

        int task = metadata.get(0).asInt();
        Bukkit.getScheduler().cancelTask(task);
        event.setDropItems(false);

        ParticleDisplay particle = ParticleDisplay.of(XParticle.LARGE_SMOKE).withLocation(beehive.getLocation());
        particle.count = 100;
        particle.offset(0.5, 0.5, 0.5);
        particle.spawn();
    }

    @EventHandler
    public void onTargetChange(EntityTargetEvent event) {
        if (!BeeUtils.isBee(event.getEntity())) return;
        Entity bee = event.getEntity();

        List<MetadataValue> metadata = bee.getMetadata(NATURES_FORCE_TARGET);
        if (metadata.isEmpty()) return;
        LivingEntity target = (LivingEntity) metadata.get(0).value();
        event.setTarget(target);
    }

    @EventHandler(ignoreCancelled = true)
    public void tntDmaage(EntityDamageByEntityEvent event) {
        if (!BeeUtils.isBee(event.getDamager())) return;
        Entity bee = event.getDamager();

        List<MetadataValue> metadata = bee.getMetadata(NATURES_FORCE);
        if (metadata.isEmpty()) return;

        double damage = metadata.get(0).asDouble();
        event.setDamage(damage);
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> BeeUtils.setHasStung(bee, false), 1L);
    }
}
