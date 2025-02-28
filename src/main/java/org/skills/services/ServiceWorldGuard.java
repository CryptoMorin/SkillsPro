package org.skills.services;

import com.cryptomorin.xseries.reflection.XReflection;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.skills.main.SkillsPro;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class ServiceWorldGuard {
    public static final StateFlag ABILITIES_FLAG;
    private static final Object PVP;
    private static final MethodHandle REGION_CONTAINER;
    private static final MethodHandle CREATE_QUERY;
    private static final MethodHandle QUERY_STATE;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle regionContainer = null;
        MethodHandle createQuery = null;
        MethodHandle queryState = null;
        Object pvp = null;

        if (!XReflection.supports(13)) {
            try {
                Class<?> container = Class.forName("com.sk89q.worldguard.bukkit.RegionContainer");
                Class<?> regionQuery = Class.forName("com.sk89q.worldguard.bukkit.RegionQuery");
                Class<?> defaultFlag = Class.forName("com.sk89q.worldguard.protection.flags.DefaultFlag");

                try {
                    pvp = defaultFlag.getField("PVP").get(null);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                regionContainer = lookup.findVirtual(WorldGuardPlugin.class, "getRegionContainer", MethodType.methodType(container));
                createQuery = lookup.findVirtual(container, "createQuery", MethodType.methodType(regionQuery));
                queryState = lookup.findVirtual(regionQuery, "queryState", MethodType.methodType(StateFlag.State.class, Location.class, Player.class, StateFlag[].class));
            } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        REGION_CONTAINER = regionContainer;
        CREATE_QUERY = createQuery;
        QUERY_STATE = queryState;
        PVP = pvp;
    }

    static {
        // https://worldguard.enginehub.org/en/latest/developer/regions/custom-flags/
        StateFlag flag = null;

        if (XReflection.supports(13)) {
            String name = "abilities";
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

            try {
                // create a flag with the name "my-custom-flag", defaulting to true
                // only set our field if there was no error
                flag = new StateFlag(name, true);
                registry.register(flag);
                SkillsPro.get().getLogger().info("Successfully registered 'abilities' WorldGuard flag.");
            } catch (FlagConflictException e) {
                // some other plugin registered a flag by the same name already.
                // you can use the existing flag, but this may cause conflicts - be sure to check type
                Flag<?> existing = registry.get(name);
                if (existing instanceof StateFlag) flag = (StateFlag) existing;
                e.printStackTrace();
            } catch (IllegalStateException ex) {
                SkillsPro.get().getLogger().warning("Could not register WorldGuard flag: " + ex.getMessage());
            }
        } else {
            SkillsPro.get().getLogger().warning("Did not register 'abilities' WorldGuard flag: Unsupported server version " + Bukkit.getVersion());
        }

        ABILITIES_FLAG = flag;
    }

    public static void init() {
    }

    public static boolean canFight(Entity e1, Entity e2) {
        if (!(e1 instanceof Player) || !(e2 instanceof Player)) return true;
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        WorldGuard worldguard = WorldGuard.getInstance();
        RegionContainer regionManager = worldguard.getPlatform().getRegionContainer();
        if (regionManager == null) return true;

        RegionQuery query = regionManager.createQuery();
        return query.queryState(BukkitAdapter.adapt(e1.getLocation()), wg.wrapPlayer((Player) e1), Flags.PVP) != StateFlag.State.DENY &&
                query.queryState(BukkitAdapter.adapt(e1.getLocation()), wg.wrapPlayer((Player) e1), Flags.PVP) != StateFlag.State.DENY;
    }

    public static boolean isPvPOff(Player player) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

        WorldGuard worldguard = WorldGuard.getInstance();
        RegionContainer regionManager = worldguard.getPlatform().getRegionContainer();
        if (regionManager == null) return true;

        RegionQuery query = regionManager.createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
        LocalPlayer wrapper = wg.wrapPlayer(player);

        return query.queryState(loc, wrapper, Flags.PVP) == StateFlag.State.DENY || query.queryState(loc, wrapper, ABILITIES_FLAG) == StateFlag.State.DENY;
    }

    public static boolean canFightOld(Entity e1, Entity e2) {
        if (!(e1 instanceof Player) || !(e2 instanceof Player)) return true;

        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
//        RegionContainer regionContainer = wg.getRegionContainer();
        try {
            Object regionContainer = REGION_CONTAINER.invoke(wg);
            if (regionContainer == null) return true;

//            RegionQuery query = regionContainer.createQuery();
            Object query = CREATE_QUERY.invoke(regionContainer);
            return QUERY_STATE.invoke(query, e1.getLocation(), (Player) e1, PVP) != StateFlag.State.DENY &&
                    QUERY_STATE.invoke(query, e2.getLocation(), (Player) e2, PVP) != StateFlag.State.DENY;
//            if (query.queryState(e1.getLocation(), (Player) e1, DefaultFlag.PVP) != StateFlag.State.DENY) return false;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return true;
        }
    }

    public static boolean isInRegion(Player player, List<String> regionList) {
        WorldGuard worldguard = WorldGuard.getInstance();
        RegionContainer regionManager = worldguard.getPlatform().getRegionContainer();
        if (regionManager == null) return true;

        RegionQuery query = regionManager.createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        for (ProtectedRegion region : regions.getRegions()) {
            if (regionList.contains(region.getId())) return true;
        }
        return false;
    }

    public static boolean isPvPOffOld(Player player) {
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        try {
            Object regionContainer = REGION_CONTAINER.invoke(wg);
            if (regionContainer == null) return true;

            Object query = CREATE_QUERY.invoke(regionContainer);
            return QUERY_STATE.invoke(query, player.getLocation(), (Player) player, PVP) == StateFlag.State.DENY;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return true;
        }
    }
}