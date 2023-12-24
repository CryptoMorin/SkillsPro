package org.skills.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.skills.utils.LocationUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class MoveManager implements Listener {
    private static final Cache<UUID, Boolean> MOVING = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    private static final Map<UUID, Long> LAST_GROUND_TIME = new HashMap<>();

    public static Duration getLastTimeOnGround(Player player) {
        Long time = LAST_GROUND_TIME.get(player.getUniqueId());
        if (time == null) return Duration.ZERO;
        return Duration.ofMillis(System.currentTimeMillis()).minusMillis(time);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getY() % 1 == 0)
            LAST_GROUND_TIME.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (!LocationUtils.hasMoved(event.getFrom(), event.getTo())) return;
        MOVING.put(event.getPlayer().getUniqueId(), true);
    }

    public static boolean isMoving(Player player) {
        return MOVING.getIfPresent(player.getUniqueId()) != null;
    }
}
