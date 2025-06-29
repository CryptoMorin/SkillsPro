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
            redScreen(player, SkillsConfig.RED_SCREEN_DURATION.getInt(), percent);
    }

    public static void redScreen(Player player, int durationSeconds, int hpPercent) {
        int criticalHpPercent = SkillsConfig.RED_SCREEN_HEALTH.getInt();
        if (hpPercent > criticalHpPercent) return;
        redScreen(player, durationSeconds, criticalHpPercent, hpPercent);
    }

    /**
     * @param durationSeconds   The duration which this effect stays on the screen.
     * @param criticalHpPercent The starting percentage point of critical player health. (50% by default)
     * @param hpPercent         The current player health percentage.
     */
    public static void redScreen(Player player, int durationSeconds, int criticalHpPercent, int hpPercent) {
        // We put the border as far as possible to make the change caused by player movement minimum.
        // We calculate the lerp size (the old border radius) based on the player's HP.
        // The lower the player's HP, the closer this radius is, then we move it to the max border size.
        //
        // It's still unknown how the red tint is shown even when the warning distance is set 1 while
        // the border is extremely far away.
        //
        // Note: This method can be easily tested using "/minecraft:damage @p <damage>"
        double percentOfStart = MathUtils.getPercent(hpPercent, criticalHpPercent);
        double lerpTarget = Math.max(1, MathUtils.percentOfAmount(percentOfStart, XWorldBorder.MAX_SIZE));

        XWorldBorder wb = XWorldBorder.getOrCreate(player);
        wb.setCenter(player.getLocation());
        wb.setWarningDistance(1);
        wb.setSizeLerpTarget(lerpTarget);

        if (durationSeconds == 0) {
            wb.setSize(lerpTarget, Duration.ZERO);
        } else {
            wb.setSize(XWorldBorder.MAX_SIZE, Duration.ofSeconds(durationSeconds));
        }

        wb.update();
    }

    public static void redScreen(Player player) {
        XWorldBorder wb = XWorldBorder.getOrCreate(player);
        wb.setCenter(player.getLocation());
        wb.setWarningDistance(Integer.MAX_VALUE);
        wb.setSize(XWorldBorder.MAX_SIZE, Duration.ZERO);
        wb.update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean()) XWorldBorder.remove(player);
        if (SkillsConfig.PULSE_ENABLED.getBoolean()) HeartPulse.remove(player);
    }
}
