package org.skills.managers.blood;

import com.cryptomorin.xseries.XWorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.skills.main.SkillsConfig;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.time.Duration;
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

        if (SkillsConfig.PULSE_ENABLED.getBoolean() && percent < SkillsConfig.PULSE_HEALTH.getInt())
            HeartPulse.pulse(player, percent);
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean())
            send(player, SkillsConfig.RED_SCREEN_DURATION.getInt(), percent);
    }

    public static void send(Player player, int durationSeconds, int percent) {
        int start = SkillsConfig.RED_SCREEN_HEALTH.getInt();
        if (percent > start) return;
        send(player, durationSeconds, start, percent);
    }

    private static final double BORDER_SIZE = 1000000;

    public static void send(Player player, int durationSeconds, int start, int percent) {
        // Distances and radius in these methods still need tweaking to use the best optimal settings
        // in terms of visual and delay accurancy and also considering player movement.
        // start = 50 by default
        // 50 is 100% of 50 -> 34400000
        // 43 is  85% of 50
        // 28 is  56% of 50
        // 8  is  16% of 50 -> 6400000
        int percentOfStart = (int) MathUtils.getPercent(percent, start);
        int warningDistance = (int) MathUtils.percentOfAmount(percentOfStart, BORDER_SIZE * 40);

        //  public static void worldborder(Player player, int dist, double oldRadius, double newRadius, long delay) {
        XWorldBorder wb = XWorldBorder.getOrCreate(player);
        wb.setWarningDistance(1);

        if (durationSeconds == 0) {
            wb.setSize(warningDistance, Duration.ZERO);
            wb.setSizeLerpTarget(warningDistance);
            // worldborder(player, 1, warningDistance, warningDistance, 0);
        } else {
            wb.setSize(BORDER_SIZE * 100, Duration.ofSeconds(durationSeconds));
            wb.setSizeLerpTarget(warningDistance);
            // worldborder(player, 1, warningDistance, BORDER_SIZE * 100, durationSeconds * 1000L);
        }

        wb.send();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean()) XWorldBorder.remove(player);
        if (SkillsConfig.PULSE_ENABLED.getBoolean()) HeartPulse.remove(player);
    }
}
