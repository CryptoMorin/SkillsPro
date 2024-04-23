package org.skills.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.skills.main.SkillsConfig;

import java.time.Duration;

public final class SkillsGlobalBonus extends SkillsBonus {
    public SkillsGlobalBonus(SkillsEventType type, String multiplier, Duration duration, long start) {
        super(type, multiplier, duration, start);
    }

    public void stop() {
        super.stop();
        SkillsEventManager.EVENTS.remove(type);
    }

    @Override
    public SkillsBonus clone() {
        return new SkillsGlobalBonus(type, multiplier, duration, start);
    }

    @Override
    public void start() {
        super.start();
        SkillsEventManager.getEvents().put(getType(), this);
        if (this.bossBar != null) return;
        ConfigurationSection bossSection = SkillsConfig.BOSSBAR_EVENTS.getSection();
        if (bossSection.getBoolean("enabled")) {
            this.bossBar = new BossBarHandler(bossSection);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (appliesTo(player)) this.bossBar.addPlayer(player);
            }
        }
    }
}
