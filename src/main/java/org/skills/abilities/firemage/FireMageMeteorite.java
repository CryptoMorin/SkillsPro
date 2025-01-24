package org.skills.abilities.firemage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.EntityUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class FireMageMeteorite extends InstantActiveAbility {
    private static final String METEORITE = "METEORITE";

    public FireMageMeteorite() {
        super("FireMage", "meteorite");
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Block center;
        if (XReflection.supports(13)) center = player.getTargetBlockExact(20);
        else {
            Set<Material> blacklist = EnumSet.of(Material.AIR, XMaterial.TALL_GRASS.parseMaterial());
            List<Block> sight = player.getLineOfSight(blacklist, 20);
            center = sight.get(sight.size() - 1);
        }

        Location loc;
        if (center == null) loc = player.getLocation().clone().add(player.getLocation().getDirection().multiply(20));
        else loc = center.getLocation();

        Location source = loc.clone().add(0, 5, 0);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ParticleDisplay display = ParticleDisplay.of(XParticle.FLAME).withLocation(null).withCount(10);
        ParticleDisplay ex = ParticleDisplay.of(XParticle.EXPLOSION).withLocation(null).withCount(10).offset(1, 1, 1);

        new BukkitRunnable() {
            final float yield = (float) getScaling(info, "yield");
            int balls = (int) getScaling(info, "fireballs");

            @Override
            public void run() {
                Location src = source.clone().add(random.nextDouble(-3, 3), random.nextDouble(1, 15), random.nextDouble(-3, 3));
                Location to = loc.clone().add(random.nextDouble(-1, 1), 0, random.nextDouble(-1, 1));
                Vector direction = to.toVector().subtract(src.toVector());

                Fireball fireball = (Fireball) src.getWorld().spawnEntity(src, EntityType.FIREBALL);
                fireball.setDirection(direction.multiply(0.01));
                fireball.setYield(yield);
                fireball.setMetadata(METEORITE, new FixedMetadataValue(SkillsPro.get(), player));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!fireball.isValid()) {
                            cancel();
                            return;
                        }
                        display.spawn(fireball.getLocation());
                        ex.spawn(fireball.getLocation());
                    }
                }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
                if (balls-- == 0) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0L, 5L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireBall(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Fireball)) return;
        Fireball fireBall = (Fireball) event.getDamager();
        List<MetadataValue> meta = fireBall.getMetadata(METEORITE);
        if (meta.isEmpty()) return;

        Player caster = (Player) meta.get(0).value();
        if (caster == event.getEntity()) event.setCancelled(true);
        else {
            if (caster == null || !ServiceHandler.canFight(event.getEntity(), caster)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireballLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) return;
        Fireball fireBall = (Fireball) event.getEntity();
        if (!fireBall.hasMetadata(METEORITE)) return;

        ParticleDisplay display = ParticleDisplay.of(XParticle.LAVA).withLocation(fireBall.getLocation()).withCount(50).offset(1, 1, 1);
        display.spawn();

        Player player = (Player) fireBall.getMetadata(METEORITE).get(0).value();
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        double damage = getScaling(info, "damage");
        double range = getScaling(info, "range");

        for (Entity entity : fireBall.getNearbyEntities(range, range, range)) {
            if (EntityUtil.filterEntity(player, entity)) continue;
            DamageManager.damage((LivingEntity) entity, player, damage);
        }
    }
}
