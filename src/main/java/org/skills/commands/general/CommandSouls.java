package org.skills.commands.general;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.services.ServiceVault;
import org.skills.services.manager.ServiceHandler;

import java.util.Locale;

public class CommandSouls extends SkillsCommand {
    public CommandSouls() {
        super("souls", SkillsLang.COMMAND_SOULS_DESCRIPTION, "soulbank");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!ServiceHandler.isAvailable("Vault")) {
            SkillsLang.COMMAND_SOULS_SERVICE_UNAVAILABLE.sendMessage(sender);
            return;
        }
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendConsoleMessage();
            return;
        }
        if (args.length < 2) {
            SkillsCommandHandler.sendUsage(sender, "<deposit/withdraw> <amount>");
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            SkillsLang.Command_Not_Number.sendMessage(sender, "%arg%", args[1], "%needed%", "souls");
            return;
        }
        if (amount <= 0) {
            SkillsLang.COMMAND_AT_LEAST_ONE.sendMessage(sender, "%arg%", args[1], "%needed%", "souls");
            return;
        }

        Player player = (Player) sender;
        String type = args[0].toLowerCase(Locale.ENGLISH);
        if (type.equalsIgnoreCase("deposit")) {
            if (!SkillsConfig.ECONOMY_DEPOSIT_ENABLED.getBoolean()) {
                SkillsLang.COMMAND_SOULS_DEPOSIT_DISABLED.sendMessage(player);
                return;
            }
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
            if (info.getSouls() < amount) {
                SkillsLang.COMMAND_SOULS_NOT_ENOUGH_SOULS.sendMessage(player, "%amount%", amount);
                return;
            }

            info.addSouls(-amount);
            double worth = amount * SkillsConfig.ECONOMY_DEPOSIT_SOUL_WORTH.getDouble();
            ServiceVault.deposit(player, worth);
            SkillsLang.COMMAND_SOULS_DEPOSIT_SUCCESS.sendMessage(player, "%amount%", amount, "%translated%", worth, "%balance%", ServiceVault.getMoney(player));
        } else if (type.equalsIgnoreCase("withdraw")) {
            if (!SkillsConfig.ECONOMY_WITHDRAW_ENABLED.getBoolean()) {
                SkillsLang.COMMAND_SOULS_WITHDRAW_DISABLED.sendMessage(player);
                return;
            }

            double worth = amount * SkillsConfig.ECONOMY_WITHDRAW_SOUL_WORTH.getDouble();
            if (!ServiceVault.hasMoney(player, worth)) {
                SkillsLang.COMMAND_SOULS_NOT_ENOUGH_MONEY.sendMessage(player, "%amount%", amount);
                return;
            }
            SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);

            info.addSouls(amount);
            ServiceVault.withdraw(player, worth);
            SkillsLang.COMMAND_SOULS_WITHDRAW_SUCCESS.sendMessage(player, "%amount%", amount, "%translated%", worth, "%balance%", ServiceVault.getMoney(player));
        } else {
            SkillsLang.COMMAND_SOULS_UNKNOWN_TRANSACTION.sendMessage(player, "%transaction%", args[0]);
        }
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) return new String[]{"deposit", "withdraw"};
            if (args.length == 2) return new String[]{"<amount>"};
        }
        return new String[0];
    }
}
