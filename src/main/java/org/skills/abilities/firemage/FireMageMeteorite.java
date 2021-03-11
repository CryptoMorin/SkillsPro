package org.skills.abilities.firemage;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.managers.LastHitManager;
import org.skills.services.manager.ServiceHandler;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FireMageMeteorite extends ActiveAbility {
    private static final String METEORITE = "METEORITE";

    public FireMageMeteorite() {
        super("FireMage", "meteorite", true);
    }

    @Override
    protected void useSkill(Player player) {
        SkilledPlayer info = activeCheckup(player);
        if (info == null) return;

        Block center;
        if (XMaterial.supports(13)) center = player.getTargetBlockExact(20);
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
        ParticleDisplay display = ParticleDisplay.simple(null, Particle.FLAME).withCount(10);
        ParticleDisplay ex = ParticleDisplay.simple(null, Particle.EXPLOSION_NORMAL).withCount(10).offset(1, 1, 1);

        new BukkitRunnable() {
            final float yield = (float) getExtraScaling(info, "yield");
            int balls = (int) getExtraScaling(info, "fireballs");

            @Override
            public void run() {
                Location src = source.clone().add(random.nextDouble(-3, 3), random.nextDouble(1, 15), random.nextDouble(-3, 3));
                Location to = loc.clone().add(random.nextDouble(-1, 1), 0, random.nextDouble(-1, 1));
                Vector direction = to.toVector().subtract(src.toVector());

                Fireball fireball = (Fireball) src.getWorld().spawnEntity(src, EntityType.FIREBALL);
                fireball.setDirection(direction.multiply(0.01));
                fireball.setYield(yield);
                fireball.setMetadata(METEORITE, new FixedMetadataValue(SkillsPro.get(), player.getUniqueId()));
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

        UUID id = (UUID) meta.get(0).value();
        if (event.getEntity().getUniqueId().equals(id)) event.setCancelled(true);
        else {
            Player caster = Bukkit.getPlayer(id);
            if (caster == null || !ServiceHandler.canFight(event.getEntity(), caster)) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireballLand(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball)) return;
        Fireball fireBall = (Fireball) event.getEntity();
        if (!fireBall.hasMetadata(METEORITE)) return;

        ParticleDisplay display = ParticleDisplay.simple(fireBall.getLocation(), Particle.LAVA).withCount(50).offset(1, 1, 1);
        display.spawn();

        UUID id = (UUID) fireBall.getMetadata(METEORITE).get(0).value();
        OfflinePlayer player = Bukkit.getOfflinePlayer(id);
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        double scaling = getScaling(info);
        double range = getExtraScaling(info, "range");

        for (Entity entity : fireBall.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity.getType() == EntityType.ARMOR_STAND) continue;
            if (entity.getUniqueId().equals(player.getUniqueId())) continue;
            if (entity instanceof Player) {
                Player player1 = (Player) entity;
                if (player1.getGameMode() == GameMode.CREATIVE || player1.getGameMode() == GameMode.SPECTATOR) continue;
            }

            LastHitManager.damage((LivingEntity) entity, (Player) player, scaling);
        }
    }
}
