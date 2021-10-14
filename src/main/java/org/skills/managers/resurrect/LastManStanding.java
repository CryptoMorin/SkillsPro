package org.skills.managers.resurrect;

import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.LocationUtils;

final class LastManStanding {
    protected final float speed;
    protected final GameMode gameMode;
    protected final Player player;
    private final BukkitTask invulnerability, bleedOut;
    protected Player reviver;
    protected int progress;
    protected BukkitTask reviveTask;

    public LastManStanding(Player player) {
        this.player = player;
        speed = player.getWalkSpeed();
        gameMode = player.getGameMode();

        int invulnerable = SkillsConfig.LAST_BREATH_INVULNERABILITY.getInt();
        if (invulnerable > 0) {
            player.setInvulnerable(true);
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
                    player.setHealth(0);
                }
            }.runTaskLater(SkillsPro.get(), bleed * 20L);
        } else bleedOut = null;
    }

    public void resetProgress() {
        this.progress = 0;
        this.reviver = null;
        if (this.reviveTask != null) this.reviveTask.cancel();
    }

    public int progress() {
        return ++this.progress;
    }

    public void recover() {
        end();
        player.setWalkSpeed(speed);
        if (gameMode != null) player.setGameMode(gameMode);
        player.setSwimming(false);
        player.setSprinting(false);
        standWouldYouKindly();

        Location location = player.getLocation().add(0, 1, 0);
        player.sendBlockChange(location, location.getBlock().getBlockData());
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
    }
}