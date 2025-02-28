package org.skills.services.manager;

import com.cryptomorin.xseries.reflection.XReflection;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.services.*;
import org.skills.services.mobs.ServiceEliteMobs;
import org.skills.services.mobs.ServiceLevelledMobs;
import org.skills.services.mobs.ServiceLorinthsRpgMobs;
import org.skills.services.mobs.ServiceMythicMobs;
import org.skills.services.placeholders.ServicePlaceholderAPI;
import org.skills.services.placeholders.SkillsPlaceholders;
import org.skills.utils.Pair;
import org.skills.utils.StringUtils;

import java.util.*;
import java.util.function.BiFunction;

public class ServiceHandler {
    private static final Set<String> PRESENT = new HashSet<>();
    private static final Map<Plugin, BiFunction<Player, Player, Boolean>> FRIENDLY_HANDLERS = new HashMap<>();

    public static void init(SkillsPro plugin) {
        for (String plugins : plugin.getDescription().getSoftDepend()) {
            Plugin dependency = Bukkit.getPluginManager().getPlugin(plugins);
            if (dependency != null && dependency.isEnabled()) {
                PRESENT.add(plugins);
                MessageHandler.sendConsolePluginMessage("&e" + dependency.getName() + " &3found and hooked.");
            } else MessageHandler.sendConsolePluginMessage("&e" + plugins + " &cnot found.");
        }
    }

    public static boolean isAvailable(String service) {
        return PRESENT.contains(service);
    }

    public static String translatePlaceholders(OfflinePlayer player, String str) {
        Objects.requireNonNull(player, "Cannot translate placeholder for null player");
        Validate.isTrue(str != null, "Cannot translate null string");

        str = StringUtils.replace(str, "%player%", player.getName());
        if (player.isOnline()) str = StringUtils.replace(str, "%displayname%", ((Player) player).getDisplayName());

        return isAvailable("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders(player, str)
                : SkillsPlaceholders.translatePlaceholders(player, str);
    }

    public static boolean isMyPet(Entity e) {
        return isAvailable("MyPet") && ServiceMyPet.isMyPet(e);
    }

    public static Player getPetOwner(Entity entity) {
        return ServiceMyPet.getPetOwner(entity);
    }

    public static boolean isNPC(Entity e) {
        return isAvailable("Citizens") && ServiceCitizens.isNPC(e);
    }

    public static boolean areFriendly(Entity e1, Entity e2) {
        if (e1.getEntityId() == e2.getEntityId()) return true;
        if (isAvailable("Citizens")) if (ServiceCitizens.isNPC(e1) || ServiceCitizens.isNPC(e2)) return true;
        if (!(e1 instanceof Player)) return false;
        if (!(e2 instanceof Player)) return false;

        Player player1 = (Player) e1;
        Player player2 = (Player) e2;

        SkilledPlayer info1 = SkilledPlayer.getSkilledPlayer(player1);
        SkilledPlayer info2 = SkilledPlayer.getSkilledPlayer(player2);
        if (info1.isFrendly(info2)) return true;

        if (Bukkit.getPluginManager().getPlugin("Kingdoms") != null) {
            return !ServiceKingdoms.canFight(player1, player2);
        } else {
            Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
            if (factions != null) {
                try {
                    if (Bukkit.getPluginManager().getPlugin("MasiveCore") != null && !factions.getDescription().getWebsite().contains("factionsuuid")) {
                        if (!ServiceMassiveFactions.canFight(player1, player2)) return true;
                    } else {
                        if (!ServiceFactions.canFight(player1, player2)) return true;
                    }
                } catch (Throwable e) {
                    SLogger.error("Factions support has encountered an error!");
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Plugin, BiFunction<Player, Player, Boolean>> handlers : FRIENDLY_HANDLERS.entrySet()) {
            try {
                Boolean result = handlers.getValue().apply(player1, player2);
                if (result != null && result) return true;
            } catch (Throwable err) {
                MessageHandler.sendConsolePluginMessage("&4An error occurred while '&e" + handlers.getKey().getName() + "&4' plugin was handling friendly checks&8:");
                err.printStackTrace();
            }
        }
        return false;
    }

    public static void logPartyChat(Player player, String message) {
        if (isAvailable("DiscordSRV")) ServiceDiscordSRV.logPartyChat(player, message);
    }

    public static boolean isKingdomMob(LivingEntity entity) {
        return isAvailable("Kingdoms") && ServiceKingdoms.isKingdomMob(entity);
    }

    public static boolean canFight(Entity e1, Entity e2) {
        if (areFriendly(e1, e2)) return false;
        if (isAvailable("Residence")) return ServiceResidence.canFight(e1, e2);
        if (isAvailable("WorldGuard")) {
            try {
                if (XReflection.supports(13)) {
                    if (!ServiceWorldGuard.canFight(e1, e2)) return false;
                } else {
                    if (!ServiceWorldGuard.canFightOld(e1, e2)) return false;
                }

            } catch (Throwable e) {
                Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
                String ver = wg == null ? "NOT FOUND" : wg.getDescription().getVersion();
                SLogger.error("WorldGuard support [" + ver + "] has encountered an error!");
                e.printStackTrace();
            }
        }
        return true;
    }

    public static Pair<String, Number> getMobProperties(LivingEntity entity) {
        if (entity.hasMetadata("NPC")) return Pair.of(ServiceCitizens.getNPCName(entity), 0);
        if (isAvailable("LevelledMobs")) {
            Pair<String, Number> property = ServiceLevelledMobs.getMobProperties(entity);
            if (property != null) return property;
        }
        if (isAvailable("MythicMobs")) {
            Pair<String, Number> property = ServiceMythicMobs.getMobProperties(entity);
            if (property != null) return property;
        }
//        if (isAvailable("Boss")) {
//            Pair<String, Number> property = ServiceBoss.getMobProperties(entity);
//            if (property != null) return property;
//        }
        if (isAvailable("EliteMobs")) {
            Pair<String, Number> proeprty = ServiceEliteMobs.getMobProperties(entity);
            if (proeprty != null) return proeprty;
        }
        if (isAvailable("LorinthsRpgMobs")) {
            return ServiceLorinthsRpgMobs.getMobProperties(entity);
        }
        return null;
    }

    public static boolean isPvPOff(Player player) {
        if (!isAvailable("WorldGuard")) return false;
        if (XReflection.supports(13)) {
            return ServiceWorldGuard.isPvPOff(player);
        } else {
            return ServiceWorldGuard.isPvPOffOld(player);
        }
    }

    public static boolean isInRegion(Player player, List<String> regions) {
        if (regions == null || regions.isEmpty()) return false;
        if (isAvailable("WorldGuard")) return false;
        return ServiceWorldGuard.isInRegion(player, regions);
    }

    public static void registerPlaceHolders() {
        if (isAvailable("PlaceholderAPI")) {
            boolean registered = new ServicePlaceholderAPI().register();
            if (registered) MessageHandler.sendConsolePluginMessage("&3Successfully registered placeholders.");
            else MessageHandler.sendConsolePluginMessage("&cCould not register placeholders!");
        }
    }

    public static void registerFriendlyHandler(Plugin plugin, BiFunction<Player, Player, Boolean> handler) {
        if (plugin instanceof SkillsPro)
            throw new IllegalArgumentException("Friendly registrar cannot be SkillsPro plugin");
        Objects.requireNonNull(handler, "Friendly handler function cannot be null");
        if (FRIENDLY_HANDLERS.containsKey(plugin))
            throw new IllegalArgumentException("Plugin has already registered a friendly handler: " + plugin.getName());
        FRIENDLY_HANDLERS.put(plugin, handler);
    }

    public static void unregisterFriendlyHandler(Plugin plugin) {
        Objects.requireNonNull(plugin, "Cannot unregister friendly handler for null plugin");
        if (FRIENDLY_HANDLERS.remove(plugin) == null)
            throw new IllegalArgumentException("Plugin did not register any friendly handlers: " + plugin.getName());
    }
}
