package org.skills.managers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.skills.main.locale.SkillsLang;

import java.util.HashMap;
import java.util.Map;

public final class DebugManager implements Listener {
    public static final Map<Integer, Player> ACTIVE = new HashMap<>();

    private static void debug(EntityDamageByEntityEvent event, String priority) {
        if (ACTIVE.isEmpty()) return;
        double damage = event.getDamage();
        double finDamage = event.getFinalDamage();

        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        String msg = SkillsLang.COMMAND_DEBUG_MESSAGE.parse("%damager%", damager.getName(), "%custom_damager%", damager.getCustomName(),
                "%entity%", entity.getName(), "%custom_entity%", entity.getCustomName(),
                "%damage%", damage, "%final_damage%", finDamage, "%priority%", priority);

        for (Player player : ACTIVE.values()) player.sendMessage(msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ACTIVE.remove(event.getPlayer().getEntityId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDamageLowest(EntityDamageByEntityEvent event) {
        debug(event, "&4LOWEST");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamageLow(EntityDamageByEntityEvent event) {
        debug(event, "&6LOW");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDamageNormal(EntityDamageByEntityEvent event) {
        debug(event, "&fNORMAL");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamageHigh(EntityDamageByEntityEvent event) {
        debug(event, "&eHIGH");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamageHighest(EntityDamageByEntityEvent event) {
        debug(event, "&2HIGHEST");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamageMonitor(EntityDamageByEntityEvent event) {
        debug(event, "&9&lMONITOR");
    }
}
