package org.skills.data.database.json;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.skills.data.database.DataContainer;
import org.skills.data.database.SkillsDatabase;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsEventType;
import org.skills.main.SLogger;
import org.skills.party.SkillsParty;
import org.skills.utils.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.UUID;

public class JsonDatabase<T extends DataContainer> implements SkillsDatabase<T> {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .registerTypeAdapter(Duration.class, new AdapterDuration())
            .registerTypeAdapter(UUID.class, new AdapterUUID())
            .registerTypeAdapter(SkilledPlayer.class, new AdapterSkilledPlayer())
            .registerTypeAdapter(PlayerSkill.class, new AdapterPlayerSkill())
            .registerTypeAdapter(SkillsParty.class, new AdapterSkillsParty())
            .registerTypeHierarchyAdapter(SkillsEventType.class, new AdapterEventType())
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();
    private final Class<T> adapter;
    public Path dbFolder;
    private String[] keys;

    public JsonDatabase(File dbFolder, Class<T> adapter) {
        this.dbFolder = dbFolder.toPath();
        this.adapter = adapter;

        if (!Files.exists(this.dbFolder)) {
            try {
                Files.createDirectories(this.dbFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean verifyJson(String name) {
        return name.length() == 36 + 5 && StringUtils.countMatches(name, '-') == 4 && name.toLowerCase().endsWith(".json");
    }

    @Override
    public void delete(String key) {
        if (Strings.isNullOrEmpty(key)) return;
        Path path = dbFolder.resolve(key + ".json");
        if (!Files.exists(path)) return;
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(T data) {
        String key = data.getKey() + ".json";
        Path path = dbFolder.resolve(key);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(data, adapter, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T load(String key) {
        if (Strings.isNullOrEmpty(key)) return null;
        String name = key + ".json";
        Path path = dbFolder.resolve(name);
        if (!Files.exists(path)) return null;

        T info;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            info = gson.fromJson(reader, adapter);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot load data file " + path, e);
        }

        if (info == null) {
            try {
                SLogger.error("Corrupted data file '" + path + "': " + Files.readAllLines(path));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        info.setIdentifier(key);
        info.setSaveMeta();
        return info;
    }

    @Override
    public boolean hasData(String key) {
        Path path = dbFolder.resolve(key + ".json");
        return Files.exists(path);
    }

    @Override
    public @NonNull
    String[] getAllKeys() {
        if (keys != null) return keys;
        if (!Files.exists(dbFolder)) {
            try {
                Files.createDirectories(dbFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new String[0];
        }

        try {
            return keys = Files.walk(dbFolder).map(x -> x.getFileName().toString()).filter(JsonDatabase::verifyJson).toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }
}