package org.skills.managers.resurrect;

import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XWorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.managers.blood.DamageAestheticsManager;
import org.skills.managers.blood.HeartPulse;
import org.skills.utils.LocationUtils;
import org.skills.utils.StringUtils;

final class LastManStanding {
    protected final float speed;
    protected final GameMode gameMode;
    protected final Player player;
    protected final Object dataWatcher;
    private final BukkitTask invulnerability, bleedOut, bossBarUpdate;
    private final BossBar bossBar;
    protected Player reviver;
    protected int progress;
    protected BukkitTask reviveTask;

    public LastManStanding(Player player) {
        this.player = player;
        this.dataWatcher = LastBreath.registerDataWatcher(player, true);
        this.speed = player.getWalkSpeed();
        this.gameMode = player.getGameMode();
        this.bossBar = initBossBar();

        int invulnerable = SkillsConfig.LAST_BREATH_INVULNERABILITY.getInt();
        if (invulnerable > 0) {
            player.setInvulnerable(true); // Note: This doesn't fucking work for creative mode players.
            invulnerability = new BukkitRunnable() {
                @Override
                public void run() {
                    player.setInvulnerable(false);
                }
            }.runTaskLater(SkillsPro.get(), invulnerable * 20L);
        } else invulnerability = null;

        int bleed = SkillsConfig.LAST_BREATH_BLEED_OUT.getInt();
        if (bleed > 0) {
            bleedOut = new BukkitRunnable() {
                @Override
                public void run() {
                    die();
                    XSound.play(SkillsConfig.LAST_BREATH_SOUNDS_BLEED_OUT.getString(), x -> x.forPlayers(player));
                }
            }.runTaskLater(SkillsPro.get(), bleed * 20L);

            if (bossBar != null) {
                bossBarUpdate = new BukkitRunnable() {
                    float seconds = bleed;

                    @Override
                    public void run() {
                        bossBar.setProgress(seconds / (bleed + 1)); // For the sake of lags
                        seconds -= 0.05f;
                    }
                }.runTaskTimerAsynchronously(SkillsPro.get(), 0L, 1L);
            } else bossBarUpdate = null;
        } else {
            bleedOut = null;
            bossBarUpdate = null;
        }

        aesthetics();
        DamageAestheticsManager.MANAGED_PLAYERS.add(player.getUniqueId());
    }

    public BossBar initBossBar() {
        ConfigurationSection bossConfig = SkillsConfig.LAST_BREATH_BOSSBAR.getSection();
        if (!bossConfig.getBoolean("enabled")) return null;

        BossBar bossBar = StringUtils.parseBossBarFromConfig(player, bossConfig);
        bossBar.addPlayer(player);
        return bossBar;
    }

    public void die() {
        player.setHealth(0);
        resetState();
        LastBreath.LAST_MEN_STANDING.remove(player.getEntityId());
    }

    public void aesthetics() {
        player.setSwimming(true);
        player.setSprinting(true);
        player.setGameMode(GameMode.ADVENTURE);
        player.setWalkSpeed((float) SkillsConfig.LAST_BREATH_SPEED.getDouble());
        player.setFoodLevel(0);

        XSound.play(SkillsConfig.LAST_BREATH_SOUNDS_START.getString(), x -> x.forPlayers(player));
        XSound.play(SkillsConfig.LAST_BREATH_SOUNDS_MUSIC.getString(), x -> x.forPlayers(player));

        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            if (SkillsConfig.PULSE_ENABLED.getBoolean()) HeartPulse.pulse(player, 0, 0);
            if (SkillsConfig.RED_SCREEN_ENABLED.getBoolean()) DamageAestheticsManager.send(player, 0, 0);
        }, 1L);
    }

    public void resetProgress() {
        this.progress = 0;
        this.reviver = null;
        player.setFoodLevel(0);
        if (this.reviveTask != null) this.reviveTask.cancel();
    }

    public int progress() {
        return ++this.progress;
    }

    public void revive() {
        XSound.play(SkillsConfig.LAST_BREATH_SOUNDS_REVIVE.getString(), x -> x.forPlayers(player));
        resetState();
        standWouldYouKindly();

        Location location = player.getLocation().add(0, 1, 0);
        player.sendBlockChange(location, location.getBlock().getBlockData());

        HeartPulse.remove(player);
        XWorldBorder.remove(player);
    }

    public void resetState() {
        end();
        player.setWalkSpeed(speed);
        player.setGameMode(gameMode);
        player.setSwimming(false);
        player.setSprinting(false);
        player.setInvulnerable(false);
        XSound.stopMusic(player);
    }

    private void standWouldYouKindly() {
        Bukkit.getScheduler().runTaskAsynchronously(SkillsPro.get(), () -> {
            Object metadata = LastBreath.registerDataWatcher(player, false);
            Location loc = player.getLocation();
            for (Player player : player.getWorld().getPlayers()) {
                if (this.player != player && LocationUtils.distanceSquared(loc, player.getLocation()) < LastBreath.VIEW_DISTANCE) {
                    ReflectionUtils.sendPacketSync(player, metadata);
                }
            }
        });
    }

    public void end() {
        if (invulnerability != null) invulnerability.cancel();
        if (bleedOut != null) bleedOut.cancel();
        if (reviveTask != null) reviveTask.cancel();
        if (bossBar != null) {
            bossBar.removeAll();
            bossBarUpdate.cancel();
        }
        DamageAestheticsManager.MANAGED_PLAYERS.remove(player.getUniqueId());
    }
}