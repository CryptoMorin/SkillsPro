package org.skills.utils;

import com.cryptomorin.xseries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A whole class to create Guardian Beams by reflection </br>
 * Inspired by the API <a href="https://www.spigotmc.org/resources/guardianbeamapi.18329">GuardianBeamAPI</a></br>
 * <b>1.9 -> 1.16</b>
 * @author SkytAsul
 * @see <a href="https://github.com/SkytAsul/GuardianBeam">GitHub page</a>
 */
public final class Laser {
    /**
     * https://minidigger.github.io/MiniMappingViewer/#/spigot/server/1.16.4/packetplayoutscoreboardteam
     * https://wiki.vg/Protocol#Teams
     * Max length: 16
     */
    private static final String
            TEAM = "skill",
            COLLISION_RULE = "never";
    private static final AtomicInteger TEAM_ID = new AtomicInteger(), LAST_ISSUED_EID = new AtomicInteger(2000000000);

    private final int duration, distanceSquared;
    private final Object createGuardianPacket, createSquidPacket, teamAddPacket, destroyPacket,
            metadataPacketGuardian, metadataPacketSquid, fakeGuardianDataWatcher;

    private final int squid, guardian;
    private final UUID squidUUID, guardianUUID;

    private final Map<Integer, Player> players = new HashMap<>(10);
    private Location start, end;
    private BukkitRunnable run;

    /**
     * Create a Laser instance
     * @param start    Location where laser will starts
     * @param end      Location where laser will ends
     * @param duration Duration of laser in seconds (<i>-1 if infinite</i>)
     * @param distance Distance where laser will be visible
     */
    public Laser(Location start, Location end, int duration, int distance) throws ReflectiveOperationException {
        if (start.getWorld() != end.getWorld())
            throw new IllegalArgumentException("Laser start world is different from the end location: " + start.getWorld() + " - " + end.getWorld());

        this.start = start;
        this.end = end;
        this.duration = duration;
        this.distanceSquared = distance * distance;

        createSquidPacket = Packets.createPacketSquidSpawn(end);
        squid = (int) Packets.getField(Packets.packetSpawn, "a", createSquidPacket);
        squidUUID = (UUID) Packets.getField(Packets.packetSpawn, "b", createSquidPacket);
        metadataPacketSquid = Packets.createPacketMetadata(squid, Packets.fakeSquidWatcher);
        Packets.setDirtyWatcher(Packets.fakeSquidWatcher);

        fakeGuardianDataWatcher = Packets.createFakeDataWatcher();
        createGuardianPacket = Packets.createPacketGuardianSpawn(start, fakeGuardianDataWatcher, squid);
        guardian = (int) Packets.getField(Packets.packetSpawn, "a", createGuardianPacket);
        guardianUUID = (UUID) Packets.getField(Packets.packetSpawn, "b", createGuardianPacket);
        metadataPacketGuardian = Packets.createPacketMetadata(guardian, fakeGuardianDataWatcher);

        teamAddPacket = Packets.createPacketTeamAddEntities(squidUUID, guardianUUID);
        destroyPacket = Packets.createPacketRemoveEntities(squid, guardian);
    }

    private static double distanceSquared(Location start, Location location) {
        return NumberConversions.square(start.getX() - location.getX()) +
                NumberConversions.square(start.getY() - location.getY()) +
                NumberConversions.square(start.getZ() - location.getZ());
    }

    public void start(Plugin plugin) {
        if (run != null) throw new IllegalStateException("Laser is already started");
        run = new BukkitRunnable() {
            final World world = start.getWorld();
            int time = duration;

            @Override
            public void run() {
                for (Player player : world.getPlayers()) {
                    if (isCloseEnough(player.getLocation())) {
                        if (players.put(player.getEntityId(), player) == null) sendStartPackets(player);
                    } else if (players.remove(player.getEntityId()) != null) {
                        ReflectionUtils.sendPacket(player, destroyPacket);
                    }
                }
                if (--time == 0) cancel();
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                for (Player player : players.values()) ReflectionUtils.sendPacket(player, destroyPacket);
                run = null;
            }
        };
        run.runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    public void clear() {
        players.clear();
    }

    public void stop() {
        if (run == null) throw new IllegalStateException("Laser is not started");
        run.cancel();
    }

    public void moveStart(Location location) throws ReflectiveOperationException {
        this.start = location;
        Object packet = Packets.createPacketMoveEntity(start, guardian);
        for (Player player : players.values()) ReflectionUtils.sendPacket(player, packet);
    }

    public Location getStart() {
        return start;
    }

    public void moveEnd(Location location) throws ReflectiveOperationException {
        this.end = location;
        Object packet = Packets.createPacketMoveEntity(end, squid);
        for (Player player : players.values()) ReflectionUtils.sendPacket(player, packet);
    }

    public Location getEnd() {
        return end;
    }

    public void callColorChange() {
        for (Player player : players.values()) ReflectionUtils.sendPacket(player, metadataPacketGuardian);
    }

    public boolean isStarted() {
        return run != null;
    }

    private void sendStartPackets(Player p) {
        List<Object> packets = new ArrayList<>(6);
        packets.add(createSquidPacket);
        packets.add(createGuardianPacket);
        if (Packets.version > 14) {
            packets.add(metadataPacketSquid);
            packets.add(metadataPacketGuardian);
        }
//        packets.add(Packets.packetTeamCreate);
        packets.add(teamAddPacket);
        ReflectionUtils.sendPacketSync(p, packets.toArray());
    }

    private boolean isCloseEnough(Location location) {
        return distanceSquared(start, location) <= distanceSquared ||
                distanceSquared(end, location) <= distanceSquared;
    }

    private static final class Packets {
        private static final int version = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1).split("_")[1]);
        private static final String npack = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + '.';
        private static final String cpack = Bukkit.getServer().getClass().getPackage().getName() + '.';
        //        private static final MethodHandle SPAWN_ID, SPAWN_UUID, SPAWN_TYPE, SPAWN_X, SPAWN_Y, SPAWN_Z, SPAWN_YAW, SPAWN_PITCH, SPAWN_WATCHER;
        private static Object packetTeamCreate;
        private static Constructor<?> watcherConstructor;
        private static Method watcherSet;
        private static Method watcherRegister;
        private static Method watcherDirty;
        private static Class<?> packetSpawn;
        private static Class<?> packetRemove;
        private static Class<?> packetTeleport;
        private static Class<?> packetTeam;
        private static Class<?> packetMetadata;
        private static Object watcherObject1; // invisilibity
        private static Object watcherObject2; // spikes
        private static Object watcherObject3; // attack id
        private static int SQUID_ID, GUARDIAN_ID;
        private static Object fakeSquid;
        private static Object fakeSquidWatcher;

        static {
            try {
                String watcherName1 = null, watcherName2 = null, watcherName3 = null;
                if (version < 13) {
                    watcherName1 = "Z";
                    watcherName2 = "bA";
                    watcherName3 = "bB";
                    SQUID_ID = 94;
                    GUARDIAN_ID = 68;
                } else if (version == 13) {
                    watcherName1 = "ac";
                    watcherName2 = "bF";
                    watcherName3 = "bG";
                    SQUID_ID = 70;
                    GUARDIAN_ID = 28;
                } else if (version == 14) {
                    watcherName1 = "W";
                    watcherName2 = "b";
                    watcherName3 = "bD";
                    SQUID_ID = 73;
                    GUARDIAN_ID = 30;
                } else if (version == 15) {
                    watcherName1 = "T";
                    watcherName2 = "b";
                    watcherName3 = "bA";
                    SQUID_ID = 74;
                    GUARDIAN_ID = 31;
                } else if (version >= 16) {
                    watcherName1 = "S";
                    watcherName2 = "b";
                    watcherName3 = "d";
                    SQUID_ID = 74;
                    GUARDIAN_ID = 31;
                }
                watcherObject1 = getField(Class.forName(npack + "Entity"), watcherName1, null);
                watcherObject2 = getField(Class.forName(npack + "EntityGuardian"), watcherName2, null);
                watcherObject3 = getField(Class.forName(npack + "EntityGuardian"), watcherName3, null);

                watcherConstructor = Class.forName(npack + "DataWatcher").getDeclaredConstructor(Class.forName(npack + "Entity"));
                watcherSet = getMethodIgnoreParams(Class.forName(npack + "DataWatcher"), "set");
                watcherRegister = getMethodIgnoreParams(Class.forName(npack + "DataWatcher"), "register");
                if (version >= 15) watcherDirty = getMethodIgnoreParams(Class.forName(npack + "DataWatcher"), "markDirty");
                packetSpawn = Class.forName(npack + "PacketPlayOutSpawnEntityLiving");
                packetRemove = Class.forName(npack + "PacketPlayOutEntityDestroy");
                packetTeleport = Class.forName(npack + "PacketPlayOutEntityTeleport");
                packetTeam = Class.forName(npack + "PacketPlayOutScoreboardTeam");
                packetMetadata = Class.forName(npack + "PacketPlayOutEntityMetadata");

//                packetTeamCreate = packetTeam.newInstance();
//                setField(packetTeamCreate, "a", TEAM);
//                setField(packetTeamCreate, "i", 0);
//                setField(packetTeamCreate, "f", COLLISION_RULE);

                Object world = Class.forName(cpack + "CraftWorld").getDeclaredMethod("getHandle").invoke(Bukkit.getWorlds().get(0));
                Object[] entityConstructorParams = version < 14 ?
                        new Object[]{world} :
                        new Object[]{Class.forName(npack + "EntityTypes").getDeclaredField("SQUID").get(null), world};
                fakeSquid = getMethodIgnoreParams(Class.forName(cpack + "entity.CraftSquid"), "getHandle")
                        .invoke(Class.forName(cpack + "entity.CraftSquid")
                                .getDeclaredConstructors()[0].newInstance(null, Class.forName(npack + "EntitySquid")
                                .getDeclaredConstructors()[0].newInstance(entityConstructorParams)));
                fakeSquidWatcher = createFakeDataWatcher();
                tryWatcherSet(fakeSquidWatcher, watcherObject1, (byte) 32);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        private static int generateEID() {
            return LAST_ISSUED_EID.getAndIncrement();
        }

        public static Object createFakeDataWatcher() throws ReflectiveOperationException {
            Object watcher = watcherConstructor.newInstance(fakeSquid);
            if (version > 13) setField(watcher, "registrationLocked", false);
            return watcher;
        }

        public static void setDirtyWatcher(Object watcher) throws ReflectiveOperationException {
            if (version >= 15) watcherDirty.invoke(watcher, watcherObject1);
        }

        public static Object createPacketSquidSpawn(Location location) throws ReflectiveOperationException {
            Object packet = packetSpawn.newInstance();
            setField(packet, "a", generateEID());
            setField(packet, "b", UUID.randomUUID());
            setField(packet, "c", SQUID_ID);
            setField(packet, "d", location.getX());
            setField(packet, "e", location.getY());
            setField(packet, "f", location.getZ());
            setField(packet, "j", (byte) (location.getYaw() * 256.0F / 360.0F));
            setField(packet, "k", (byte) (location.getPitch() * 256.0F / 360.0F));
            if (version <= 14) setField(packet, "m", fakeSquidWatcher);
            return packet;
        }

        public static Object createPacketGuardianSpawn(Location location, Object watcher, int squidId) throws ReflectiveOperationException {
            Object packet = packetSpawn.newInstance();
            setField(packet, "a", generateEID());
            setField(packet, "b", UUID.randomUUID());
            setField(packet, "c", GUARDIAN_ID);
            setField(packet, "d", location.getX());
            setField(packet, "e", location.getY());
            setField(packet, "f", location.getZ());
            setField(packet, "j", (byte) (location.getYaw() * 256.0F / 360.0F));
            setField(packet, "k", (byte) (location.getPitch() * 256.0F / 360.0F));
            tryWatcherSet(watcher, watcherObject1, (byte) 32);
            tryWatcherSet(watcher, watcherObject2, false);
            tryWatcherSet(watcher, watcherObject3, squidId);
            if (version <= 14) setField(packet, "m", watcher);
            return packet;
        }

        public static Object createPacketRemoveEntities(int squidId, int guardianId) throws ReflectiveOperationException {
            Object packet = packetRemove.newInstance();
            setField(packet, "a", new int[]{squidId, guardianId});
            return packet;
        }

        public static Object createPacketMoveEntity(Location location, int entityId) throws ReflectiveOperationException {
            Object packet = packetTeleport.newInstance();
            setField(packet, "a", entityId);
            setField(packet, "b", location.getX());
            setField(packet, "c", location.getY());
            setField(packet, "d", location.getZ());
            setField(packet, "e", (byte) (location.getYaw() * 256.0F / 360.0F));
            setField(packet, "f", (byte) (location.getPitch() * 256.0F / 360.0F));
            setField(packet, "g", true);
            return packet;
        }

        @SuppressWarnings("unchecked")
        public static Object createPacketTeamAddEntities(UUID squidUUID, UUID guardianUUID) throws ReflectiveOperationException {
            Object packet = packetTeam.newInstance();
            setField(packet, "a", TEAM + TEAM_ID.getAndIncrement());
            setField(packet, "f", COLLISION_RULE);
            setField(packet, "i", 3); // Method 3: add players to team

            Collection<String> players = (Collection<String>) getField(packetTeam, "h", packet);
            players.add(squidUUID.toString());
            players.add(guardianUUID.toString());
            return packet;
        }

        private static Object createPacketMetadata(int entityId, Object watcher) throws ReflectiveOperationException {
            return packetMetadata.getConstructor(int.class, watcher.getClass(), boolean.class).newInstance(entityId, watcher, false);
        }

        private static void tryWatcherSet(Object watcher, Object watcherObject, Object watcherData) throws ReflectiveOperationException {
            try {
                watcherSet.invoke(watcher, watcherObject, watcherData);
            } catch (InvocationTargetException ex) {
                watcherRegister.invoke(watcher, watcherObject, watcherData);
                if (version >= 15) watcherDirty.invoke(watcher, watcherObject);
            }
        }

        private static Method getMethodIgnoreParams(Class<?> clazz, String name) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name)) return method;
            }
            return null;
        }

        private static void setField(Object instance, String name, Object value) throws ReflectiveOperationException {
            Objects.requireNonNull(instance);
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(instance, value);
        }

        private static Object getField(Class<?> clazz, String name, Object instance) throws ReflectiveOperationException {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        }
    }
}
