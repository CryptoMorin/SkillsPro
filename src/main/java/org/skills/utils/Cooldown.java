package org.skills.utils;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A simple cooldown system using {@link Cache}.
 * Expired cooldowns will be automatically removed.
 */
public class Cooldown {
    private static final Map<String, Cooldown> COOLDOWNS = new HashMap<>();
    private final long time;
    private final long start;

    /**
     * Starts a new cooldown for the given ID with the name.
     * @param id   the ID of the entity.
     * @param name the cooldown's name.
     * @param time the time in milliseconds.
     */
    public Cooldown(Object id, String name, long time, TimeUnit timeUnit) {
        this.time = timeUnit.toMillis(time);
        this.start = System.currentTimeMillis();

        COOLDOWNS.put(id + name, this);
    }

    public Cooldown(Object id, String name, long time) {
        this(id, name, time, TimeUnit.MILLISECONDS);
    }

    public static boolean isInCooldown(Object id, String name) {
        return getTimeLeft(id, name) != 0;
    }

    public static Cooldown stop(Object id, String name) {
        return COOLDOWNS.remove(id + name);
    }

    public static Cooldown getCooldown(Object id, String name) {
        return COOLDOWNS.get(id + name);
    }

    public static long getTimeLeft(Object id, String name) {
        Cooldown cooldown = getCooldown(id, name);
        if (cooldown == null) return 0;

        long now = System.currentTimeMillis();
        long difference = now - cooldown.start;
        if (difference >= cooldown.time) {
            stop(id, name);
            return 0;
        }
        return cooldown.time - difference;
    }
}
