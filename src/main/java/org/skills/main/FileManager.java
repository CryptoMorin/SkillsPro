package org.skills.main;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.skills.gui.GUIConfig;
import org.skills.main.locale.MessageHandler;
import org.skills.managers.LevelManager;
import org.skills.masteries.managers.MasteryManager;
import org.skills.types.Energy;
import org.skills.types.Skill;
import org.skills.types.SkillManager;
import org.skills.types.Stat;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    public static boolean created; // Temporary for the old converter
    public static boolean isNew;
    private final SkillsPro plugin;

    public FileManager(SkillsPro plugin) {
        this.plugin = plugin;
    }

    public void createDataFolder() {
        Path path = plugin.getDataFolder().toPath();
        isNew = !Files.exists(path);

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        Path config = plugin.getDataFolder().toPath().resolve("config.yml");

        if (!Files.exists(config)) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();
            created = true;
        }
        plugin.reloadConfig();

        SkillsMasteryConfig.getAdapter().register();
        SkillsMasteryConfig.getAdapter().reloadConfig();
    }

    public void setupWatchService() {
        Path path = plugin.getDataFolder().toPath();
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Path, WatchService> guiWatchers = new HashMap<>();
        try {
            Files.walkFileTree(path.resolve("guis").resolve(SkillsConfig.LANG.getString()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    guiWatchers.put(dir, watchService);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path skills = path.resolve("Skills");
        WatchService skillsServices = null;
        try {
            skillsServices = FileSystems.getDefault().newWatchService();
            skills.register(skillsServices, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        WatchService finalService = watchService;
        WatchService finalSkillsServices = skillsServices;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            WatchKey key = finalService.poll();
            if (key != null) {
                for (WatchEvent<?> events : key.pollEvents()) {
                    String file = events.context().toString();

                    if (file.equalsIgnoreCase("config.yml")) {
                        MessageHandler.sendConsolePluginMessage("&2Detected changes for config, reloading...");
                        plugin.reloadConfig();
                        Stat.init(plugin);
                        Energy.init(plugin);
                        LevelManager.load(plugin);
                    } else if (file.equalsIgnoreCase("masteries.yml")) {
                        MessageHandler.sendConsolePluginMessage("&2Detected changes for masteries, reloading...");
                        SkillsMasteryConfig.getAdapter().register();
                        SkillsMasteryConfig.getAdapter().reloadConfig();
                        MasteryManager.getMasteries().forEach(HandlerList::unregisterAll);
                        if (SkillsMasteryConfig.MASTERIES_ENABLED.getBoolean()) new MasteryManager();
                    } else if (file.equalsIgnoreCase(SkillsConfig.LANG.getString() + ".yml")) {
                        MessageHandler.sendConsolePluginMessage("&2Detected changes for language file, reloading...");
                        plugin.reload();
                    }
                }
                key.reset();
            }

            for (Map.Entry<Path, WatchService> guis : guiWatchers.entrySet()) {
                key = guis.getValue().poll();
                if (key == null) continue;
                for (WatchEvent<?> events : key.pollEvents()) {
                    String file = events.context().toString();
                    MessageHandler.sendConsolePluginMessage("&2Detected changes for GUI&8: &9" + file);
                    GUIConfig.registerGUI(plugin, file.substring(0, file.length() - 4));
                }

                key.reset();
            }

            key = finalSkillsServices.poll();
            if (key != null) {
                for (WatchEvent<?> events : key.pollEvents()) {
                    Path eventPath = (Path) events.context();
                    String file = eventPath.toString();
                    file = file.substring(0, file.length() - 4);

                    MessageHandler.sendConsolePluginMessage("&2Detected changes for skill&8: &9" + file);
                    Skill skill = SkillManager.getSkill(file);
                    skill.getAdapter().reloadConfig();
                    SkillManager.registerScalings(skill);
                }
                key.reset();
            }
        }, 20L * 10, 20L * 5);
    }
}
