package org.skills.data.managers.backup;

import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class SkillsBackup extends BackupManager {
    private static SkillsBackup instance;

    public SkillsBackup(SkillsPro plugin) {
        super(new File(plugin.getDataFolder(), "backups"), plugin.getDataFolder());
        instance = this;
        useMultiBackups = SkillsConfig.BACKUPS_IGNORE_TODAYS_BACKUP.getBoolean();
        autoBackup(plugin);
    }

    public static SkillsBackup getInstance() {
        return instance;
    }

    public void autoBackup(SkillsPro plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                takeBackup();
            }
        }.runTaskTimerAsynchronously(plugin, 0L,
                20L * 60 * 60 * 24 // second * 60 = 1 minute * 60 = 1 hour * 24 = 1 day
                        + 60L); // delay a few seconds to make sure it's the right time.
    }

    @Override
    public void takeBackup() {
        MessageHandler.sendConsolePluginMessage("&2Taking a backup...");
        if (!useMultiBackups && hasBackupToday()) {
            MessageHandler.sendConsolePluginMessage("&2You already have a backup for today!");
            return;
        }
        deleteOldBackups(SkillsConfig.BACKUPS_DELETE_BACKUPS_OLDER_THAN.getInt(), TimeUnit.DAYS);
        zipFiles().thenAccept(result -> MessageHandler.sendConsolePluginMessage("&2Backed up &6" + result + " &2files."));
    }

    @Override
    public boolean isWhitelistedDirectory(@NonNull Path file) {
        String name = file.getFileName().toString();
        return SkillsConfig.BACKUPS_ENABLED.getBoolean() && name.equals("players");
    }

    @Override
    public boolean isWhitelistedFile(@NonNull Path file) {
        String name = file.getFileName().toString();
        return (StringUtils.countMatches(name, "-") == 4 && name.replace(".json", "").length() == 36 && name.toLowerCase().endsWith(".json"))
                || name.toLowerCase().endsWith(".yml");
    }
}
