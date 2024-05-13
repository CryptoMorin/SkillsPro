package org.skills.abilities.arbalist;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
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

public class ArbalistMinions extends ActiveAbility {
    private static final String MINION = "ARBALIST_MINION";
    private static final Map<UUID, Set<Entity>> MINIONS = new HashMap<>();

    public ArbalistMinions() {
        super("Arbalist", "minions");
    }

    private static void killMinion(LivingEntity livingMinion) {
        livingMinion.getWorld().spawnParticle(XParticle.DRAGON_BREATH.get(), livingMinion.getLocation(), 100, 0.5, 0.5, 0.5, 0.05);
        livingMinion.setHealth(0);
        removeEntity(livingMinion);
    }

    @EventHandler
    public void onTargetChange(EntityTargetEvent event) {
        if (event.getEntityType() != EntityType.SKELETON &&
                (XMaterial.supports(14) && event.getEntityType() != EntityType.PILLAGER)) return;
        Entity minion = event.getEntity();
        List<MetadataValue> metas = minion.getMetadata(MINION);
        if (metas.isEmpty()) return;
        LivingEntity target = (LivingEntity) metas.get(0).value();

        if (event.getTarget() == null || target.getEntityId() != event.getTarget().getEntityId())
            event.setTarget(target);
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
            event.getEntity().getWorld().spawnParticle(XParticle.DRAGON_BREATH.get(), event.getEntity().getLocation(), 100, 0.5, 0.5, 0.5, 0.05);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;

        Player player = (Player) arrow.getShooter();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        int lvl = info.getAbilityLevel(this);

        boolean shouldMax = XMaterial.supports(14) && lvl > 2;
        Location center = player.getLocation();
        EntityType type = shouldMax ? EntityType.PILLAGER : EntityType.SKELETON;
        ItemStack bow = shouldMax ? XMaterial.CROSSBOW.parseItem() : XMaterial.BOW.parseItem();
        XSound sound = shouldMax ? XSound.ENTITY_PILLAGER_AMBIENT : XSound.ENTITY_SKELETON_STEP;

        int dmg = (int) getScaling(info, "enchants.arrow-damage");
        if (dmg > 0) bow.addUnsafeEnchantment(XEnchantment.POWER.getEnchant(), dmg);
        int flame = (int) getScaling(info, "enchants.flame");
        if (flame > 0) bow.addUnsafeEnchantment(XEnchantment.FLAME.getEnchant(), flame);

        new BukkitRunnable() {
            int times = 5;

            @Override
            public void run() {
                sound.play(player);
                times--;
                if (times == 0) cancel();
            }
        }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 5L);

        Set<Entity> minions = new HashSet<>();
        for (int i = (int) getScaling(info, "amount"); i > 0; i--) {
            int x = MathUtils.randInt(1, 3);
            int z = MathUtils.randInt(1, 3);

            Location spawn = center.clone().add(x, 0, z);
            if (!spawn.getBlock().getType().name().endsWith("AIR")) spawn = player.getLocation();
            spawn.getWorld().spawnParticle(XParticle.PORTAL.get(), spawn.clone().add(0, 1, 0), 500, 0, 0, 0, 1);

            Location finalSpawn = spawn;
            AtomicBoolean isDead = new AtomicBoolean();
            Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
                if (!entity.isValid()) {
                    isDead.set(true);
                    return;
                }
                Location facing = finalSpawn.setDirection(entity.getLocation().toVector().subtract(finalSpawn.toVector()));
                Monster minion = (Monster) finalSpawn.getWorld().spawnEntity(facing, type);

                minion.setMetadata(MINION, new FixedMetadataValue(SkillsPro.get(), entity));
                minion.setTarget((LivingEntity) entity);
                minion.setRemoveWhenFarAway(true);
                minion.setCustomName(MessageHandler.colorize("&c" + player.getName() + " Minion"));
                minion.setCustomNameVisible(true);

                EntityEquipment equips = minion.getEquipment();
                equips.setItemInMainHand(bow);

                applyEffects(info, minion);
                applyEffects(info, player);
                minions.add(minion);
                addEntity(minion);
            }, 40L);
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
