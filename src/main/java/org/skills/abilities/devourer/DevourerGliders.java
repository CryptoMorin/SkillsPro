package org.skills.abilities.devourer;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.LocationUtils;
import org.skills.utils.MathUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DevourerGliders extends Ability {
    private static final Set<UUID> TEMP_FLY = new HashSet<>();
    private static final Set<UUID> JUMPING = new HashSet<>();

    public DevourerGliders() {
        super("Devourer", "gliders");
    }

    public static void onDisable() {
        TEMP_FLY.forEach(x -> {
            Player player = Bukkit.getPlayer(x);
            if (player != null) player.setAllowFlight(false);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSprint(PlayerMoveEvent event) {
        if (!LocationUtils.hasMoved(event.getFrom(), event.getTo())) return;

        Player player = event.getPlayer();
        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return;
        if (ServiceHandler.isNPC(player)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                SkilledPlayer info = DevourerGliders.this.checkup(player);
                if (info == null) return;
                UUID id = player.getUniqueId();
                int lvl = info.getImprovementLevel(DevourerGliders.this);

                boolean onGround = MathUtils.isInteger(event.getTo().getY());
                if (onGround) {
                    JUMPING.remove(id);
                    if (TEMP_FLY.remove(id)) {
                        Bukkit.getScheduler().runTask(SkillsPro.get(), () -> {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                        });
                    }
                }

                if (lvl >= getExtraScaling(info, "double-jump-level") && player.getGameMode() != GameMode.CREATIVE && !JUMPING.contains(id)) {
                    if (!onGround) {
                        if (!player.getAllowFlight()) {
                            TEMP_FLY.add(id);
                            JUMPING.add(id);
                            Bukkit.getScheduler().runTask(SkillsPro.get(), () -> player.setAllowFlight(true));
                        }
                    }
                }

                if (!player.isSprinting()) return;
                if (!player.hasPotionEffect(XPotion.SPEED.parsePotionEffectType()))
                    Bukkit.getScheduler().runTask(SkillsPro.get(), () -> applyEffects(info, player));

            }
        }.runTaskAsynchronously(SkillsPro.get());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (SkillsConfig.isInDisabledWorld(player.getLocation())) return;
        if (ServiceHandler.isNPC(player)) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (info.getImprovementLevel(this) < getExtraScaling(info, "double-jump-level")) return;

        ParticleDisplay dis = ParticleDisplay.simple(player.getLocation(), Particle.SMOKE_LARGE);
        dis.count = 70;
        dis.offset(0.5, 0, 0.5);
        dis.spawn();
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (info.getImprovementLevel(this) < 3) return;

        if (TEMP_FLY.remove(player.getUniqueId())) {
            player.setAllowFlight(false);
            player.setFlying(false);
            event.setCancelled(true);
        }
        player.setVelocity(player.getLocation().getDirection().multiply(1.3).setY(getScaling(info)));

        ParticleDisplay dis = ParticleDisplay.simple(player.getLocation(), Particle.CLOUD);
        dis.count = 70;
        dis.offset(0.5, 0.5, 0.5);
        dis.spawn();
    }
}
