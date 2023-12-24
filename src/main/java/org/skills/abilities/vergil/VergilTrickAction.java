package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.RayTraceResult;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SLogger;
import org.skills.managers.DamageManager;
import org.skills.managers.MoveManager;
import org.skills.utils.Cooldown;

import java.util.concurrent.TimeUnit;

public class VergilTrickAction extends InstantActiveAbility {
    private static final String TRICK_BEHIND = "TRICK_BEHIND";

    public VergilTrickAction() {
        super("Vergil", "trick_action");
        setPvPBased(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTrickBehind(EntityDamageByEntityEvent event) {
        if (commonDamageCheckupReverse(event)) return;
        Player player = (Player) event.getEntity();

        SkilledPlayer info = basicCheckup(player);
        SLogger.info("info is: " + info);
        if (info == null) return;
        //if (info.getAbilityLevel(this) == 3) return;
        new Cooldown(player.getUniqueId(), TRICK_BEHIND, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        Location playerLocation = player.getEyeLocation();
        ParticleDisplay display = ParticleDisplay.of(Particle.CLOUD).withCount(200).offset(1);
        display.spawn(playerLocation.clone());

        SLogger.info("last on ground: " + MoveManager.getLastTimeOnGround(player).toMillis());
        Location teleportTo;
        boolean changeYawPitch = true;

        if (player.isSneaking() && Cooldown.isInCooldown(player.getUniqueId(), TRICK_BEHIND)) {
            LivingEntity src = DamageManager.getLastSourceDamager(player, true);
            teleportTo = src.getLocation().add(src.getLocation().getDirection().multiply(-2));
            teleportTo.setDirection(teleportTo.toVector().subtract(src.getLocation().toVector()).multiply(-1));
            changeYawPitch = false;
        } else if (!player.isOnGround()) {
            teleportTo = player.getLocation().add(0, 7, 0);
            if (XPotion.SLOW_FALLING.isSupported()) player.addPotionEffect(XPotion.SLOW_FALLING.buildPotionEffect(20 * 5, 20));
        } else {
            RayTraceResult trace = player.getWorld().rayTrace(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    50, FluidCollisionMode.SOURCE_ONLY, true, 2, (e) -> player != e && e instanceof LivingEntity);
            if (trace != null) {
                if (trace.getHitBlock() != null) {
                    teleportTo = trace.getHitBlock().getLocation().add(0, 1, 0);
                } else if (trace.getHitEntity() != null) {
                    teleportTo = trace.getHitEntity().getLocation();
                } else {
                    teleportTo = trace.getHitPosition().toLocation(player.getWorld());
                }
            } else {
                teleportTo = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(30));
            }
        }

        if (changeYawPitch) {
            teleportTo.setYaw(playerLocation.getYaw());
            teleportTo.setPitch(playerLocation.getPitch());
        }

        display.spawn(teleportTo);
        player.teleport(teleportTo);
    }
}
