package org.skills.commands.friends;

import com.cryptomorin.xseries.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIOption;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandFriends extends SkillsCommand {
    public CommandFriends() {
        super("friends", SkillsLang.COMMAND_FRIENDS_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        InteractiveGUI gui = GUIParser.parseOption(player, "friends");
        List<Integer> slots = gui.getHolder("friend").getSlots();

        for (OfflinePlayer friend : info.getPlayerFriends()) {
            GUIOption holder = gui.getHolder("friend");
            ItemStack item = holder.getItem();
            ItemMeta meta = item.getItemMeta();
            SkullUtils.applySkin(meta, friend);
            item.setItemMeta(meta);
            GUIOption.defineVariables(item, friend);

            int slot = slots.remove(0);
            gui.push(holder, item, slot, () -> {
                if (!SkillsConfig.FRIENDS_TELEPORT_ENABLED.getBoolean()) return;
                if (!player.hasPermission("skills.command.friendtp")) {
                    SkillsLang.COMMAND_FRIENDS_TELEPORT_PERMISSION.sendMessage(player, "%name%", friend.getName());
                    return;
                }

                if (!friend.isOnline()) {
                    SkillsLang.COMMAND_FRIENDS_TELEPORT_NOT_ONLINE.sendMessage(player, "%name%", friend.getName());
                    return;
                }
                Player tpTo = (Player) friend;

                if (player.hasPermission("skills.command.friendtp.instant")) {
                    player.teleport(tpTo);
                    SkillsLang.COMMAND_FRIENDTP_TELEPORTED.sendMessage(player, "%friend%", tpTo.getName());
                    SkillsLang.COMMAND_FRIENDTP_NOTIFY.sendMessage(tpTo, "%friend%", player.getName());
                    return;
                }

                Set<CommandFriendAccept.Teleportation> reqs = CommandFriendAccept.requests.getOrDefault(friend.getUniqueId(), new HashSet<>());
                CommandFriendAccept.Teleportation sent = reqs.stream().filter(x -> x.from.equals(player.getUniqueId())).findFirst().orElse(null);
                if (sent != null) {
                    SkillsLang.COMMAND_FRIENDTP_ALREADY_REQUESTED.sendMessage(player, "%friend%", friend.getName());
                    return;
                }

                SkillsLang.COMMAND_FRIENDTP_REQUESTED.sendMessage(player, "%friend%", tpTo.getName());
                SkillsLang.COMMAND_FRIENDTP_NOTIFY_REQUEST.sendMessage(tpTo, "%friend%", player.getName());

                int time = SkillsConfig.FRIENDS_TELEPORT_REQUEST_TIME.getInt();
                int id = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Set<CommandFriendAccept.Teleportation> sentReqsNew = CommandFriendAccept.requests.getOrDefault(tpTo.getUniqueId(), new HashSet<>());
                    CommandFriendAccept.Teleportation request = sentReqsNew.stream().filter(x -> x.from.equals(player.getUniqueId())).findFirst().orElse(null);
                    if (request != null) {
                        if (player.isOnline()) SkillsLang.COMMAND_FRIENDTP_EXPIRED.sendMessage(player, "%friend%", tpTo.getName());
                        if (tpTo.isOnline()) SkillsLang.COMMAND_FRIENDTP_EXPIRED_NOTIFY.sendMessage(tpTo, "%friend%", player.getName());
                        sentReqsNew.remove(request);
                        if (sentReqsNew.isEmpty()) CommandFriendAccept.requests.remove(tpTo.getUniqueId());
                        else CommandFriendAccept.requests.put(tpTo.getUniqueId(), sentReqsNew);
                    }
                }, time * 20).getTaskId();

                reqs.add(new CommandFriendAccept.Teleportation(player.getUniqueId(), id));
                CommandFriendAccept.requests.put(player.getUniqueId(), reqs);
            }, "%name%", friend.getName(), "%online%", MessageHandler.colorize(friend.isOnline() ? "&2Online" : "&cOffline"));
        }

        gui.dispose("friend");
        gui.setRest();
        gui.openInventory(player);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
