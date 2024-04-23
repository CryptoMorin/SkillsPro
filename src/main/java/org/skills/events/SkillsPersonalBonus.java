package org.skills.events;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.skills.main.SkillsConfig;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public final class SkillsPersonalBonus extends SkillsBonus {
    private transient UUID id;

    public SkillsPersonalBonus(UUID id, SkillsEventType type, String multiplier, Duration duration, long start) {
        super(type, multiplier, duration, start);
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public SkillsBonus clone() {
        return new SkillsPersonalBonus(id, type, multiplier, duration, start);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }

    @Override
    public boolean appliesTo(Player player) {
        return player.getUniqueId().equals(this.id) && super.appliesTo(player);
    }

    @Override
    public void start() {
        super.start();
        if (this.bossBar != null) return;

        ConfigurationSection bossSection = SkillsConfig.BOSSBAR_BONUSES.getSection();
        if (bossSection.getBoolean("enabled")) {
            this.bossBar = new BossBarHandler(bossSection);
            Player player = getPlayer();
            if (player != null) this.bossBar.addPlayer(player);
        }
    }

    @ApiStatus.Internal
    public void setPlayerId(UUID id) {
        Objects.requireNonNull(id);
        if (this.id != null) throw new IllegalArgumentException("Bonus ID already set");
        this.id = id;
    }

    public UUID getPlayerId() {
        return this.id;
    }
}
