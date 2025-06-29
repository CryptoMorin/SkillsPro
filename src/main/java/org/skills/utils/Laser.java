package org.skills.utils;

import com.cryptomorin.xseries.reflection.XReflection;
import com.cryptomorin.xseries.reflection.jvm.FieldMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.MethodMemberHandle;
import com.cryptomorin.xseries.reflection.jvm.classes.StaticClassHandle;
import com.cryptomorin.xseries.reflection.minecraft.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.kingdoms.utils.Validate;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.cryptomorin.xseries.reflection.XReflection.*;

/**
 * A whole class to create Guardian Beams by reflection.
 * Inspired by the API <a href="https://www.spigotmc.org/resources/guardianbeamapi.18329">GuardianBeamAPI</a>
 * This stimulates the guardian attacking squid pattern.
 * <p>
 * 1.9+
 *
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
            TEAM = "skillspro",
            COLLISION_RULE = "never";
    private static final AtomicInteger TEAM_ID = new AtomicInteger(), LAST_ISSUED_EID = new AtomicInteger(2000000000);

    private final int duration, distanceSquared;
    private final Object destroyPacket;
    private final Object teamCreatePacket;

    private final Map<UUID, Player> players = new ConcurrentHashMap<>(10);
    private final Set<Integer> seen = new HashSet<>();
    private NMSEntityInfo squid;
    private final NMSEntityInfo guardian;
    private Location start, lastEndLocation;
    private Supplier<Location> endLocationTracker;
    private BukkitRunnable run;

    private LivingEntity endEntity;

    private final UUID guardianUUID = UUID.randomUUID();
    private final int guardianID = NMSReflection.generateEID();

    private final UUID squidUUID = UUID.randomUUID();
    private final int squidID = NMSReflection.generateEID();

    // Fields from packet, not the entity object
    private static final String UUIDFieldName = XReflection.v(21, "e").v(19, "d").orElse("b"); // "id"
    private static final String EntityIdFieldName = XReflection.v(21, "d").v(19, "c").orElse("a"); // "uuid"
    private static final boolean SUPPORTS_CUSTOM_IDS = supports(1, 19, 3);

    private Location correctStart;
    private Location correctEnd;

    /**
     * Create a Laser instance
     *
     * @param start              Location where laser will starts
     * @param endLocationTracker Location where laser will ends
     * @param duration           Duration of laser in seconds (<i>-1 if infinite</i>)
     * @param distance           Distance where laser will be visible
     */
    public Laser(Location start, Supplier<Location> endLocationTracker, int duration, int distance) throws Throwable {
        Objects.requireNonNull(start, "Start location is null");

        Location end = endLocationTracker.get();
        if (start.getWorld() != end.getWorld())
            throw new IllegalArgumentException("Laser start world is different from the end location: " + start.getWorld() + " - " + end.getWorld());

        this.start = start;
        this.endLocationTracker = endLocationTracker;
        this.duration = duration;
        this.distanceSquared = distance * distance;

        this.squid = createSquid(end);
        this.guardian = createGuardian(squid.id);
        setTargetEntity(squid.uuid, squid.id);

        teamCreatePacket = NMSReflection.createPacketTeamAddEntities(squid.uuid, guardian.uuid);
        destroyPacket = NMSReflection.createPacketRemoveEntities(squid.id, guardian.id);
    }

    public Laser(Location start, LivingEntity target, int duration, int distance) throws Throwable {
        Objects.requireNonNull(start, "Start location is null");
        Objects.requireNonNull(target, "Target is null");

        if (start.getWorld() != target.getWorld())
            throw new IllegalArgumentException("Laser start world is different from the end location: " + start.getWorld() + " - " + target.getWorld());

        this.start = start;
        this.endEntity = target;
        this.duration = duration;
        this.distanceSquared = distance * distance;

        this.guardian = createGuardian(target.getEntityId());
        setTargetEntity(target.getUniqueId(), target.getEntityId());

        // Keep the squid id and UUID in case we no longer wish to follow a real entity.
        teamCreatePacket = NMSReflection.createPacketTeamAddEntities(squidUUID, guardian.uuid);
        destroyPacket = NMSReflection.createPacketRemoveEntities(squidID, guardian.id);
    }

    private NMSEntityInfo createSquid(Location end) throws Throwable {
        Object squid;

        if (supports(17)) { // What happened is it from 9 or 17 ugh
            squid = NMSReflection.createSquid(end, squidUUID, squidID);
        } else {
            squid = null;
        }

        Object watcherData;
        if (supports(1, 19, 0)) {
            watcherData = NMSExtras.getDataWatcher(squid);
        } else {
            watcherData = NMSReflection.fakeSquidWatcher;
        }

        NMSReflection.setDirtyWatcher(watcherData);
        NMSEntityInfo info = new NMSEntityInfo(squid, NMSReflection.SQUID_TYPE, squidUUID, squidID, watcherData);
        createPackets(info);
        return info;
    }

    private NMSEntityInfo createGuardian(int targetId) throws Throwable {
        Object guardian;
        Location start = getCorrectStart();

        if (supports(17)) {
            guardian = NMSReflection.createGuardian(start, guardianUUID, guardianID);
        } else {
            guardian = null;
        }

        Object watcherData;
        if (supports(1, 19, 0)) {
            watcherData = NMSExtras.getDataWatcher(guardian);
        } else {
            watcherData = NMSReflection.initGuardianWatcher(guardian, targetId);
        }

        NMSEntityInfo info = new NMSEntityInfo(
                guardian, NMSReflection.GUARDIAN_TYPE,
                guardianUUID, guardianID, watcherData
        );
        createPackets(info);
        return info;
    }

    private void createPackets(NMSEntityInfo info) throws Throwable {
        info.createPacket = NMSReflection.createEntitySpawnPacket(info, start);
        info.metadataPacket = NMSReflection.createPacketMetadata(info.id, info.watcherData);
    }

    /**
     * To avoid world checks
     */
    private static double distanceSquared(Location start, Location location) {
        return NumberConversions.square(start.getX() - location.getX()) +
                NumberConversions.square(start.getY() - location.getY()) +
                NumberConversions.square(start.getZ() - location.getZ());
    }

    public void start(Plugin plugin) {
        if (run != null) throw new IllegalStateException("Laser is already started");
        if (endLocationTracker == null && endEntity == null)
            throw new IllegalStateException("No end location or end entity provided");

        run = new BukkitRunnable() {
            final World world = start.getWorld();
            int time = duration;

            @Override
            public void run() {
                for (Player player : world.getPlayers()) {
                    if (isCloseEnough(player.getLocation())) {
                        if (players.put(player.getUniqueId(), player) == null)
                            sendStartPackets(plugin, player, !seen.add(player.getEntityId()));
                    } else if (players.remove(player.getUniqueId()) != null) destroy(player);
                }
                if (--time == 0) cancel();

                if (endLocationTracker != null) {
                    try {
                        Location end = getEndLocation();
                        if (end != null) {
                            Object packet = NMSReflection.teleport(squid, end);
                            for (Player player : players.values()) {
                                MinecraftConnection.sendPacket(player, packet, squid.metadataPacket);
                            }
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                run = null;
                for (Player player : players.values()) destroy(player);
                players.clear();
            }
        };
        run.runTaskTimerAsynchronously(plugin, 0L, 20L);
    }

    protected Location getCorrectStart() {
        if (correctStart == null) {
            // Not needed for the latest version v1.21.4
            correctStart = start.clone();
        }
        return correctStart;
    }

    protected Location getCorrectEnd(Location end) {
        if (correctEnd == null) {
            correctEnd = end.clone();
            correctEnd.subtract(0, 0.5, 0);

            Vector corrective = correctEnd.toVector().subtract(getCorrectStart().toVector()).normalize();
            if (Double.isNaN(corrective.getX())) corrective.setX(0);
            if (Double.isNaN(corrective.getY())) corrective.setY(0);
            if (Double.isNaN(corrective.getZ())) corrective.setZ(0);
            // coordinates can be NaN when start and end are stricly equals
            correctEnd.subtract(corrective);

        }
        return correctEnd;
    }

    public void destroy(Player player) {
        MinecraftConnection.sendPacket(player, destroyPacket);
    }

    public void clear() {
        players.clear();
    }

    public void stop() {
        Validate.isTrue(run != null, "Laser not started");
        run.cancel();
    }

    public void moveStart(Location location) {
        this.start = location;
        correctStart = null;
        Location corrected = getCorrectStart();

        Object packet;
        try {
            // First teleport so the spawn packet can use the new block position.
            packet = NMSReflection.teleport(guardian, corrected);
            guardian.createPacket = NMSReflection.createEntitySpawnPacket(guardian, corrected);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        for (Player player : players.values())
            MinecraftConnection.sendPacket(player, packet);
    }

    public Location getStart() {
        return start;
    }

    public void moveEnd(Supplier<Location> endLocationTracker) {
        this.endLocationTracker = endLocationTracker;
    }

    /**
     * This simply "resets" the color change gradient phase, so
     * calling it doesn't really yield great results.
     */
    public void changeColor() {
        for (Player player : players.values())
            MinecraftConnection.sendPacket(player, guardian.metadataPacket);
    }

    public boolean isStarted() {
        return run != null;
    }

    /**
     * Makes the laser follow an entity (moving end location).
     * <p>
     * This is done client-side by making the fake guardian follow the existing entity.
     * Hence, there is no consuming of server resources.
     *
     * @param entity living entity the laser will follow
     */
    public void attachEndEntity(LivingEntity entity) {
        if (entity.getWorld() != start.getWorld()) {
            throw new IllegalArgumentException("Attached entity is not in the same world as the laser.");
        }

        this.endEntity = entity;
        try {
            setTargetEntity(entity.getUniqueId(), entity.getEntityId());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public LivingEntity getEndEntity() {
        return endEntity;
    }

    @SuppressWarnings("unused")
    private void setTargetEntity(UUID uuid, int id) throws Throwable {
        guardian.watcherData = NMSReflection.initGuardianWatcher(guardian.entity, id);
        guardian.metadataPacket = NMSReflection.createPacketMetadata(guardian.id, guardian.watcherData);
    }

    private void sendStartPackets(Plugin plugin, Player player, boolean hasSeen) {
        List<Object> packets = new ArrayList<>(6);

        // Delayed because the initial cached squid packet gets spawned on the first getEndLocation() value
        // But teleporting the entity will cause a small visual glitch.
        // To fix this, we simply spawn the guardian later after the squid was teleported.
        // 5 Ticks seems to be the best delay.
        // Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
        //     packets.clear();
        //     packets.add(guardian.createPacket);
        //     if (supports(15)) packets.add(guardian.metadataPacket);
        //     if (!hasSeen) packets.add(teamCreatePacket);
        //
        //     Location end = getEndLocation();
        //     if (end != null) {
        //         try {
        //             packets.add(NMSReflection.teleport(squid, end));
        //         } catch (ReflectiveOperationException e) {
        //             throw new RuntimeException(e);
        //         }
        //     }
        //
        //     MinecraftConnection.sendPacket(player, packets.toArray());
        // }, 5L);

        boolean useFakeTarget = squid != null;
        packets.add(guardian.createPacket);
        if (useFakeTarget) packets.add(squid.createPacket);

        if (useFakeTarget) {
            Location end = getEndLocation();
            if (end != null) {
                try {
                    packets.add(NMSReflection.teleport(squid, end));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (supports(15)) {
            packets.add(guardian.metadataPacket);
            if (useFakeTarget) packets.add(squid.metadataPacket);
        }

        if (!hasSeen) packets.add(teamCreatePacket);
        MinecraftConnection.sendPacket(player, packets.toArray());
    }

    private boolean isCloseEnough(Location location) {
        return distanceSquared(start, location) <= distanceSquared ||
                (lastEndLocation != null && distanceSquared(lastEndLocation, location) <= distanceSquared);
    }

    private Location getEndLocation() {
        Location end = endLocationTracker.get();
        return end == null ? lastEndLocation : (lastEndLocation = end);
    }

    private static final class NMSEntityInfo {
        private final Object entity, entityType;
        private UUID uuid;
        private int id;
        private Object watcherData;
        private Object createPacket, metadataPacket;

        private NMSEntityInfo(Object entity, Object entityType, UUID uuid, int id, Object watcherData) {
            this.entity = entity;
            this.entityType = entityType;
            this.uuid = uuid;
            this.id = id;
            this.watcherData = watcherData;
        }
    }

    private static final class NMSReflection {
        private static final MethodHandle PACKET_SPAWN;
        private static final MethodHandle SET_LOCATION;
        private static final MethodHandle BlockPositionCtor;
        private static final MethodHandle setUUID;
        private static final MethodHandle setID;
        private static MethodHandle ClientboundSetPlayerTeamPacket_createAddOrModifyPacket;
        private static MethodHandle PlayerTeam_init;
        private static MethodHandle PlayerTeam_setCollisionRule;
        private static MethodHandle PlayerTeam_getPlayers;
        private static MethodHandle Scoreboard_init;
        private static MethodHandle SynchedEntityData_packDirty;
        private static final Object NMS_WORLD;
        private static final int TEAM_METHOD_ADD = 3;
        private static final Class<?> packetRemove, packetTeleport, packetTeam, packetMetadata;
        private static Constructor<?> watcherConstructor;
        private static final Constructor<?> GUARDIAN_CONSTRUCTOR;

        private static Method watcherSet, watcherRegister, watcherDirty;
        private static Object WATCHER_INVISILIBITY, WATCHER_SPIKES, WATCHER_ATTACK_ID;
        private static Object SQUID_TYPE, GUARDIAN_TYPE;
        private static Object fakeSquidEntity, fakeSquidWatcher;
        private static Object WatcherItemEmptyArray, Team$CollisionRule_NEVER;


        private static final Class<?> guardian = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity.monster")
                .map(MinecraftMapping.MOJANG, "Guardian")
                .map(MinecraftMapping.SPIGOT, "EntityGuardian").unreflect();

        private static MethodHandle
                Entity$defineSynchedData, Entity_blockPosition, Entity_position, Entity_getDeltaMovement,
                SynchedEntityData$Builder_Ctor, SynchedEntityData$Builder_define, SynchedEntityData$Builder_build;

        private static final Class<?> squid = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity.animal")
                .map(MinecraftMapping.MOJANG, "Squid")
                .map(MinecraftMapping.SPIGOT, "EntitySquid")
                .unreflect();

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Object nmsWorld = null;
            MethodHandle setLocation = null, packetSpawn = null, blockPositionCtor = null;

            Class<?> craftWorld = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB).named("CraftWorld").unreflect();
            Class<?> entity = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity").named("Entity").unreflect();
            Class<?> entityLiving = NMSExtras.EntityLiving;
            Class<?> craftSquid = XReflection.ofMinecraft().inPackage(MinecraftPackage.CB, "entity").named("CraftSquid").unreflect();

            Class<?> SynchedEntityData = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.syncher")
                    .map(MinecraftMapping.MOJANG, "SynchedEntityData")
                    .map(MinecraftMapping.SPIGOT, "DataWatcher").unreflect();
            MinecraftClassHandle SynchedEntityData$Builder = of(SynchedEntityData).inner(ofMinecraft()
                    .map(MinecraftMapping.MOJANG, "Builder")
                    .map(MinecraftMapping.SPIGOT, "a"));
            Class<?> DataWatcherObject = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.syncher")
                    .map(MinecraftMapping.MOJANG, "EntityDataAccessor")
                    .map(MinecraftMapping.SPIGOT, "DataWatcherObject")
                    .unreflect();
            Class<?> EntityType = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity")
                    .map(MinecraftMapping.MOJANG, "EntityType")
                    .map(MinecraftMapping.SPIGOT, "EntityTypes").unreflect();
            Class<?> packetSpawnClass = ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundAddEntityPacket")
                    .map(MinecraftMapping.SPIGOT,
                            XReflection.v(19, "PacketPlayOutSpawnEntity").orElse("PacketPlayOutSpawnEntityLiving")).unreflect();

            packetRemove = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundRemoveEntitiesPacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutEntityDestroy")
                    .unreflect();
            if (XReflection.supports(1, 21, 3)) {
                packetTeleport = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                        .map(MinecraftMapping.MOJANG, "ClientboundEntityPositionSyncPacket")
                        .map(MinecraftMapping.SPIGOT, "ClientboundEntityPositionSyncPacket")
                        .map(MinecraftMapping.OBFUSCATED, "acq")
                        .unreflect();
            } else {
                packetTeleport = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                        .map(MinecraftMapping.MOJANG, "ClientboundTeleportEntityPacket")
                        .map(MinecraftMapping.SPIGOT, "PacketPlayOutEntityTeleport")
                        .map(MinecraftMapping.OBFUSCATED, v(21, 1, "afw").v(20, 4, "acz").orElse("afu"))
                        .unreflect();
            }

            packetTeam = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundSetPlayerTeamPacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutScoreboardTeam")
                    .unreflect();
            packetMetadata = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.protocol.game")
                    .map(MinecraftMapping.MOJANG, "ClientboundSetEntityDataPacket")
                    .map(MinecraftMapping.SPIGOT, "PacketPlayOutEntityMetadata")
                    .unreflect();


            MinecraftClassHandle PlayerTeam = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "world.scores")
                    .map(MinecraftMapping.SPIGOT, "ScoreboardTeam")
                    .map(MinecraftMapping.MOJANG, "PlayerTeam");

            MinecraftClassHandle Scoreboard = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "world.scores")
                    .map(MinecraftMapping.SPIGOT, "Scoreboard")
                    .map(MinecraftMapping.MOJANG, "Scoreboard");

            MinecraftClassHandle Team$CollisionRule = ofMinecraft()
                    .inPackage(MinecraftPackage.NMS, "world.scores")
                    .map(MinecraftMapping.SPIGOT, "ScoreboardTeamBase$EnumTeamPush")
                    .map(MinecraftMapping.MOJANG, "Team$CollisionRule");

            if (supports(1, 19, 0)) {
                PlayerTeam_init = PlayerTeam.constructor(Scoreboard.unreflect(), String.class).unreflect();
                PlayerTeam_setCollisionRule = PlayerTeam.method()
                        .returns(void.class).parameters(Team$CollisionRule)
                        .map(MinecraftMapping.MOJANG, "setCollisionRule")
                        .map(MinecraftMapping.OBFUSCATED, "a")
                        .unreflect();
                PlayerTeam_getPlayers = PlayerTeam
                        .method("public Collection<String> getPlayers()")
                        .map(MinecraftMapping.MOJANG, "getPlayers")
                        .map(MinecraftMapping.OBFUSCATED, v(21, 5, "h").orElse("g"))
                        .unreflect();
                Scoreboard_init = Scoreboard.constructor().unreflect();
                Team$CollisionRule_NEVER = Team$CollisionRule.enums()
                        .map(MinecraftMapping.MOJANG, "NEVER")
                        .map(MinecraftMapping.OBFUSCATED, "b")
                        .getEnumConstant();
                ClientboundSetPlayerTeamPacket_createAddOrModifyPacket = XReflection.of(packetTeam)
                        .method().asStatic().returns(packetTeam).parameters(PlayerTeam.unreflect(), boolean.class)
                        .map(MinecraftMapping.MOJANG, "createAddOrModifyPacket")
                        .map(MinecraftMapping.OBFUSCATED, "a")
                        .unreflect();
            }

            try {
                StaticClassHandle Entity = of(entity);
                StaticClassHandle Guardian = of(guardian);
                StaticClassHandle EntityTypeX = of(EntityType);

                String watcherInvis = null; // protected static final DataWatcherObject<Byte> an;
                String watcherSpikes = null, watcherAttacker = null;

                FieldMemberHandle watcherInvisH = Entity.field().getter().returns(DataWatcherObject).makeAccessible();
                FieldMemberHandle watcherSpikesH = Guardian.field().getter().returns(DataWatcherObject).makeAccessible();
                FieldMemberHandle watcherAttackerH = Guardian.field().getter().returns(DataWatcherObject).makeAccessible();

                FieldMemberHandle squadType = EntityTypeX.field().getter().returns(EntityTypeX).makeAccessible();
                FieldMemberHandle guardianType = EntityTypeX.field().getter().returns(EntityTypeX).makeAccessible();

                if (MINOR_NUMBER < 13) {
                    watcherInvis = "Z";
                    watcherSpikes = "bA";
                    watcherAttacker = "bB";
                    SQUID_TYPE = 94;
                    GUARDIAN_TYPE = 68;
                } else if (MINOR_NUMBER == 13) {
                    watcherInvis = "ac";
                    watcherSpikes = "bF";
                    watcherAttacker = "bG";
                    SQUID_TYPE = 70;
                    GUARDIAN_TYPE = 28;
                } else if (MINOR_NUMBER == 14) {
                    watcherInvis = "W";
                    watcherSpikes = "b";
                    watcherAttacker = "bD";
                    SQUID_TYPE = 73;
                    GUARDIAN_TYPE = 30;
                } else if (MINOR_NUMBER == 15) {
                    watcherInvis = "T";
                    watcherSpikes = "b";
                    watcherAttacker = "bA";
                    SQUID_TYPE = 74;
                    GUARDIAN_TYPE = 31;
                } else if (MINOR_NUMBER == 16) {
                    watcherInvis = "S"; // protected static final DataWatcherObject<Byte>    S;
                    watcherSpikes = "b"; // private   static final DataWatcherObject<Boolean> b;
                    watcherAttacker = "d"; // private   static final DataWatcherObject<Integer> d;
                    SQUID_TYPE = 74;
                    GUARDIAN_TYPE = 31;
                } else if (MINOR_NUMBER == 17) {
                    watcherInvis = "Z";
                    watcherSpikes = "b";
                    watcherAttacker = "e";
                    SQUID_TYPE = squadType.named("aJ"); // 86
                    GUARDIAN_TYPE = guardianType.named("K"); // 35
                } else if (MINOR_NUMBER == 18) {
                    watcherInvis = "Z"; // this.Y.b(Z, (byte)(b0 | 1 << i));
                    watcherSpikes = "b";
                    watcherAttacker = "e";
                    SQUID_TYPE = squadType.named("aJ");
                    GUARDIAN_TYPE = guardianType.named("K");
                } else if (MINOR_NUMBER == 19 || MINOR_NUMBER == 20) {
                    watcherInvis = v(20, 2, "ao").orElse("an");
                    watcherSpikes = "b"; // private static final DataWatcherObject<Boolean> b;
                    watcherAttacker = "e"; // private static final DataWatcherObject<Integer> e;
                    SQUID_TYPE = squadType.named("aT");
                    GUARDIAN_TYPE = guardianType.named("V");
                } else { // 21
                    //     public void setInvisible(boolean flag) {
                    //         if (!this.persistentInvisibility) { // Prevent Minecraft from removing our invisibility flag
                    //             this.setSharedFlag(5, flag);
                    //         }
                    //         // CraftBukkit - end
                    //     }
                    watcherInvisH
                            .map(MinecraftMapping.MOJANG, "DATA_SHARED_FLAGS_ID")
                            .map(MinecraftMapping.OBFUSCATED, "am");
                    watcherSpikesH
                            .map(MinecraftMapping.MOJANG, "DATA_ID_MOVING") // Yarn names it SPIKES_RETRACTED
                            .map(MinecraftMapping.OBFUSCATED, "a");
                    watcherAttackerH
                            .map(MinecraftMapping.MOJANG, "DATA_ID_ATTACK_TARGET")
                            .map(MinecraftMapping.OBFUSCATED, "d");

                    SQUID_TYPE = squadType
                            .map(MinecraftMapping.MOJANG, "SQUID")
                            .map(MinecraftMapping.OBFUSCATED, v(21, 5, "bs").orElse("bq"));
                    GUARDIAN_TYPE = guardianType
                            .map(MinecraftMapping.MOJANG, "GUARDIAN")
                            .map(MinecraftMapping.OBFUSCATED, v(21, 5, "aj").orElse("ai"));
                }

                if (SQUID_TYPE instanceof FieldMemberHandle) {
                    SQUID_TYPE = ((FieldMemberHandle) SQUID_TYPE).get(null);
                    GUARDIAN_TYPE = ((FieldMemberHandle) GUARDIAN_TYPE).get(null);
                }

                // this.Y.b(an, (byte)(b0 | 1 << i));
                //          ^^ This is the flag.
                if (watcherInvis == null) {
                    WATCHER_INVISILIBITY = watcherInvisH.get(null);
                    WATCHER_SPIKES = watcherSpikesH.get(null);
                    WATCHER_ATTACK_ID = watcherAttackerH.get(null);
                } else {
                    WATCHER_INVISILIBITY = getField(entity, watcherInvis, null);
                    WATCHER_SPIKES = getField(guardian, watcherSpikes, null); // DATA_ID_MOVING
                    WATCHER_ATTACK_ID = getField(guardian, watcherAttacker, null); // DATA_ID_ATTACK_TARGET
                }

                nmsWorld = craftWorld.getDeclaredMethod("getHandle").invoke(Bukkit.getWorlds().get(0));
                Objects.requireNonNull(nmsWorld, () -> "Could not get NMS world for: " + Bukkit.getWorlds().get(0));

                Class<?> blockPos = ofMinecraft().inPackage(MinecraftPackage.NMS, "core")
                        .map(MinecraftMapping.MOJANG, "BlockPos")
                        .map(MinecraftMapping.SPIGOT, "BlockPosition")
                        .unreflect();

                MinecraftClassHandle Vec3 = ofMinecraft().inPackage(MinecraftPackage.NMS, "world.phys")
                        .map(MinecraftMapping.MOJANG, "Vec3")
                        .map(MinecraftMapping.SPIGOT, "Vec3D")
                        .map(MinecraftMapping.OBFUSCATED, "fbb");

                if (supports(21)) {

                    Class<?> SyncedDataHolder = ofMinecraft()
                            .inPackage(MinecraftPackage.NMS, "network.syncher")
                            .named("SyncedDataHolder")
                            .unreflect();

                    Class<?> WatcherItem = of(SynchedEntityData).inner(ofMinecraft()
                                    .map(MinecraftMapping.MOJANG, "DataItem")
                                    .map(MinecraftMapping.SPIGOT, "Item"))
                            .unreflect();

                    // protected abstract void a(DataWatcher.a var1);
                    // protected abstract void defineSynchedData(DataWatcher.a datawatcher_a);
                    Entity$defineSynchedData = XReflection.of(entity).method()
                            .returns(void.class).parameters(SynchedEntityData$Builder)
                            .map(MinecraftMapping.MOJANG, "defineSynchedData")
                            .map(MinecraftMapping.SPIGOT, "a")
                            .makeAccessible()
                            .reflect();
                    Entity_position = XReflection.of(entity)
                            .method()
                            .returns(Vec3)
                            .map(MinecraftMapping.MOJANG, "position")
                            .map(MinecraftMapping.OBFUSCATED, "dt")
                            .reflect();
                    Entity_getDeltaMovement = XReflection.of(entity)
                            .method()
                            .returns(Vec3)
                            .map(MinecraftMapping.MOJANG, "getDeltaMovement")
                            .map(MinecraftMapping.OBFUSCATED, "dy")
                            .reflect();
                    Entity_blockPosition = XReflection.of(entity)
                            .method()
                            .returns(blockPos)
                            .map(MinecraftMapping.MOJANG, "blockPosition")
                            .map(MinecraftMapping.OBFUSCATED, "dv")
                            .reflect();
                    SynchedEntityData$Builder_Ctor = SynchedEntityData$Builder.constructor()
                            .parameters(SyncedDataHolder)
                            .reflect();
                    SynchedEntityData$Builder_build = SynchedEntityData$Builder.method()
                            .returns(SynchedEntityData)
                            .map(MinecraftMapping.MOJANG, "build")
                            .map(MinecraftMapping.MOJANG, "a")
                            .reflect();
                    SynchedEntityData$Builder_define = SynchedEntityData$Builder.method()
                            .returns(SynchedEntityData$Builder).parameters(DataWatcherObject, Object.class)
                            .map(MinecraftMapping.MOJANG, "define")
                            .map(MinecraftMapping.MOJANG, "a")
                            .reflect();

                    WatcherItemEmptyArray = Array.newInstance(WatcherItem, 0);
                    watcherConstructor = SynchedEntityData.getDeclaredConstructor(SyncedDataHolder, WatcherItemEmptyArray.getClass());
                    watcherConstructor.setAccessible(true);
                } else {
                    watcherConstructor = SynchedEntityData.getDeclaredConstructor(entity);
                }

                {
                    // public <T> void set(DataWatcherObject<T> datawatcherobject, T t0)
                    MethodMemberHandle watcherSetMeth = of(SynchedEntityData).method()
                            .returns(void.class)
                            .map(MinecraftMapping.MOJANG, "set")
                            .map(MinecraftMapping.SPIGOT, v(21, 5, "a").v(18, "b").orElse("set"));

                    if (supports(1, 19, 3)) {
                        watcherSetMeth.parameters(DataWatcherObject, Object.class, boolean.class);
                    } else {
                        watcherSetMeth.parameters(DataWatcherObject, Object.class);
                    }
                    watcherSet = watcherSetMeth.reflectJvm();
                }

                // "Registering datawatcher object after entity initialization"
                if (!supports(21)) {
                    watcherRegister = XReflection.of(SynchedEntityData).method()
                            .returns(void.class).parameters(DataWatcherObject, Object.class)
                            .map(MinecraftMapping.SPIGOT, v(18, "a").orElse("register"))
                            .reflectJvm();
                }

                if (supports(15)) watcherDirty = getMethodIgnoreParams(SynchedEntityData, "markDirty");
                if (supports(19)) SynchedEntityData_packDirty = XReflection.of(SynchedEntityData).method()
                        .map(MinecraftMapping.MOJANG, "packDirty")
                        .map(MinecraftMapping.OBFUSCATED, "b")
                        .returns(List.class)
                        .reflect();

                blockPositionCtor = lookup.findConstructor(blockPos,
                        v(19, MethodType.methodType(void.class, int.class, int.class, int.class)).orElse(
                                MethodType.methodType(void.class, double.class, double.class, double.class)));

                // (int entityId, UUID uuid, double x, double y, double z, float pitch, float yaw, EntityType<?> entityType, int entityData, Vec3 velocity, double headYaw)
                // Using the constructor with blockpos causes the entity's location to be inaccurate.
                packetSpawn = lookup.findConstructor(packetSpawnClass,
                        // v(21, MethodType.methodType(void.class, entity, int.class /* entityData */, blockPos)).
                        v(21, MethodType.methodType(void.class, int.class, UUID.class,
                                double.class, double.class, double.class,
                                float.class, float.class,
                                EntityTypeX.reflect(), int.class, Vec3.reflect(), double.class
                        )).
                                v(19, MethodType.methodType(void.class, entity)).
                                v(17, MethodType.methodType(void.class, entityLiving))
                                .orElse(MethodType.methodType(void.class)));

                if (supports(17))
                    setLocation = XReflection.of(entity).method()
                            .map(MinecraftMapping.MOJANG, v(21, 5, "snapTo").orElse("moveTo"))
                            .map(MinecraftMapping.SPIGOT, v(21, 5, "b").v(21, 4, "e").v(18, "a").orElse("setLocation"))
                            .returns(void.class)
                            .parameters(double.class, double.class, double.class, float.class, float.class)
                            .reflect();

                Object[] entityConstructorParams = supports(14) ?
                        new Object[]{supports(17) ? SQUID_TYPE : EntityType.getDeclaredField("SQUID").get(null), nmsWorld} :
                        new Object[]{nmsWorld};
//                fakeSquid = getMethodIgnoreParams(craftSquid, "getHandle")
//                        .invoke(craftSquid.getDeclaredConstructors()[0].newInstance(null,
//                                squid.getDeclaredConstructors()[0].newInstance(entityConstructorParams))
//                        );

                if (!supports(21)) {
                    fakeSquidEntity = squid.getDeclaredConstructors()[0].newInstance(entityConstructorParams);
                    fakeSquidWatcher = createDataWatcher(fakeSquidEntity, Collections.singletonList(
                            new NMSReflection.WatcherPair(WATCHER_INVISILIBITY, (byte) 32)
                    ));
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }

            setUUID = XReflection.of(entity)
                    .method("public void setUUID(UUID uuid)")
                    .map(MinecraftMapping.OBFUSCATED, v(15, "a_").v(14, 4, "a").orElse("a"))
                    .unreflect();
            setID = XReflection.of(entity)
                    .method("public void setId(int id)")
                    .map(MinecraftMapping.OBFUSCATED, v(14, "e").v(13, "a").orElse("h"))
                    .unreflect();

            BlockPositionCtor = blockPositionCtor;
            NMS_WORLD = nmsWorld;
            SET_LOCATION = setLocation;
            PACKET_SPAWN = packetSpawn;
            GUARDIAN_CONSTRUCTOR = guardian.getDeclaredConstructors()[0];
        }

        private static int generateEID() {
            return LAST_ISSUED_EID.getAndIncrement();
        }

        public static void setDirtyWatcher(Object watcher) throws ReflectiveOperationException {
            if (supports(15)) watcherDirty.invoke(watcher, WATCHER_INVISILIBITY);
        }

        public static Object createSquid(Location location, UUID uuid, int id) throws Throwable {
            // public net.minecraft.world.entity.animal.Squid(net.minecraft.world.entity.EntityType,net.minecraft.world.level.Level)
            Object entity = squid.getDeclaredConstructors()[0].newInstance(SQUID_TYPE, NMS_WORLD);
            if (supports(1, 19, 3)) setEntityIDs(entity, uuid, id);
            teleport(entity, location);

            NMSReflection.createDataWatcher(entity, Collections.singletonList(
                    new NMSReflection.WatcherPair(WATCHER_INVISILIBITY, (byte) 32)
            ));

            return entity;
        }

        public static Object createGuardian(Location location, UUID uuid, int id) throws Throwable {
            Object entity = GUARDIAN_CONSTRUCTOR.newInstance(GUARDIAN_TYPE, NMS_WORLD);
            if (supports(1, 19, 3)) setEntityIDs(entity, uuid, id);
            teleport(entity, location);
            return entity;
        }

        public static void setEntityIDs(Object entity, UUID uuid, int id) throws Throwable {
            setUUID.invoke(entity, uuid);
            setID.invoke(entity, id);
        }

        public static Object initGuardianWatcher(Object guardian, int attackId) throws ReflectiveOperationException {
            return NMSReflection.createDataWatcher(guardian, Arrays.asList(
                    new NMSReflection.WatcherPair(WATCHER_INVISILIBITY, (byte) 32),
                    new NMSReflection.WatcherPair(WATCHER_SPIKES, false),
                    new NMSReflection.WatcherPair(WATCHER_ATTACK_ID, attackId)
            ));
        }

        public static Object createEntitySpawnPacket(NMSEntityInfo info, Location location) throws Throwable {
            if (supports(21)) {
                // Object invoke = BlockPositionCtor.invoke(0, 0, 0);
                // Object currentPos = Entity_blockPosition.invoke(entity);
                // return PACKET_SPAWN.invoke(entity, 0, currentPos);

                // // (int entityId, UUID uuid, double x, double y, double z, float pitch, float yaw,
                // EntityType<?> entityType, int entityData, Vec3 velocity, double headYaw)
                Object velocity = Entity_getDeltaMovement.invoke(info.entity);

                return PACKET_SPAWN.invoke(
                        info.id, info.uuid,
                        location.getX(), location.getY(), location.getZ(),
                        /* pitch */ 0f,
                        /* yaw */ 0f,
                        info.entityType,
                        /* entityData */ (int) 0,
                        velocity,
                        /* headYaw */ 0D
                );
            } else if (supports(17)) {
                Object packet = PACKET_SPAWN.invoke(info.entity);

                // Is this needed for v1.17?
                info.uuid = (UUID) NMSReflection.getField(UUIDFieldName, packet);
                info.id = (int) NMSReflection.getField(EntityIdFieldName, packet);

                return packet;
            } else {
                Object packet = PACKET_SPAWN.invoke();

                setField(packet, "a", info.id);
                setField(packet, "b", info.uuid);
                setField(packet, "c", info.entityType);
                setField(packet, "d", location.getX());
                setField(packet, "e", location.getY());
                setField(packet, "f", location.getZ());
                setField(packet, "j", (byte) (location.getYaw() * 256.0F / 360.0F));
                setField(packet, "k", (byte) (location.getPitch() * 256.0F / 360.0F));
                if (!supports(15)) setField(packet, "m", fakeSquidWatcher);

                return packet;
            }
        }

        @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
        public static Object createPacketRemoveEntities(Integer squidId, int guardianId) throws ReflectiveOperationException {
            int[] entityIds = squidId == null ? new int[]{guardianId} : new int[]{squidId, guardianId};

            if (XReflection.supports(17)) {
                Constructor<?> ctor = packetRemove.getConstructor(int[].class);
                return ctor.newInstance(entityIds);
            }

            Object packet = packetRemove.newInstance();
            setField(packet, "a", entityIds);
            return packet;
        }

        public static void teleport(Object entity, Location location) {
            try {
                SET_LOCATION.invoke(entity, location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public static Object createPacketTeamAddEntities(UUID squidUUID, UUID guardianUUID) throws Throwable {
            String teamName = TEAM + TEAM_ID.getAndIncrement();
            Object packet;

            if (supports(19)) {
                Object team = PlayerTeam_init.invoke(Scoreboard_init.invoke(), teamName);
                PlayerTeam_setCollisionRule.invoke(team, Team$CollisionRule_NEVER);

                @SuppressWarnings("unchecked")
                Collection<String> players = (Collection<String>) PlayerTeam_getPlayers.invoke(team);

                if (squidUUID != null) players.add(squidUUID.toString());
                players.add(guardianUUID.toString());

                packet = ClientboundSetPlayerTeamPacket_createAddOrModifyPacket.invoke(team, true);
            } else if (supports(17)) {
                Collection<String> players = new ArrayList<>(2);
                if (squidUUID != null) players.add(squidUUID.toString());
                players.add(guardianUUID.toString());

                Constructor<?> ctor = packetTeam.getDeclaredConstructor(String.class, int.class, Optional.class, Collection.class);
                ctor.setAccessible(true);
                packet = ctor.newInstance(teamName, TEAM_METHOD_ADD, Optional.empty(), players);
            } else {
                packet = packetTeam.newInstance();
                setField(packet, "a", teamName);
                setField(packet, "f", COLLISION_RULE);
                setField(packet, "i", TEAM_METHOD_ADD);

                Collection<String> players = (Collection<String>) getField(packetTeam, "h", packet);
                if (squidUUID != null) players.add(squidUUID.toString());
                players.add(guardianUUID.toString());
            }

            return packet;
        }

        public static Object teleport(NMSEntityInfo entityInfo, Location location) throws ReflectiveOperationException {
            Object packet;

            if (supports(21)) {
                try {
                    teleport(entityInfo.entity, location);
                    packet = XReflection.of(packetTeleport).method()
                            .returns(packetTeleport)
                            .parameters(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity").named("Entity"))
                            .map(MinecraftMapping.MOJANG, "of")
                            .map(MinecraftMapping.OBFUSCATED, "a")
                            .reflect()
                            .invoke(entityInfo.entity);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return null;
                }
            } else if (supports(17)) {
                try {
                    teleport(entityInfo.entity, location);
                    packet = packetTeleport
                            .getConstructor(XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "world.entity").named("Entity").unreflect())
                            .newInstance(entityInfo.entity);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return null;
                }
            } else {
                packet = packetTeleport.newInstance();
                setField(packet, "a", entityInfo.id);
                setField(packet, "b", location.getX());
                setField(packet, "c", location.getY());
                setField(packet, "d", location.getZ());
                setField(packet, "e", (byte) (location.getYaw() * 256.0F / 360.0F));
                setField(packet, "f", (byte) (location.getPitch() * 256.0F / 360.0F));
                setField(packet, "g", true);
            }

            return packet;
        }

        private static Object createPacketMetadata(int entityId, Object watcher) throws Throwable {
            if (supports(19)) {
                // noinspection JavaReflectionInvocation
                return packetMetadata
                        .getConstructor(int.class, List.class)
                        .newInstance(entityId, SynchedEntityData_packDirty.invoke(watcher));
            } else {
                return packetMetadata
                        .getConstructor(int.class, watcher.getClass(), boolean.class)
                        .newInstance(entityId, watcher, false);
            }
        }

        private static Object createDataWatcher(Object entity, List<NMSReflection.WatcherPair> watcherData) throws ReflectiveOperationException {
            if (supports(21)) {
                Objects.requireNonNull(entity);
                try {
                    // Object builder = SynchedEntityData$Builder_Ctor.invoke(entity);
                    // for (WatcherPair watcherDatum : watcherData) {
                    //     SynchedEntityData$Builder_define.invoke(builder, watcherDatum.id, watcherDatum.value);
                    // }
                    // Entity$defineSynchedData.invoke(entity, builder);
                    // Object watcher = SynchedEntityData$Builder_build.invoke(builder);
                    Object watcher = NMSExtras.getDataWatcher(entity);
                    for (NMSReflection.WatcherPair watcherDatum : watcherData) {
                        tryWatcherSet(watcher, watcherDatum.id, watcherDatum.value);
                    }
                    // return SynchedEntityData$Builder_build.invoke(builder);
                    return watcher;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } else {
                Object watcher = watcherConstructor.newInstance(entity);
                for (NMSReflection.WatcherPair watcherDatum : watcherData) {
                    try {
                        tryWatcherSet(watcher, watcherDatum.id, watcherDatum.value);
                    } catch (NullPointerException ex) {
                        watcherRegister.invoke(watcher, watcherDatum.id, watcherDatum.value);
                        if (supports(14)) setField(watcher, "registrationLocked", false);
                    }
                }
                return watcher;
            }
        }

        private static final class WatcherPair {
            public final Object id;
            public final Object value;

            private WatcherPair(Object id, Object value) {
                this.id = Objects.requireNonNull(id);
                this.value = Objects.requireNonNull(value);
            }
        }

        private static void tryWatcherSet(Object watcher, Object watcherObject, Object watcherData) throws ReflectiveOperationException {
            try {
                if (supports(1, 19, 3)) watcherSet.invoke(watcher, watcherObject, watcherData, true);
                else watcherSet.invoke(watcher, watcherObject, watcherData);
            } catch (InvocationTargetException ex) {
                try {
                    watcherRegister.invoke(watcher, watcherObject, watcherData);
                    if (supports(15)) watcherDirty.invoke(watcher, watcherObject);
                } catch (Throwable ex2) {
                    RuntimeException re = new RuntimeException("Failed to set watcher for " + watcher);
                    re.addSuppressed(ex);
                    re.addSuppressed(ex2);
                    throw re;
                }
            }
        }

        private static Method getMethodIgnoreParams(Class<?> clazz, String name) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name)) return method;
            }
            return null;
        }

        private static void setField(Object instance, String name, Object value) throws ReflectiveOperationException {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(instance, value);
        }

        private static Object getField(Class<?> clazz, String name, Object instance) throws ReflectiveOperationException {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        }

        private static Object getField(String name, Object instance) throws ReflectiveOperationException {
            return getField(instance.getClass(), name, instance);
        }
    }
}
