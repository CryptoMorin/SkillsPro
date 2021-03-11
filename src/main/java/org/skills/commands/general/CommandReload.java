package org.skills.commands.general;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.skills.commands.SkillsCommand;
import org.skills.commands.SkillsCommandHandler;
import org.skills.data.managers.CosmeticCategory;
import org.skills.gui.GUIConfig;
import org.skills.main.FileManager;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsMasteryConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.HealthAndEnergyManager;
import org.skills.masteries.managers.MasteryManager;
import org.skills.types.Energy;
import org.skills.types.SkillManager;
import org.skills.types.Stat;
import org.skills.utils.Hologram;

public class CommandReload extends SkillsCommand {
    public CommandReload() {
        super("reload", SkillsLang.COMMAND_RELOAD_DESCRIPTION, false);
    }

    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        FileManager.created = false;
        FileManager.isNew = false;
        FileManager manager = new FileManager(plugin);

        manager.createDataFolder();
        manager.loadConfig();
        plugin.reload();
        if (args.length > 0 && args[0].equalsIgnoreCase("config")) {
            SkillsLang.Command_Reload_Done.sendMessage(sender);
            return;
        }

        CosmeticCategory.load(plugin);
        new SkillsCommandHandler(plugin);
        Hologram.load();
        Stat.init(plugin);
        Energy.init(plugin);

        plugin.getLevelManager().load();
        SkillManager.init(plugin);
        new GUIConfig(plugin);

        MasteryManager.getMasteries().forEach(HandlerList::unregisterAll);
        if (SkillsMasteryConfig.MASTERIES_ENABLED.getBoolean()) new MasteryManager();

        for (Player players : Bukkit.getOnlinePlayers()) {
            if (!SkillsConfig.isInDisabledWorld(players.getLocation()))
                HealthAndEnergyManager.updateStats(players);
        }

        plugin.getPlayerDataManager().saveAll();
        SkillsLang.Command_Reload_Done.sendMessage(sender);
    }

    @Override
    public String[] tabComplete(@NonNull CommandSender sender, @NotNull String[] args) {
        if (args.length > 1) return new String[0];
        return new String[]{"config"};
    }
}
