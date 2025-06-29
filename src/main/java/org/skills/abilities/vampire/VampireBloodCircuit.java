package org.skills.abilities.vampire;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.EntityEffect;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.managers.DamageManager;
import org.skills.utils.Laser;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VampireBloodCircuit extends ActiveAbility {
    private static final String MINION = "VAMPIRE_VEX";
    private static boolean USE_LASER = true;

    public VampireBloodCircuit() {
        super("vampire", "blood_circuit");
    }

    private static void killMinion(LivingEntity livingMinion) {
        ParticleDisplay.of(XParticle.CLOUD).withLocation(livingMinion.getLocation()).withCount(100).offset(0.5).withExtra(0.05).spawn();
        livingMinion.setHealth(0);
    }

    private static Vex spawnMinion(Player player, LivingEntity entity) {
        Vex vex = (Vex) player.getWorld().spawnEntity(player.getLocation(), EntityType.VEX);
        vex.setTarget(entity);
        if (XReflection.supports(13)) vex.setCharging(true);
        vex.setMetadata(MINION, new FixedMetadataValue(SkillsPro.get(), entity));
        vex.setRemoveWhenFarAway(true);
        vex.setCustomName(MessageHandler.colorize("&c" + player.getName() + " Minion"));
        vex.setCustomNameVisible(true);
        return vex;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVampireCircuit(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (entity.getHealth() - event.getFinalDamage() < 1) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (entity instanceof Player) {
            Player victim = (Player) entity;
            victim.playEffect(EntityEffect.GUARDIAN_TARGET);
        }

        EnderCrystal crystal = (EnderCrystal) player.getWorld().spawnEntity(player.getLocation().add(0, 2, 0), XEntityType.END_CRYSTAL.get());
        crystal.setShowingBottom(false);

        XSound.ENTITY_ELDER_GUARDIAN_CURSE.play(crystal.getLocation());
        Laser laser = null;
        if (USE_LASER) {
            try {
                laser = new Laser(crystal.getLocation().add(0, 0.5, 0), () -> entity.getEyeLocation().add(0, -0.75, 0), -1, 16);
            } catch (Throwable e) {
                e.printStackTrace();
                USE_LASER = false;
            }
            laser.start(SkillsPro.get());
        }
        Laser finalLaser = laser;

        int lvl = info.getAbilityLevel(this);
        new BukkitRunnable() {
            final double damageMod = getScaling(info, "damage-modifier", event);
            final int inferno = (int) getScaling(info, "inferno", event);
            final double distance = getScaling(info, "distance", event);
            final Set<LivingEntity> minions = new HashSet<>();
            final ParticleDisplay particle = ParticleDisplay.of(XParticle.DRAGON_BREATH).withLocation(null).withCount(10).offset(0.1);
            double damage = getScaling(info, "damage", event);
            int duration = (int) (getScaling(info, "duration", event) * 20);
            int repeat = 0;
            int particleTimer = 0;

            @Override
            public void run() {
                if (!entity.isValid() || !crystal.isValid() || duration-- <= 0) {
                    cancel();
                    if (USE_LASER) finalLaser.stop();
                    ParticleDisplay.of(XParticle.WITCH).withLocation(crystal.getLocation()).withCount(200).offset(0.5).withExtra(0.5).spawn();
                    crystal.remove();
                    for (LivingEntity minion : minions) killMinion(minion);
                    return;
                }
                boolean farAway = entity.getLocation().distance(crystal.getLocation()) > distance;
                if (farAway) return;

                if (particleTimer++ == 10) {
                    particleTimer = 0;
                    if (!USE_LASER)
                        Particles.line(crystal.getLocation().add(0, 0.5, 0), entity.getEyeLocation(), 0.2, particle);
                }
                if (repeat++ == inferno) {
                    repeat = 0;
                    DamageManager.damage(entity, null, damage);
                    if (lvl > 1) VersionSupport.heal(player, damage);

                    damage += damageMod;
                    if (lvl > 2) minions.add(spawnMinion(player, entity));
                    if (USE_LASER) finalLaser.changeColor();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 1, 1);
    }

    @EventHandler
    public void onTargetChange(EntityTargetEvent event) {
        if (!XReflection.supports(11)) return;
        if (event.getEntity().getType() != EntityType.VEX) return;

        Entity minion = event.getEntity();
        List<MetadataValue> metas = minion.getMetadata(MINION);
        if (metas.isEmpty()) return;
        Entity target = (Entity) metas.get(0).value();

        if (event.getTarget() != target) event.setTarget(target);
    }
}
