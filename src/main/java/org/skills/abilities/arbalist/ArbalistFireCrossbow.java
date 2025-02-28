package org.skills.abilities.arbalist;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.reflection.XReflection;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

public class ArbalistFireCrossbow extends InstantActiveAbility {
    public static final String ARBALIST_FIRECROSSBOW = "ARBALIST_CROSS";

    public ArbalistFireCrossbow() {
        super("Arbalist", "fire_crossbow");
        if (!XReflection.supports(14)) Bukkit.getPluginManager().registerEvents(new Old(), SkillsPro.get());
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        SkilledPlayer info = context.getInfo();

        Vector vector = player.getEyeLocation().getDirection();
        double extraScaling = getScaling(info, "range");
        int kb = (int) getScaling(info, "knockback");
        int fire = (int) getScaling(info, "fire");
        vector.multiply(extraScaling);

        Arrow arrow = player.launchProjectile(Arrow.class, vector);
        arrow.setInvulnerable(true);
        arrow.setBounce(false);
        arrow.setFireTicks(fire);
        arrow.setKnockbackStrength(kb);
        arrow.setMetadata(ARBALIST_FIRECROSSBOW, new FixedMetadataValue(SkillsPro.get(), null));
        if (XReflection.supports(14)) arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

        int shotgunChance = (int) getScaling(info, "shotgun.chance");
        if (MathUtils.hasChance(shotgunChance)) {
            int min = (int) getScaling(info, "shotgun.amount.min");
            int max = (int) getScaling(info, "shotgun.amount.max");
            double offset = (int) getScaling(info, "shotgun.offset");
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < random.nextInt(min, max); i++) {
                Arrow extra = player.getWorld().spawnArrow(player.getEyeLocation(), vector, (float) extraScaling, (float) offset);
                extra.setMetadata(ARBALIST_FIRECROSSBOW, new FixedMetadataValue(SkillsPro.get(), null));
                if (XReflection.supports(14)) extra.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
            }
        }

        arrow.setGlowing(true);
        if (MathUtils.hasChance((int) getScaling(info, "critical-chance"))) arrow.setCritical(true);
        XSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR.play(player);

        player.spawnParticle(XParticle.LAVA.get(), player.getLocation(), (int) (extraScaling * 2) + 10, 0.1, 0.1, 0.1, 1);
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                if (i > 4) cancel();
                if (arrow.isOnGround()) cancel();

                if (XReflection.supports(13))
                    player.playNote(arrow.getLocation(), Instrument.CHIME, Note.natural(1, Note.Tone.values()[i]));
                player.spawnParticle(XParticle.FLAME.get(), arrow.getLocation(), (int) (extraScaling * 2), 0.01, 0.01, 0.01, 0.1);
            }
        }.runTaskTimer(SkillsPro.get(), 5L, 5L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTargetHit(EntityDamageByEntityEvent event) {
        Entity arrow = event.getDamager();
        if (!(arrow instanceof Projectile)) return;
        if (!arrow.hasMetadata(ARBALIST_FIRECROSSBOW)) return;

        Player shooter = (Player) ((Projectile) arrow).getShooter();
        if (shooter == null) return;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(shooter);

        event.setDamage((int) this.getScaling(info, "damage"));
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(shooter, 2, 0);
        shooter.spawnParticle(XParticle.LAVA.get(), event.getEntity().getLocation(), 30, 0.1, 0.1, 0.1, 0.1);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        // Note: getHitBlock() is not supported in v1.9.4

        Projectile arrow = event.getEntity();
        if (!arrow.hasMetadata(ARBALIST_FIRECROSSBOW)) return;
        arrow.removeMetadata(ARBALIST_FIRECROSSBOW, SkillsPro.get());

        Player shooter = (Player) arrow.getShooter();
        if (shooter == null) return;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(shooter);
        if (MathUtils.hasChance((int) getScaling(info, "explosion-chance"))) {
            TNTPrimed TNT = (TNTPrimed) shooter.getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), XEntityType.TNT.get());
            TNT.setFuseTicks(1);
        }
    }

    private static class Old implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onPlayerPickupItem(PlayerPickupArrowEvent event) {
            if (event.getItem().hasMetadata(ArbalistPassive.ARBALIST_ARROW)) event.setCancelled(true);
        }
    }
}