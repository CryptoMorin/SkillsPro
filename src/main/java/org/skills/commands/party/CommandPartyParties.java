package org.skills.commands.party;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.gui.GUIOption;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.main.locale.SkillsLang;
import org.skills.party.SkillsParty;

import java.util.List;

public class CommandPartyParties extends SkillsCommand {
    public CommandPartyParties(SkillsCommand group) {
        super("parties", group, SkillsLang.COMMAND_PARTY_LIST_DESCRIPTION);
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }

        Player player = (Player) sender;
        InteractiveGUI gui = GUIParser.parseOption(player, "parties");
        if (gui == null) SLogger.error("Could not find parties GUI.");
        GUIOption option = gui.getHolder("party");
        List<Integer> slots = option.getSlots();

        for (SkillsParty party : SkillsPro.get().getPartyManager().getAllData()) {
            GUIOption holder = option.clone();
            ItemStack item = XSkull.of(holder.getItem()).profile(Profileable.of(party.getLeader())).apply();
            OfflinePlayer leader = Bukkit.getOfflinePlayer(party.getLeader());
            GUIOption.defineVariables(item, leader);

            gui.push(holder, item, slots.remove(0), () -> {
            }, "%party%", party.getName(), "%leader%", leader.getName());
        }

        gui.dispose("party");
        gui.setRest();
        gui.openInventory(player);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        return new String[0];
    }
}
