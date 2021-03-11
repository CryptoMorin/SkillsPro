package org.skills.data.managers;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skills.data.database.DataContainer;
import org.skills.data.database.SkillsDatabase;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.CacheHandler;
import org.skills.utils.FastUUID;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class DataManager<T extends DataContainer> {
    private static final long INTERVAL = SkillsConfig.AUTOSAVE_INTERVAL.getTimeMillis(TimeUnit.MINUTES);
    public SkillsDatabase<T> database;
    protected final LoadingCache<UUID, T> cache = CacheHandler.newBuilder()
            .expireAfterAccess(INTERVAL * 2, TimeUnit.MILLISECONDS)
            .build(new CacheLoader<UUID, T>() {
                @Nullable
                @Override
                public T load(@NonNull UUID identifier) {
                    return database.load(FastUUID.toString(identifier));
                }
            });

    public DataManager(SkillsDatabase<T> database) {
        this.database = database;
    }

    public void delete(@NonNull UUID identifier) {
        unload(identifier);
        database.delete(identifier.toString());
    }

    protected void autoSave(@NonNull SkillsPro plugin) {
        long time = INTERVAL;
        time = TimeUnit.MILLISECONDS.toSeconds(time);
        time *= 20L; // To Ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                saveAll();
            }
        }.runTaskTimerAsynchronously(plugin, time, time);
    }

    public void saveAll() {
        for (T entry : cache.asMap().values()) {
            if (entry.shouldSave()) save(entry);
        }
    }

    public void save(@NonNull T player) {
        database.save(player);
    }

    public void unload(@NonNull UUID key) {
        cache.invalidate(key);
    }

    public void load(T data) {
        cache.put(FastUUID.fromString(data.getKey()), data);
    }

    public @Nullable
    T peek(@NonNull UUID identifier) {
        T data = cache.getIfPresent(identifier);
        if (data != null) return data;
        else database.load(FastUUID.toString(identifier));
        return data;
    }

    public @Nullable
    T getData(@NonNull UUID identifier) {
        return cache.get(identifier);
    }

    public Collection<T> getAllData() {
        @NonNull String[] keys = this.database.getAllKeys();
        Map<UUID, T> datas = new HashMap<>(keys.length);
        datas.putAll(cache.asMap());
        for (String key : keys) {
            UUID id = FastUUID.fromString(key);
            datas.computeIfAbsent(id, (v) -> cache.get(id));
        }
        return datas.values();
    }
}
