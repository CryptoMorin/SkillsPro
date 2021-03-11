package org.skills.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.LanguageManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathUtils;
import org.skills.utils.NoEpochDate;
import org.skills.utils.StringUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SkillsEvent implements Cloneable {
    public final long time;
    public final long start;
    private final transient SkillsEventType type;
    private final String multiplier;
    private final transient UUID id;
    public transient boolean stopped;
    protected transient BossBar bossBar;

    public SkillsEvent(UUID id, SkillsEventType type, String multiplier, long time, TimeUnit timeUnit, long start) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "Event type cannot be null");
        ;
        this.multiplier = Objects.requireNonNull(multiplier, "Event multiplier cannot be null");
        ;
        this.time = timeUnit.toMillis(time);
        this.start = start;
    }

    public SkillsEvent(UUID id, SkillsEventType type, String multiplier, long time, TimeUnit timeUnit) {
        this(id, type, multiplier, time, timeUnit, System.currentTimeMillis());
    }

    public SkillsEvent(UUID id, SkillsEventType type, String multiplier, long time) {
        this(id, type, multiplier, time, TimeUnit.MILLISECONDS, System.currentTimeMillis());
    }

    public void stop() {
        this.stopped = true;
        if (bossBar != null) bossBar.removeAll();
        SkillsEventManager.EVENTS.remove(type);
    }

    @Override
    public SkillsEvent clone() {
        try {
            return (SkillsEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startEvent() {
        if (SkillsEventManager.isEventRunning(type)) throw new IllegalArgumentException(type + " global event is already running");
        SkillsEventManager.getEvents().put(type, this);
        if (SkillsConfig.BOSSBAR_EVENTS_ENABLED.getBoolean()) {
            ConfigurationSection bossConfig = SkillsConfig.BOSSBAR_EVENTS.getSection();
            String title = bossConfig.getString("title");
            this.bossBar = StringUtils.parseBossBarFromConfig(null, bossConfig);
            for (Player players : Bukkit.getOnlinePlayers()) bossBar.addPlayer(players);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) {
                        cancel();
                        if (!stopped) stop();
                        return;
                    }

                    float percent = Math.abs((float) getTimeLeft() / time);
                    bossBar.setProgress(percent);
                    bossBar.setTitle(LanguageManager.buildMessage(title, null,
                            "%type%", type.toString(), "%multiplier%", String.valueOf(calcMultiplier(null)),
                            "%time%", new NoEpochDate(getTimeLeft()).format(SkillsConfig.TIME_FORMAT.getString())));
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
        }
    }

    public void startBonus(Player player) {
        if (SkillsConfig.BOSSBAR_BONUSES_ENABLED.getBoolean()) {
            ConfigurationSection bossConfig = SkillsConfig.BOSSBAR_BONUSES.getSection();
            String title = bossConfig.getString("title");
            this.bossBar = StringUtils.parseBossBarFromConfig(null, bossConfig);
            bossBar.addPlayer(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isActive()) {
                        cancel();
                        if (!stopped) stop();
                        return;
                    }

                    float percent = Math.abs((float) getTimeLeft() / time);
                    bossBar.setProgress(percent);
                    bossBar.setTitle(LanguageManager.buildMessage(title, null,
                            "%type%", type.toString(), "%multiplier%", String.valueOf(calcMultiplier(null)),
                            "%time%", new NoEpochDate(getTimeLeft()).format(SkillsConfig.TIME_FORMAT.getString())));
                }
            }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
        }
    }

    @Override
    public String toString() {
        return type.name() + ", " + multiplier + ", " + time + ", " + start;
    }

    public SkillsEventType getType() {
        return this.type;
    }

    public String getMultiplier() {
        return this.multiplier;
    }

    public double calcMultiplier(OfflinePlayer player) {
        String placeholded = this.multiplier;
        if (player != null)
            placeholded = ServiceHandler.translatePlaceholders(player, this.getMultiplier());
        return MathUtils.evaluateEquation(placeholded);
    }

    public String getDisplayTime() {
        return new NoEpochDate(getTimeLeft()).format(SkillsConfig.TIME_FORMAT.getString());
    }

    public long getStart() {
        return this.start;
    }

    public long getTimeLeft() {
        return Math.max(0, this.time - getPassedTime());
    }

    public long getPassedTime() {
        return System.currentTimeMillis() - this.start;
    }

    public boolean isActive() {
        return !stopped && getTimeLeft() != 0;
    }

    public UUID getId() {
        return this.id;
    }

    public long getTime() {
        return time;
    }
}
