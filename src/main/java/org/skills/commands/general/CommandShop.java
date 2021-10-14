package org.skills.commands.general;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.TabCompleteManager;
import org.skills.data.managers.SkilledPlayer;
import org.skills.gui.GUIParser;
import org.skills.gui.InteractiveGUI;
import org.skills.main.locale.MessageHandler;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.SkillItemManager;
import org.skills.services.manager.ServiceHandler;
import org.skills.utils.MathUtils;
import org.skills.utils.nbt.ItemNBT;

public class CommandShop extends SkillsCommand {
    public CommandShop() {
        super("shop", SkillsLang.COMMAND_SHOP_DESCRIPTION, "magic", "magicshop");
    }

    @Override
    public void runCommand(@NonNull CommandSender sender, @NonNull String[] args) {
        if (!(sender instanceof Player)) {
            SkillsLang.PLAYERS_ONLY.sendMessage(sender);
            return;
        }
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("give")) {
            if (player.hasPermission("skills.command.shop.give")) {
                SkillsLang.COMMAND_SHOP_GIVE_PERMISSION.sendMessage(player);
                return;
            }

            if (args.length < 2) {
                SkillsLang.COMMAND_SHOP_GIVE_USAGE.sendMessage(player);
                return;
            }

            Player target = null;
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    SkillsLang.COMMAND_SHOP_GIVE_USAGE.sendMessage(player);
                    return;
                }
            }

            if (target == null && args.length > 3) {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    SkillsLang.PLAYER_NOT_FOUND.sendMessage(player);
                    return;
                }
            } else target = player;

            String type = args[1];
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("skills-items." + type);
            if (section == null) {
                SkillsLang.COMMAND_SHOP_GIVE_INVALID_TYPE.sendMessage(player, "%type%", args[1]);
                return;
            }

            ItemStack item = XItemStack.deserialize(section);
            item = ItemNBT.addSimpleTag(item, SkillItemManager.SKILL_ITEM, section.getString("skills-item-type"));
            if (target.getInventory().firstEmpty() > -1) target.getInventory().addItem(item);
            else target.getWorld().dropItemNaturally(target.getLocation(), item);
            return;
        }

        SkilledPlayer info = SkilledPlayer.getSkilledPlayer(player);
        InteractiveGUI gui = GUIParser.parseOption(player, "shop");

        for (String sell : gui.getHolders()) {
            if (!sell.startsWith("sell")) continue;
            gui.push(sell, () -> {
                ConfigurationSection guiSection = gui.getOptionsSection().getConfigurationSection(sell);
                String type = guiSection.getString("skills-item");
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("skills-items." + type);
                if (section == null) {
                    MessageHandler.sendConsolePluginMessage("&4Unable to find skills item specified in &e" + sell + " &4option with type&8: &e" + type);
                    return;
                }

                String costScaling = section.getString("cost");
                int cost = (int) MathUtils.evaluateEquation(ServiceHandler.translatePlaceholders(player, costScaling));

                if (info.getSouls() < cost) {
                    SkillsLang.SKILLS_ITEM_COST.sendMessage(player, "%cost%", cost);
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    return;
                }

                Inventory inv = player.getInventory();
                int free = inv.firstEmpty();
                if (free < 0) {
                    SkillsLang.SKILLS_ITEM_COST.sendMessage(player, "%cost%", cost);
                    XSound.BLOCK_NOTE_BLOCK_BASS.play(player);
                    return;
                }

                info.addSouls(-cost);

                ItemStack item = XItemStack.deserialize(section);
                item = ItemNBT.addSimpleTag(item, SkillItemManager.SKILL_ITEM, section.getString("skills-item-type"));
                inv.addItem(item);
            });
        }

        gui.setRest();
        gui.openInventory(player);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
        if (sender.hasPermission("skills.command.shop.give")) {
            if (args.length == 1) return new String[]{"give"};
            if (args.length == 2) return TabCompleteManager.getSuggestions(
                    plugin.getConfig().getConfigurationSection("skills-items").getKeys(false).toArray(new String[0]), args[1]);
            if (args.length == 3) return null;
            if (args.length == 4) return new String[]{"<amount>"};
        }
        return new String[0];
    }
}
