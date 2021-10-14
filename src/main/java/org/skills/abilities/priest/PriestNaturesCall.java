package org.skills.abilities.priest;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import org.skills.utils.MathUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PriestNaturesCall extends ActiveAbility {
    private static final String MINION = "PRIEST_NATURES_CALL";
    private static final String DAMAGE = "PRIEST_NATURES_DAMAGE";
    private static final Map<UUID, Set<Entity>> MINIONS = new HashMap<>();

    public PriestNaturesCall() {
        super("Arbalist", "natures_call");
    }

    private static void killMinion(LivingEntity livingMinion) {
        livingMinion.getLocation().getWorld().spawnParticle(Particle.DRAGON_BREATH, livingMinion.getLocation(), 100, 0.5, 0.5, 0.5, 0.05);
        livingMinion.setHealth(0);
    }

    @EventHandler
    public void onTargetChange(EntityTargetEvent event) {
        if (event.getEntityType() != EntityType.WOLF) return;
        Entity minion = event.getEntity();
        List<MetadataValue> metas = minion.getMetadata(MINION);
        if (metas.isEmpty()) return;
        LivingEntity target = (LivingEntity) metas.get(0).value();

        if (event.getTarget() == null || !target.getUniqueId().equals(event.getTarget().getUniqueId())) event.setTarget(target);
    }

    @EventHandler
    public void onMinionTargetDeath(EntityDeathEvent event) {
        Set<Entity> minions = MINIONS.get(event.getEntity().getUniqueId());
        if (minions != null) {
            for (Entity minion : minions) {
                if (minion.isValid() && minion instanceof LivingEntity) {
                    LivingEntity livingMinion = (LivingEntity) minion;
                    killMinion(livingMinion);
                }
            }
        }

        if (event.getEntity().hasMetadata(MINION)) {
            event.getEntity().getLocation().getWorld().spawnParticle(Particle.DRAGON_BREATH, event.getEntity().getLocation(), 100, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinionAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity)) return;
        LivingEntity minion = (LivingEntity) event.getDamager();
        List<MetadataValue> metas = minion.getMetadata(DAMAGE);
        if (metas.isEmpty()) return;
        double damage = metas.get(0).asDouble();
        event.setDamage(damage);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        Entity entity = event.getEntity();
        Location center = player.getLocation();

        new BukkitRunnable() {
            int times = 10;

            @Override
            public void run() {
                XSound.ENTITY_FOX_AGGRO.play(player);
                times--;
                if (times == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 5L);

        Set<Entity> minions = new HashSet<>();
        double damage = getScaling(info, "damage");
        for (int i = (int) getScaling(info, "minions"); i > 0; i--) {
            int x = MathUtils.randInt(1, 3);
            int z = MathUtils.randInt(1, 3);

            Location spawn = center.clone().add(x, 0, z);
            if (!spawn.getBlock().getType().name().endsWith("AIR")) spawn = player.getLocation();
            ParticleDisplay.simple(spawn, Particle.SMOKE_LARGE).withCount(100).offset(0.5, 0.5, 0.5).spawn();

            AtomicBoolean isDead = new AtomicBoolean();
            if (!entity.isValid()) {
                isDead.set(true);
                return;
            }
            Location facing = spawn.setDirection(entity.getLocation().toVector().subtract(spawn.toVector()));
            Wolf fox = (Wolf) spawn.getWorld().spawnEntity(facing, EntityType.WOLF);

            fox.setMetadata(MINION, new FixedMetadataValue(SkillsPro.get(), entity));
            fox.setMetadata(DAMAGE, new FixedMetadataValue(SkillsPro.get(), damage));
            fox.setTarget((LivingEntity) entity);
            fox.setRemoveWhenFarAway(true);
            fox.setCustomName(MessageHandler.colorize("&c" + player.getName() + " Wolf"));
            fox.setCustomNameVisible(true);

            applyEffects(info, fox);
            applyEffects(info, player);
            minions.add(fox);
            if (isDead.get()) break;
        }
        MINIONS.put(entity.getUniqueId(), minions);

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            for (Entity minion : minions) {
                if (minion.isValid()) {
                    LivingEntity livingMinion = (LivingEntity) minion;
                    killMinion(livingMinion);
                }
            }
        }, (long) (getScaling(info, "time") * 20L));
    }
}
