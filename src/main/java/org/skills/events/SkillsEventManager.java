package org.skills.events;

import org.bukkit.boss.BossBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.EnumMap;

public class SkillsEventManager implements Listener {
    protected static final EnumMap<SkillsEventType, SkillsEvent> EVENTS = new EnumMap<>(SkillsEventType.class);

    public static EnumMap<SkillsEventType, SkillsEvent> getEvents() {
        return EVENTS;
    }

    public static SkillsEvent getEvent(SkillsEventType type) {
        return EVENTS.get(type);
    }

    public static boolean isEventRunning(SkillsEventType type) {
        SkillsEvent event = getEvent(type);
        if (event == null) return false;
        return event.isActive();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (SkillsEvent skillEvent : EVENTS.values()) {
            BossBar bossBar = skillEvent.bossBar;
            if (bossBar != null) bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        for (SkillsEvent skillEvent : EVENTS.values()) {
            BossBar bossBar = skillEvent.bossBar;
            if (bossBar != null) bossBar.removePlayer(event.getPlayer());
        }
    }
}
