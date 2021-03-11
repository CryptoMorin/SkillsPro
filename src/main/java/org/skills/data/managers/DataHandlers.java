package org.skills.data.managers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.skills.data.database.DataContainer;
import org.skills.data.database.SkillsDatabase;
import org.skills.data.database.json.JsonDatabase;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.StringUtils;

import java.io.File;

public class DataHandlers implements Listener {
    private final SkillsPro plugin;

    public DataHandlers(SkillsPro plugin) {
        this.plugin = plugin;
    }

    public static <T extends DataContainer> SkillsDatabase<T> getDatabase(SkillsPro plugin, String folder, Class<T> adapter) {
        String db = SkillsConfig.DATABASE.getString().toLowerCase();

        if (db.equals("json")) return new JsonDatabase<>(new File(plugin.getDataFolder(), folder), adapter);

        MessageHandler.sendConsolePluginMessage("&4Invalid database type&8: &e" + db);
        MessageHandler.sendConsolePluginMessage("&4Disabling the plugin...");
        plugin.onDisable();
        return null;
    }

    @EventHandler
    public void onLeaveEvent(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().save(SkilledPlayer.getSkilledPlayer(event.getPlayer()));
        plugin.getPlayerDataManager().unload(event.getPlayer().getUniqueId());
    }
}
