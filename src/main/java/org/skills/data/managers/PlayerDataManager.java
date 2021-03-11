package org.skills.data.managers;

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.main.locale.MessageHandler;
import org.skills.masteries.managers.Mastery;
import org.skills.masteries.managers.MasteryManager;
import org.skills.utils.FastUUID;
import org.skills.utils.MathUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerDataManager extends DataManager<SkilledPlayer> {
    private static final Map<UUID, Integer> LEVELS = new ConcurrentHashMap<>();

    public PlayerDataManager(SkillsPro plugin) {
        super(DataHandlers.getDatabase(plugin, "players", SkilledPlayer.class));
        autoSave(plugin);
    }

    public static void addLevel(OfflinePlayer player, int level) {
        LEVELS.put(player.getUniqueId(), level);
    }

    public static CompletableFuture<List<UUID>> getTopLevels(int size) {
        int listSize = LEVELS.size();
        if (listSize <= size) size = listSize;
        int finaleSize = size;

        return CompletableFuture.supplyAsync(() ->
                LEVELS.entrySet().stream().sorted(Map.Entry.comparingByValue()).skip(listSize - finaleSize)
                        .map(Map.Entry::getKey).collect(Collectors.toList()));
    }

    @Override
    public void saveAll() {
        int saved = 0;
        for (SkilledPlayer entry : cache.asMap().values()) {
            if (entry.shouldSave()) {
                save(entry);
                saved++;
            }
        }
        if (SkillsConfig.ANNOUNCE_AUTOSAVES.getBoolean()) MessageHandler.sendConsolePluginMessage("Saved a total of &e" + saved + " &3players data.");
    }

    private void generateCrap() {
        for (int i = 0; i < 1000; i++) {
            SkilledPlayer info = new SkilledPlayer(UUID.randomUUID());
            info.setXP(MathUtils.randInt(0, 10));
            info.setLevel(MathUtils.randInt(0, 100));
            info.setSouls(MathUtils.randInt(0, 1000));

            for (int j = 0; j < MathUtils.randInt(1, MasteryManager.getMasteries().size()); j++) {
                Mastery m = MasteryManager.getMasteries().get(MathUtils.randInt(0, MasteryManager.getMasteries().size() - 1));
                info.setMasteryLevel(m, MathUtils.randInt(0, 45));
            }

//            for (int j = 0; j < MathUtils.randInt(1, AbilityManager.getAllAbilities().size()); j++) {
//                Ability ab = AbilityManager.getAllAbilities().get(MathUtils.randInt(0, AbilityManager.getAllAbilities().size() - 1));
//                info.setImprovement(ab, MathUtils.randInt(0, 30));
//            }
        }
//
//        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
//            SLogger.error("generating crap");
//            long startTime = System.currentTimeMillis();
//            generateCrap();
//            saveAll();
//            long finishTime = System.currentTimeMillis();
//
//            SLogger.error("generating crap took: " + (finishTime - startTime) + "ms");
//        });
    }

    public void setTopLevels(SkillsPro plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean cache = SkillsConfig.LOAD_ALL_DATA_ON_STARTUP.getBoolean();
                int loaded = 0;

                for (String key : database.getAllKeys()) {
                    String idKey = key.substring(0, key.length() - 5);
                    UUID id = FastUUID.fromString(idKey);
                    SkilledPlayer info = cache ? getData(id) : peek(id);

                    if (info != null) LEVELS.put(id, info.getLevel());
                    else MessageHandler.sendConsolePluginMessage("&cCould not load data for &e" + id);
                    loaded++;
                }
                MessageHandler.sendConsolePluginMessage("&3Loaded &e" + loaded + " &3player data.");
            }
        }.runTaskAsynchronously(plugin);
    }
}