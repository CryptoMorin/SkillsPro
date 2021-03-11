package org.skills.abilities.vampire;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.EntityEffect;
import org.bukkit.Particle;
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
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.managers.LastHitManager;
import org.skills.utils.FastUUID;
import org.skills.utils.Laser;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VampireBloodCircuit extends ActiveAbility {
    private static final String MINION = "VAMPIRE_VEX";

    public VampireBloodCircuit() {
        super("vampire", "blood_circuit", false);
    }

    private static void killMinion(LivingEntity livingMinion) {
        livingMinion.getLocation().getWorld().spawnParticle(Particle.CLOUD, livingMinion.getLocation(), 100, 0.5, 0.5, 0.5, 0.05);
        livingMinion.setHealth(0);
    }

    private static Vex spawnMinion(Player player, LivingEntity entity) {
        Vex vex = (Vex) player.getWorld().spawnEntity(player.getLocation(), EntityType.VEX);
        vex.setTarget(entity);
        if (XMaterial.isNewVersion()) vex.setCharging(true);
        vex.setMetadata(MINION, new FixedMetadataValue(SkillsPro.get(), entity.getUniqueId()));
        vex.setRemoveWhenFarAway(true);
        vex.setCustomName(MessageHandler.colorize("&c" + player.getName() + " Minion"));
        vex.setCustomNameVisible(true);
        return vex;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onVampireCircuit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() < 1) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;

        LivingEntity entity = (LivingEntity) event.getEntity();
        if (entity instanceof Player) {
            Player victim = (Player) entity;
            victim.playEffect(EntityEffect.GUARDIAN_TARGET);
        }

        EnderCrystal crystal = (EnderCrystal) player.getWorld().spawnEntity(player.getLocation().add(0, 2, 0), EntityType.ENDER_CRYSTAL);
        crystal.setShowingBottom(false);

        XSound.ENTITY_ELDER_GUARDIAN_CURSE.play(crystal.getLocation());
        boolean useLaser = getExtra(info, "use-laser").getBoolean();
        Laser laser = null;
        if (useLaser) {
            try {
                laser = new Laser(crystal.getLocation().add(0, 0.5, 0), entity.getLocation(), -1, 16);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            laser.start(SkillsPro.get());
        }
        Laser finalLaser = laser;

        int lvl = info.getImprovementLevel(this);
        new BukkitRunnable() {
            final double damageMod = getExtraScaling(info, "damage-modifier", event);
            final int inferno = (int) getExtraScaling(info, "inferno", event);
            final double distance = getExtraScaling(info, "distance", event);
            final Set<LivingEntity> minions = new HashSet<>();
            final ParticleDisplay particle = ParticleDisplay.simple(null, Particle.DRAGON_BREATH).withCount(10).offset(0.1, 0.1, 0.1);
            double damage = getScaling(info, event);
            int duration = (int) (getExtraScaling(info, "duration", event) * 20);
            int repeat = 0;
            int particleTimer = 0;

            @Override
            public void run() {
                if (!entity.isValid() || !crystal.isValid() || duration-- <= 0) {
                    cancel();
                    if (useLaser) finalLaser.stop();
                    crystal.getLocation().getWorld().spawnParticle(Particle.SPELL_WITCH, crystal.getLocation(), 200, 0.5, 0.5, 0.5, 0.05);
                    crystal.remove();
                    for (LivingEntity minion : minions) killMinion(minion);
                    return;
                }
                boolean farAway = entity.getLocation().distance(crystal.getLocation()) > distance;

                if (useLaser) {
                    try {
                        if (farAway) finalLaser.moveEnd(finalLaser.getStart());
                        else finalLaser.moveEnd(entity.getEyeLocation().add(0, -0.75, 0));
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }
                if (farAway) return;

                if (particleTimer++ == 10) {
                    particleTimer = 0;
                    if (!useLaser) XParticle.line(crystal.getLocation().add(0, 0.5, 0), entity.getEyeLocation(), 0.2, particle);
                }
                if (repeat++ == inferno) {
                    repeat = 0;
                    LastHitManager.damage(entity, player, damage);
                    if (lvl > 1) VersionSupport.heal(player, damage);

                    damage += damageMod;
                    if (lvl > 2) minions.add(spawnMinion(player, entity));
                    if (useLaser) finalLaser.callColorChange();
                }
            }
        }.runTaskTimer(SkillsPro.get(), 1, 1);
    }

    @EventHandler
    public void onTargetChange(EntityTargetEvent event) {
        if (!XMaterial.supports(11) || event.getEntity().getType() != EntityType.VEX) return;
        Entity minion = event.getEntity();
        List<MetadataValue> metas = minion.getMetadata(MINION);
        if (metas.isEmpty()) return;
        UUID target = FastUUID.fromString(metas.get(0).asString());

        if (event.getTarget() == null || !target.equals(event.getTarget().getUniqueId())) {
            for (LivingEntity entity : minion.getLocation().getWorld().getLivingEntities()) {
                if (target.equals(entity.getUniqueId())) {
                    event.setTarget(entity);
                    break;
                }
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%damage-modifier%", translate(info, "damage-modifier"),
                "%duration%", translate(info, "duration"), "%inferno%", translate(info, "inferno"),
                "%distance%", translate(info, "distance")};
    }
}
