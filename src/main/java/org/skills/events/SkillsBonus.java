package org.skills.events;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.LanguageManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathUtils;
import org.skills.utils.NoEpochDate;
import org.skills.utils.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class SkillsBonus implements Cloneable {
    protected final Duration duration;
    protected final long start;
    protected final SkillsEventType type;
    protected final String multiplier;
    protected transient boolean stopped;
    protected transient BossBarHandler bossBar;

    public SkillsBonus(SkillsEventType type, String multiplier, Duration duration, long start) {
        this.type = Objects.requireNonNull(type, "Event type cannot be null");
        this.multiplier = Objects.requireNonNull(multiplier, "Event multiplier cannot be null");
        this.duration = Objects.requireNonNull(duration);
        this.start = start;

        if (start <= 0) throw new IllegalArgumentException("Start time not invalid: " + this);
    }

    public void stop() {
        this.stopped = true;
        if (bossBar != null) bossBar.removeAll();
    }

    @Override
    public abstract SkillsBonus clone();

    public void start() {
        this.stopped = false;
    }

    public boolean appliesTo(Player player) {
        List<String> disabledWorlds = new ArrayList<>(SkillsConfig.DISABLED_WORLDS_PLUGIN.getStringList());
        if (this.type == SkillsEventType.XP) {
            disabledWorlds.addAll(SkillsConfig.DISABLED_WORLDS_XP_GAIN.getStringList());
        } else if (this.type == SkillsEventType.SOUL) {
            disabledWorlds.addAll(SkillsConfig.DISABLED_WORLDS_SOUL_GAIN.getStringList());
        }
        return !disabledWorlds.contains(player.getWorld().getName());
    }

    public final class BossBarHandler implements Runnable {
        private BukkitTask bukkitTask;
        private final BossBar bossBar;
        private final String title;

        protected BossBarHandler(ConfigurationSection bossConfig) {
            this.title = bossConfig.getString("title");
            this.bossBar = StringUtils.parseBossBarFromConfig(null, bossConfig);
        }

        private void start() {
            this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(SkillsPro.get(), this, 0L, 1L);
        }

        private boolean isTaskRunning() {
            return this.bukkitTask != null;
        }

        public void addPlayer(Player player) {
            if (!isTaskRunning()) start();
            this.bossBar.addPlayer(player);
        }

        public void removePlayer(Player player) {
            this.bossBar.removePlayer(player);
            if (isTaskRunning() && this.bossBar.getPlayers().isEmpty()) cancel();
        }

        private void cancel() {
            bukkitTask.cancel();
            bukkitTask = null;
        }

        protected void removeAll() {
            this.bossBar.removeAll();
        }

        @Override
        public void run() {
            if (!isActive()) {
                cancel();
                if (!stopped) stop();
                return;
            }

            float percent = Math.abs((float) getTimeLeft().toMillis() / duration.toMillis());
            bossBar.setProgress(percent);
            bossBar.setTitle(LanguageManager.buildMessage(title, null,
                    "%type%", type.toString(),
                    "%multiplier%", String.valueOf(getMultiplierFor(null)),
                    "%time%", getDisplayDuration()));
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '(' + type.name() + ", " + multiplier + ", " + duration + ", " + start + ')';
    }

    public final SkillsEventType getType() {
        return this.type;
    }

    public String getMultiplier() {
        return this.multiplier;
    }

    /**
     * @return 1 if no multiplier applied, otherwise another number.
     */
    public static double getMultiplierFor(Player player, SkillsEventType type) {
        SkilledPlayer skilledPlayer = SkilledPlayer.getSkilledPlayer(player);
        List<SkillsBonus> bonuses = new ArrayList<>(skilledPlayer.getBonus(type));
        if (SkillsEventManager.isEventRunning(type)) bonuses.add(SkillsEventManager.getEvent(type));
        return bonuses.stream()
                .filter(SkillsBonus::isActive)
                .filter(x -> x.appliesTo(player))
                .map(x -> x.getMultiplierFor(player))
                .reduce(1.0, (a, b) -> a * b);
    }

    public final double getMultiplierFor(OfflinePlayer player) {
        String placeholded = this.multiplier;
        if (player != null)
            placeholded = ServiceHandler.translatePlaceholders(player, this.multiplier);
        return MathUtils.evaluateEquation(placeholded);
    }

    public final String getDisplayDuration() {
        return new NoEpochDate(getTimeLeft().toMillis()).format(SkillsConfig.TIME_FORMAT.getString());
    }

    public final long getStart() {
        return this.start;
    }

    public final Duration getTimeLeft() {
        Duration left = this.duration.minus(getTimePassed());
        return left.isNegative() ? Duration.ZERO : left;
    }

    public final Duration getTimePassed() {
        return Duration.ofMillis(System.currentTimeMillis() - this.start);
    }

    public final boolean isActive() {
        return !stopped && !hasExpired();
    }

    public final boolean hasExpired() {
        return getTimeLeft().isZero();
    }

    public final Duration getDuration() {
        return duration;
    }
}
