package org.skills.managers.blood;

import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.MathUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class WorldBorderAPI {
    private static final MethodHandle PACKET, WORLD, GET_HANDLE, WORLDBORDER, WORLDBORDER_WORLD, CENTER, DISTANCE, SIZE, TRANSITION;
    private static final Object INITIALIZE;
    private static final double BORDER_SIZE = 1000000;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object initialize = null;
        MethodHandle packet = null, world = null, getHandle = null, worldborder = null, worldborderWorld = null, center = null, distance = null, size = null, transition = null;
        Class<?> wb = ReflectionUtils.getNMSClass("WorldBorder");
        Class<?> craftPlayer = ReflectionUtils.getCraftClass("entity.CraftPlayer");
        Class<?> entityPlayer = ReflectionUtils.getNMSClass("EntityPlayer");
        Class<?> worldServer = ReflectionUtils.getNMSClass("WorldServer");

        Class<?> wbType;
        try {
            wbType = Class.forName("EnumWorldBorderAction");
        } catch (ClassNotFoundException e) {
            wbType = ReflectionUtils.getNMSClass("PacketPlayOutWorldBorder$EnumWorldBorderAction");
        }

        try {
            packet = lookup.findConstructor(ReflectionUtils.getNMSClass("PacketPlayOutWorldBorder"), MethodType.methodType(void.class, wb, wbType));
            getHandle = lookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            world = lookup.findGetter(entityPlayer, "world", ReflectionUtils.getNMSClass("World"));

            worldborder = lookup.findConstructor(wb, MethodType.methodType(void.class));
            worldborderWorld = lookup.findSetter(wb, "world", worldServer);
            center = lookup.findVirtual(wb, "setCenter", MethodType.methodType(void.class, double.class, double.class));
            distance = lookup.findVirtual(wb, "setWarningDistance", MethodType.methodType(void.class, int.class));
            size = lookup.findVirtual(wb, "setSize", MethodType.methodType(void.class, double.class));
            transition = lookup.findVirtual(wb, "transitionSizeBetween", MethodType.methodType(void.class, double.class, double.class, long.class));

            for (Object type : wbType.getEnumConstants()) {
                if (type.toString().equals("INITIALIZE")) {
                    initialize = type;
                    break;
                }
            }
        } catch (Exception ex) {
            MessageHandler.sendConsolePluginMessage(
                    "&4There was a problem while attempting to get &eNMS Classes&4. Please report this to developer&8: &e" + ex.getMessage());
        }

        GET_HANDLE = getHandle;
        WORLD = world;
        PACKET = packet;

        WORLDBORDER = worldborder;
        WORLDBORDER_WORLD = worldborderWorld;
        CENTER = center;
        SIZE = size;
        DISTANCE = distance;
        TRANSITION = transition;
        INITIALIZE = initialize;
    }

    public static void send(Player player, int percent) {
        int start = SkillsConfig.RED_SCREEN_HEALTH.getInt();
        if (percent > start) return;

        int percentOfStart = (int) MathUtils.getPercent(percent, start);
        int dist = (int) MathUtils.percentOfAmount(percentOfStart, BORDER_SIZE * 40);

        worldborder(player, 1, dist, BORDER_SIZE * 100, SkillsConfig.RED_SCREEN_DURATION.getInt() * 1000);
    }

    public static void sendZ(Player player, int as, double lol) {
        worldborder(player, as, lol, lol, 0);
    }

    public static void remove(Player player) {
        double size = player.getWorld().getWorldBorder().getSize();
        Location location = player.getWorld().getWorldBorder().getCenter();

        try {
            Object nmsPlayer = GET_HANDLE.invoke(player);
            Object world = WORLD.invoke(nmsPlayer);

            Object worldBorder = WORLDBORDER.invoke();
            WORLDBORDER_WORLD.invoke(worldBorder, world);
            CENTER.invoke(worldBorder, location.getX(), location.getZ());
            SIZE.invoke(worldBorder, size);

            Object packet = PACKET.invoke(worldBorder, INITIALIZE);
            ReflectionUtils.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void worldborder(Player player, int dist, double oldRadius, double newRadius, long delay) {
        Location location = player.getLocation();
        try {
            Object nmsPlayer = GET_HANDLE.invoke(player);
            Object world = WORLD.invoke(nmsPlayer);

            Object worldBorder = WORLDBORDER.invoke();
            WORLDBORDER_WORLD.invoke(worldBorder, world);
            CENTER.invoke(worldBorder, location.getX(), location.getZ());
            DISTANCE.invoke(worldBorder, dist);
            TRANSITION.invoke(worldBorder, oldRadius, newRadius, delay);

            Object packet = PACKET.invoke(worldBorder, INITIALIZE);
            ReflectionUtils.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
