package org.skills.services.manager;

import com.cryptomorin.xseries.XMaterial;
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
import org.skills.services.mobs.ServiceBoss;
import org.skills.services.mobs.ServiceEliteMobs;
import org.skills.services.mobs.ServiceLorinthsRpgMobs;
import org.skills.services.mobs.ServiceMythicMobs;
import org.skills.services.placeholders.ServicePlaceholderAPI;
import org.skills.services.placeholders.SkillsPlaceholders;
import org.skills.utils.Pair;
import org.skills.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ServiceHandler {
    private static final Set<String> PRESENT = new HashSet<>();

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

        if (e1 instanceof Player && e2 instanceof Player) {
            SkilledPlayer info1 = SkilledPlayer.getSkilledPlayer((Player) e1);
            SkilledPlayer info2 = SkilledPlayer.getSkilledPlayer((Player) e2);
            if (info1.isFrendly(info2)) return true;

            if (Bukkit.getPluginManager().getPlugin("Kingdoms") != null) {
                return !ServiceKingdoms.canFight((Player) e1, (Player) e2);
            } else {
                Plugin factions = Bukkit.getPluginManager().getPlugin("Factions");
                if (factions != null) {
                    try {
                        if (Bukkit.getPluginManager().getPlugin("MasiveCore") != null && !factions.getDescription().getWebsite().contains("factionsuuid")) {
                            if (!ServiceMassiveFactions.canFight((Player) e1, (Player) e2)) return true;
                        } else {
                            if (!ServiceFactions.canFight((Player) e1, (Player) e2)) return true;
                        }
                    } catch (Throwable e) {
                        SLogger.error("Factions support has encountered an error!");
                        e.printStackTrace();
                    }
                }
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
                if (XMaterial.isNewVersion()) {
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
        if (isAvailable("MythicMobs")) {
            Pair<String, Number> property = ServiceMythicMobs.getMobProperties(entity);
            if (property != null) return property;
        }
        if (isAvailable("Boss")) {
            Pair<String, Number> property = ServiceBoss.getMobProperties(entity);
            if (property != null) return property;
        }
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
        if (XMaterial.isNewVersion()) {
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
}
