package org.skills.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.skills.data.managers.SkilledPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsEventManager implements Listener {
    protected static final EnumMap<SkillsEventType, SkillsGlobalBonus> EVENTS = new EnumMap<>(SkillsEventType.class);

    public static EnumMap<SkillsEventType, SkillsGlobalBonus> getEvents() {
        return EVENTS;
    }

    public static SkillsGlobalBonus getEvent(SkillsEventType type) {
        SkillsGlobalBonus event = EVENTS.get(type);
        if (event != null && event.hasExpired()) {
            event.stop();
            EVENTS.remove(type);
            return null;
        }
        return event;
    }

    public static boolean isEventRunning(SkillsEventType type) {
        SkillsGlobalBonus event = getEvent(type);
        if (event == null) return false;
        return event.isActive();
    }

    private static void handleBonuses(Player player, boolean forceRemove) {
        SkilledPlayer skilledPlayer = SkilledPlayer.getSkilledPlayer(player);
        List<SkillsBonus> bonuses = new ArrayList<>(skilledPlayer.getBonuses());
        bonuses.forEach(SkillsBonus::start); // Start the bossbar and stuff for personal bonuses

        bonuses.addAll(EVENTS.values());
        bonuses = bonuses.stream()
                .filter(SkillsBonus::isActive)
                .filter(x -> x.appliesTo(player))
                .filter(x -> x.bossBar != null)
                .collect(Collectors.toList());
        for (SkillsBonus bonus : bonuses) {
            boolean remove = forceRemove || !bonus.appliesTo(player);
            if (remove && bonus.bossBar != null) {
                bonus.bossBar.removePlayer(player);
            } else {
                bonus.bossBar.addPlayer(player);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        handleBonuses(player, false);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        handleBonuses(player, true);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        handleBonuses(player, false);
    }
}
