package org.skills.commands.party;

import com.cryptomorin.xseries.SkullUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIOption;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.party.PartyRank;

import java.util.List;

public class CommandPartyList extends SkillsCommand {
    public CommandPartyList(SkillsCommand group) {
        super("list", group, SkillsLang.COMMAND_PARTY_LIST_DESCRIPTION, "members");
    }

    public static void openMember(Player player, OfflinePlayer member) {
        SkilledPlayer memberInfo = SkilledPlayer.getSkilledPlayer(member);
        InteractiveGUI memberGUI = GUIParser.parseOption(player, player, "party-member",
                "%name%", member.getName(),
                "%online%", MessageHandler.colorize(member.isOnline() ? "&2Online" : "&cOffline"),
                "%mod%", memberInfo.getRank() == PartyRank.MODERATOR
        );
        if (memberGUI == null) return;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

        memberGUI.push("mod", () -> {
            if (info.getRank() != PartyRank.LEADER) {
                SkillsLang.COMMAND_PARTY_MOD_PERMISSION.sendMessage(player);
                return;
            }

            if (memberInfo.getRank() == PartyRank.MEMBER) {
                memberInfo.setRank(PartyRank.MODERATOR);
                for (Player members : info.getParty().getOnlineMembers()) {
                    SkillsLang.COMMAND_PARTY_MOD_PROMOTED.sendMessage(members, "%mod%", player.getName());
                }
            } else {
                memberInfo.setRank(PartyRank.MEMBER);
                for (Player members : info.getParty().getOnlineMembers()) {
                    SkillsLang.COMMAND_PARTY_MOD_DEMOTED.sendMessage(members, "%mod%", player.getName());
                }
            }
        }).push("kick", () -> {
            if (info.getRank() == PartyRank.MEMBER) {
                SkillsLang.COMMAND_PARTY_KICK_PERMISSION.sendMessage(player);
                return;
            }

            if (player.getUniqueId().equals(member.getUniqueId())) {
                SkillsLang.COMMAND_PARTY_KICK_SELF.sendMessage(player);
                return;
            }

            for (OfflinePlayer members : info.getParty().getOnlineMembers()) {
                if (members.isOnline())
                    SkillsLang.COMMAND_PARTY_KICK_KICKED.sendMessage((Player) members, player, "%kicked%", player.getName(), "%kicker%", player.getName());
            }
            info.leaveParty();
        });
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        if (!info.hasParty()) {
            SkillsLang.NO_PARTY.sendMessage(player);
            return;
        }

        InteractiveGUI gui = GUIParser.parseOption(player, "party");
        List<Integer> slots = gui.getHolder("member").getSlots();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (OfflinePlayer member : info.getParty().getPlayerMembers()) {
                    GUIOption holder = gui.getHolder("member");
                    ItemStack item = holder.getItem();
                    ItemMeta meta = item.getItemMeta();
                    SkullUtils.applySkin(meta, member);
                    item.setItemMeta(meta);
                    GUIOption.defineVariables(item, member);

                    int slot = slots.remove(0);
                    gui.push(holder, item, slot, () -> openMember(player, member),
                            "%name%", member.getName(),
                            "%online%", MessageHandler.colorize(member.isOnline() ? "&2Online" : "&cOffline"));
                }

                gui.dispose("member");
                gui.setRest();
                gui.openInventory(player);
            }
        }.runTaskAsynchronously(SkillsPro.get());
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
