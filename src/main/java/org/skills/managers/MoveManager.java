package org.skills.managers;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.skills.utils.LocationUtils;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public final class MoveManager implements Listener {
    private static final Map<UUID, PlayerLocationState> LAST_GROUND_TIME = new HashMap<>();
    /**
     * https://minecraft.fandom.com/wiki/Solid_block#Height
     * Should be used for checking if the player is on the ground.
     */
    private static final Map<XMaterial, Double> BLOCK_HEIGHTS = new EnumMap<>(XMaterial.class);

    static {
        for (XMaterial carpet : XTag.CARPETS.getValues()) {
            BLOCK_HEIGHTS.put(carpet, 0.0625);
        }

        BLOCK_HEIGHTS.put(XMaterial.DIRT_PATH, 0.9375);
        BLOCK_HEIGHTS.put(XMaterial.FARMLAND, 0.9375);
        BLOCK_HEIGHTS.put(XMaterial.HONEY_BLOCK, 0.9375);
        BLOCK_HEIGHTS.put(XMaterial.CACTUS, 0.9375);
        BLOCK_HEIGHTS.put(XMaterial.BELL, 0.9375);
        BLOCK_HEIGHTS.put(XMaterial.BIG_DRIPLEAF_STEM, 0.9375);

        for (XMaterial bed : XTag.BEDS.getValues()) {
            BLOCK_HEIGHTS.put(bed, 0.5625);
        }
        BLOCK_HEIGHTS.put(XMaterial.STONECUTTER, 0.5625);
        BLOCK_HEIGHTS.put(XMaterial.CHAIN, 0.59375);

        BLOCK_HEIGHTS.put(XMaterial.REPEATER, 0.125);
        BLOCK_HEIGHTS.put(XMaterial.COMPARATOR, 0.125);
    }

    public static final class PlayerLocationState {
        public long lastGroundTime;
        public boolean wasOnGroundLastTime = true;
        public long lastMove;
    }

    private static final List<Consumer<Player>> ON_PLAYER_HIT_GROUND_LISTENERS = new ArrayList<>();

    public static void registerPlayerHitGround(Consumer<Player> consumer) {
        Objects.requireNonNull(consumer);
        ON_PLAYER_HIT_GROUND_LISTENERS.add(consumer);
    }

    public static Duration getLastTimeOnGround(Player player) {
        PlayerLocationState time = LAST_GROUND_TIME.get(player.getUniqueId());
        if (time == null) return Duration.ZERO;
        return Duration.ofMillis(System.currentTimeMillis()).minusMillis(time.lastGroundTime);
    }

    public static boolean isMoving(Player player) {
        PlayerLocationState time = LAST_GROUND_TIME.get(player.getUniqueId());
        if (time == null) return false;

        long currMilils = System.currentTimeMillis();
        long diff = currMilils - time.lastMove;

        // If they were moving 500ms ago, sure.
        return diff <= 500;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (!LocationUtils.hasMoved(event.getFrom(), event.getTo())) return;

        UUID id = event.getPlayer().getUniqueId();
        PlayerLocationState state = LAST_GROUND_TIME.get(id);
        if (state == null) {
            LAST_GROUND_TIME.put(id, state = new PlayerLocationState());
        }

        long currentMillis = System.currentTimeMillis();
        if (event.getTo().getY() % 1 == 0) {
            if (!state.wasOnGroundLastTime) {
                for (Consumer<Player> onPlayerHitGroundListener : ON_PLAYER_HIT_GROUND_LISTENERS) {
                    onPlayerHitGroundListener.accept(event.getPlayer());
                }
            }

            state.lastGroundTime = currentMillis;
            state.wasOnGroundLastTime = true;
        } else {
            state.wasOnGroundLastTime = false;
        }

        state.lastMove = currentMillis;
    }
}
