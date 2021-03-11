package org.skills.utils;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * https://minecraft.gamepedia.com/Day-night_cycle
 */
public enum MinecraftTime {
    // 20 minutes
    FULL(20 * 60 * 20),
    DAY(1000),
    NOON(6000),
    SUNSET(12000),
    NIGHT(13000),
    MIDNIGHT(18000),
    FULL_MOON(14000),
    SUNRISE(22200);

    private final long time;

    MinecraftTime(long time) {
        this.time = time;
    }

    public static boolean isDay(World world) {
        return world.getTime() > 23460 || world.getTime() < 12000;
    }

    public void setTime(Player player) {
        player.setPlayerTime(time, false);
    }
}