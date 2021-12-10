package org.skills.managers.blood;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.skills.main.SkillsConfig;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DamageAestheticsManager implements Listener {
    public static final Set<UUID> MANAGED_PLAYERS = new HashSet<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (MANAGED_PLAYERS.contains(event.getEntity().getUniqueId())) return;

        double damage = event.getFinalDamage();
        if (damage <= 0) return;
        Player player = (Player) event.getEntity();
        if (player.getHealth() - damage <= 0) return;
        int percent = VersionSupport.getHealthPercent(player, event);

        if (SkillsConfig.PULSE_ENABLED.getBoolean() && percent < SkillsConfig.PULSE_HEALTH.getInt()) HeartPulse.pulse(player, percent);
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean()) WorldBorderAPI.send(player, SkillsConfig.RED_SCREEN_DURATION.getInt(), percent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean()) WorldBorderAPI.remove(player);
        if (SkillsConfig.PULSE_ENABLED.getBoolean()) HeartPulse.remove(player);
    }
}
