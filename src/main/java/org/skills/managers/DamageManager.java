package org.skills.managers;

import com.github.benmanes.caffeine.cache.Cache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.skills.abilities.swordsman.SwordsmanPassive;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.CacheHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class DamageManager implements Listener {
    public static final String LAST_DAMAGE = "SKILLS_ATTACK";
    private static final Cache<Integer, Map<UUID, Double>> DAMAGES = CacheHandler.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final Cache<UUID, EntityDamageByEntityEvent> LAST_PLAYER_DAMAGES = CacheHandler.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final Cache<UUID, EntityDamageByEntityEvent> LAST_ENTITY_DAMAGES = CacheHandler.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final Cache<UUID, EntityDamageByEntityEvent> LAST_HIT_ENTITY = CacheHandler.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final Map<UUID, Integer> DAMAGE_TICKS = new HashMap<>();

    public static void storeDamageTicks(LivingEntity entity) {
        DAMAGE_TICKS.put(entity.getUniqueId(), entity.getNoDamageTicks());
    }

    public static void restoreDamageTicks(LivingEntity entity) {
        Integer ticks = DAMAGE_TICKS.remove(entity.getUniqueId());
        if (ticks != null) entity.setNoDamageTicks(ticks);
    }

    public static void damage(LivingEntity target, Player damager, double damage) {
        if (damage > 0) {
            target.damage(damage, damager);
        }
    }

    public static EntityDamageByEntityEvent getLastHitEntity(Player damager) {
        return LAST_HIT_ENTITY.getIfPresent(damager.getUniqueId());
    }

    public static EntityDamageByEntityEvent getLastDamager(Player victim, boolean includingMobs) {
        return (includingMobs ? LAST_ENTITY_DAMAGES : LAST_PLAYER_DAMAGES).getIfPresent(victim.getUniqueId());
    }

    public static LivingEntity getLastSourceDamager(Player victim, boolean includingMobs) {
        EntityDamageByEntityEvent event = getLastDamager(victim, includingMobs);
        return DamageManager.getDamager(event);
    }

    protected static Player getFinalHitMob(LivingEntity entity) {
        return entity.getKiller();
    }

    public static Player getMostDamaged(Entity entity) {
        Map<UUID, Double> damagers = DAMAGES.getIfPresent(entity.getEntityId());
        if (damagers == null) return null;
        double most = 0;
        Player mostPlayer = null;

        for (Map.Entry<UUID, Double> damager : damagers.entrySet()) {
            if (damager.getValue() > most) {
                Player player = Bukkit.getPlayer(damager.getKey());
                if (player != null) {
                    most = damager.getValue();
                    mostPlayer = player;
                }
            }
        }

        return mostPlayer;
    }

    public static Player getOwningPlayer(Entity entity) {
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Player) {
                Player entityShooter = (Player) shooter;
                if (ServiceHandler.isMyPet(entityShooter)) return ServiceHandler.getPetOwner(entityShooter);
                return entityShooter;
            }
        }
//        } else if (entity instanceof LivingEntity) {
//            if (ServiceHandler.isMyPet(entity)) return ServiceHandler.getPetOwner(entity);
//        }

        if (entity instanceof Player) return (Player) entity;
        return null;
    }

    public static LivingEntity getDamager(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof LivingEntity) {
                LivingEntity entityShooter = (LivingEntity) shooter;
                if (ServiceHandler.isMyPet(entityShooter)) return ServiceHandler.getPetOwner(entityShooter);
                return entityShooter;
            }
        }
        if (!(damager instanceof LivingEntity)) return null;
        return (LivingEntity) damager;
    }

    public static LivingEntity getKiller(EntityDeathEvent event) {
        Player mostDamaged = getMostDamaged(event.getEntity());
        if (mostDamaged != null) return mostDamaged;

        EntityDamageByEntityEvent killer = SwordsmanPassive.OFFHAND.remove(event.getEntity().getEntityId());
        if (killer != null) return (LivingEntity) killer.getDamager();

        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();

            if (damager instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof LivingEntity) {
                    LivingEntity entityShooter = (LivingEntity) shooter;
                    if (ServiceHandler.isMyPet(entityShooter)) return ServiceHandler.getPetOwner(entityShooter);
                    return entityShooter;
                }
            }

            if (damager instanceof LivingEntity) {
                if (ServiceHandler.isMyPet(damager)) return ServiceHandler.getPetOwner(damager);
                return (LivingEntity) damager;
            }
        }

        return event.getEntity().getKiller();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMostDamageHandle(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            LAST_PLAYER_DAMAGES.put(event.getEntity().getUniqueId(), event);
        }
        LAST_ENTITY_DAMAGES.put(event.getEntity().getUniqueId(), event);
        LAST_HIT_ENTITY.put(event.getDamager().getUniqueId(), event);

        DAMAGES.asMap().compute(event.getEntity().getEntityId(), (k, v) -> {
            if (v == null) v = new HashMap<>();
            v.compute(damager.getUniqueId(), (k2, v2) -> v2 == null ? event.getFinalDamage() : v2 + event.getFinalDamage());
            return v;
        });
    }
}
