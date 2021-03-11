package org.skills.party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.skills.data.managers.DataHandlers;
import org.skills.data.managers.DataManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.services.manager.ServiceHandler;

import java.util.*;

public class PartyManager extends DataManager<SkillsParty> implements Listener {
    public static final Map<UUID, List<UUID>> INVITES = new HashMap<>();
    public static final Set<UUID> CHAT = new HashSet<>();
    public static final Set<UUID> SPY = new HashSet<>();

    public PartyManager(SkillsPro plugin) {
        super(DataHandlers.getDatabase(plugin, "parties", SkillsParty.class));
        autoSave(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(damager);
        if (!SkillsConfig.FRIENDS_FRIENDLY_FIRE.getBoolean() && info.getFriends().contains(victim.getUniqueId())) event.setCancelled(true);
        else if (!SkillsConfig.PARTY_FRIENDLY_FIRE.getBoolean() && (info.hasParty() && info.getParty().getMembers().contains(victim.getUniqueId()))) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void partyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!CHAT.contains(player.getUniqueId())) return;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty()) {
            CHAT.remove(player.getUniqueId());
            return;
        }
        event.setCancelled(true);

        String message = MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, SkillsConfig.PARTY_CHAT_FORMAT.getString()) + event.getMessage());
        String spyMessage = MessageHandler.colorize(ServiceHandler.translatePlaceholders(player, SkillsConfig.PARTY_SPY_FORMAT.getString()) + event.getMessage());
        ServiceHandler.logPartyChat(player, MessageHandler.stripColors(message, false));
        List<UUID> members = info.getParty().getMembers();

        Bukkit.getConsoleSender().sendMessage(message);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (members.contains(players.getUniqueId())) players.sendMessage(message);
            else if (SPY.contains(players.getUniqueId())) players.sendMessage(spyMessage);
        }
    }
}
