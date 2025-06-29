package org.skills.data.database.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.FileManager;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.main.locale.LanguageManager;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.FastUUID;
import org.skills.utils.JsonUtil;
import org.skills.utils.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class OldSkillsConverter {
    //    private static final String[] MASTERIES = {
//            "logging", "harvesting", "pacifist", "criticalstrikes", "power", "glory", "precision",
//            "exploit", "reap", "mining", "bruteforce", "adriot", "regeneration", "thickskin", "serration"
//    };
    private final File dbFolder;
    private final SkillsPro plugin;
    private final Path conv;

    public OldSkillsConverter(File dbFolder, SkillsPro plugin) {
        this.plugin = plugin;
        this.dbFolder = dbFolder;
        if (!dbFolder.exists()) dbFolder.mkdirs();
        this.conv = dbFolder.getParentFile().toPath().resolve("DONT DELETE ME V4.txt");

        // validateConfigs();
        // convertData();
        // convertDataV2();
        // convertDataV3();
        convertDataV4();
    }

    private void convertDataV4() {
        if (Files.exists(this.conv)) return;
        File[] files = dbFolder.listFiles();
        if (files.length == 0) {
            createConvFile(-1);
            return;
        }
        if (!PlayerSkill.SHARED_LEVELS && !PlayerSkill.SHARED_SOULS && !PlayerSkill.SHARED_STATS) return;
        int converted = 0;

        MessageHandler.sendConsolePluginMessage("&4Converting players data... This may take a few seconds");
        for (File datas : files) {
            if (!datas.isFile()) continue;
            MessageHandler.sendConsolePluginMessage("&cConverting player data for&8: &e" + datas.getName());
            Path path = Paths.get(datas.getPath());

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject json = (JsonObject) JsonUtil.from(reader);
                String skill = json.get("skill").getAsString();
                JsonObject skills = (JsonObject) json.get("skills");

                JsonElement lvl = new JsonPrimitive(0);
                JsonElement xp = new JsonPrimitive(0);
                JsonElement souls = new JsonPrimitive(0);
                JsonElement stats = null;

                for (Object entry : skills.entrySet()) {
                    Map.Entry<String, JsonObject> pair = (Map.Entry<String, JsonObject>) entry;
                    boolean master = pair.getKey().equals(skill);
                    JsonObject data = pair.getValue();

                    if (PlayerSkill.SHARED_LEVELS) {
                        JsonElement level = data.remove("level");
                        JsonElement exp = data.remove("xp");
                        if (master) {
                            lvl = level;
                            xp = exp;
                        }
                    }
                    if (PlayerSkill.SHARED_SOULS) {
                        JsonElement soul = data.remove("souls");
                        if (master) souls = soul;
                    }
                    if (PlayerSkill.SHARED_STATS) {
                        JsonElement sts = data.remove("stats");
                        if (master) stats = sts;
                    }
                }

                if (PlayerSkill.SHARED_LEVELS) {
                    json.add("level", lvl);
                    json.add("xp", xp);
                }
                if (PlayerSkill.SHARED_SOULS) json.add("souls", souls);
                if (PlayerSkill.SHARED_STATS) json.add("stats", stats);

                try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    bw.write(JsonUtil.toString(json));
                    bw.flush();
                }

                String uuid = datas.getName().replace(".json", "");
                SkilledPlayer info = plugin.getPlayerDataManager().database.load(uuid);
                plugin.getPlayerDataManager().save(info);
                converted++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        createConvFile(converted);
        if (converted != 0) {
            MessageHandler.sendConsolePluginMessage(" ");
            MessageHandler.sendConsolePluginMessage("&4A total of &e" + converted + " &4players data have been converted. V2");
            MessageHandler.sendConsolePluginMessage("&4Note that this should only happen once. If this happened again make sure to report this issue.");
            MessageHandler.sendConsolePluginMessage(" ");
        }
    }


    private void convertDataV3() {
        if (Files.exists(this.conv)) return;
        File[] files = dbFolder.listFiles();
        if (files.length == 0) {
            createConvFile(-1);
            return;
        }
        int converted = 0;

        MessageHandler.sendConsolePluginMessage("&4Converting players data... This may take a few seconds");
        for (File datas : files) {
            if (!datas.isFile()) continue;
            MessageHandler.sendConsolePluginMessage("&cConverting player data for&8: &e" + datas.getName());
            Path path = Paths.get(datas.getPath());

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject obj = (JsonObject) JsonUtil.from(reader);

                if (!obj.has("skills")) {
                    Map<String, JsonObject> skills = new HashMap<>();
                    JsonObject skill = new JsonObject();

                    String name = obj.get("skill").getAsString();
                    skill.addProperty("skill", name);
                    skill.add("level", obj.get("level"));
                    skill.add("xp", obj.get("xp"));
                    skill.add("souls", obj.get("souls"));
                    skill.add("showReadyMessage", obj.get("showReadyMessage"));
                    skill.add("abilities", obj.get("improvements"));
                    skill.add("disabledAbilities", obj.get("disabledAbilities"));
                    skill.add("stats", obj.get("stats"));

                    skills.put(name, skill);
                    JsonObject jsonSkills = new JsonObject();
                    skills.forEach(jsonSkills::add);
                    obj.add("skills", jsonSkills);

                    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                        bw.write(JsonUtil.toString(obj));
                        bw.flush();
                    }

                    String uuid = datas.getName().replace(".json", "");
                    SkilledPlayer info = plugin.getPlayerDataManager().database.load(uuid);
                    plugin.getPlayerDataManager().save(info);
                    converted++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        createConvFile(converted);
        if (converted != 0) {
            MessageHandler.sendConsolePluginMessage(" ");
            MessageHandler.sendConsolePluginMessage("&4A total of &e" + converted + " &4players data have been converted. V2");
            MessageHandler.sendConsolePluginMessage("&4Note that this should only happen once. If this happened again make sure to report this issue.");
            MessageHandler.sendConsolePluginMessage(" ");
        }
    }

    private void convertDataV2() {
        if (Files.exists(this.conv)) return;
        File[] files = dbFolder.listFiles();
        if (files.length == 0) {
            createConvFile(-1);
            return;
        }
        int converted = 0;

        MessageHandler.sendConsolePluginMessage("&4Converting players data... This may take a few seconds");
        for (File datas : files) {
            if (!datas.isFile()) continue;
            MessageHandler.sendConsolePluginMessage("&cConverting player data for&8: &e" + datas.getName());
            Path path = Paths.get(datas.getPath());

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JsonObject obj = (JsonObject) JsonUtil.from(reader);

                if (!obj.has("disabledAbilities")) obj.add("disabledAbilities", new JsonArray());
                if (!obj.has("lastSkillChange")) obj.addProperty("lastSkillChange", 0);
                if (!obj.has("friends")) obj.add("friends", new JsonArray());
                if (!obj.has("friendRequests")) obj.add("friendRequests", new JsonArray());
                if (!obj.has("healthScaling")) obj.addProperty("healthScaling", -1);
                if (!obj.has("xp")) {
                    obj.add("xp", obj.get("exp"));
                    obj.remove("exp");
                }
                JsonObject stats = (JsonObject) obj.get("stats");
                if (!stats.has("PTS") && stats.has("points")) stats.add("PTS", stats.get("points"));

                try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                    bw.write(JsonUtil.toString(obj));
                    bw.flush();
                }

                String name = datas.getName();
                UUID uuid = FastUUID.fromString(name.substring(0, name.length() - 5));
                SkilledPlayer info = plugin.getPlayerDataManager().getData(uuid);
                plugin.getPlayerDataManager().save(info);
                converted++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        createConvFile(converted);
        if (converted != 0) {
            MessageHandler.sendConsolePluginMessage(" ");
            MessageHandler.sendConsolePluginMessage("&4A total of &e" + converted + " &4players data have been converted. V2");
            MessageHandler.sendConsolePluginMessage("&4Note that this should only happen once. If this happened again make sure to report this issue.");
            MessageHandler.sendConsolePluginMessage(" ");
        }
    }

    private void createConvFile(int converted) {
        try {
            Files.createFile(this.conv);
            try (BufferedWriter writer = Files.newBufferedWriter(this.conv)) {
                writer.write("This file is used to detect if a newer version of skills is being used.");
                writer.newLine();
                writer.write("This will avoid doing unnecessary checks during server startup and converting user's data for no reason.");
                writer.newLine();
                writer.write("This will probably be removed later.");
                writer.newLine();
                writer.write("Please DO NOT delete the file unless you know what you're doing.");

                writer.newLine();
                writer.newLine();
                if (converted == -1) {
                    writer.write("Status: You were not using an older version.");
                } else {
                    writer.write("Status: Converted a total of " + converted + " users from Skills v12");
                }
                writer.newLine();
                writer.write(StringUtils.getFullTime());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validateConfigs() {
        if (!Files.exists(this.conv)) {
            if (FileManager.isNew) {
                createConvFile(-1);
                return;
            }
        } else return;

        if (!LanguageManager.created || !FileManager.created) {
            MessageHandler.sendConsolePluginMessage("&c-----------------------------------------------------");
            MessageHandler.sendConsolePluginMessage("&cDetected &4&lSkills v12,&c taking a backup and reseting...");
            MessageHandler.sendConsolePluginMessage("&cIf this happened more than one time and you did not delete");
            MessageHandler.sendConsolePluginMessage("&cthe 'DONT DELETE ME' file, make sure to report this.");
            MessageHandler.sendConsolePluginMessage("&c-----------------------------------------------------");
        }

        File config = new File(dbFolder.getParent(), "config.yml");
        if (!FileManager.created) {
            MessageHandler.sendConsolePluginMessage("&cReseting config.yml...");
            config.delete();
            FileManager manager = new FileManager(plugin);
            manager.loadConfig();
        }

        if (!LanguageManager.created) {
            MessageHandler.sendConsolePluginMessage("&cReseting languages folder...");
            plugin.getLang().getFile().delete();
            plugin.getLang().load();
        }
    }

    private void convertData() {
        if (Files.exists(this.conv)) return;
        int converted = 0;
        MessageHandler.sendConsolePluginMessage("&4Converting players data... This may take a few seconds");
        for (File datas : dbFolder.listFiles()) {
            if (!datas.isFile()) continue;
            if (!datas.getName().toLowerCase().endsWith(".json")) {
                MessageHandler.sendConsolePluginMessage("&cConverting player data for&8: &e" + datas.getName());
                File newData = new File(dbFolder, datas.getName() + ".json");
                datas.renameTo(newData);

                Path path = Paths.get(newData.getPath());
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    JsonObject obj = (JsonObject) JsonUtil.from(reader);
                    Object id = obj.get("uuid");
                    if (id == null) {
                        SLogger.error("No ID element found. Skipping...");
                        continue;
                    }

                    obj.remove("id");
                    obj.remove("uuid");

                    String skill = obj.get("skill").getAsString();
                    if (skill.equalsIgnoreCase(PlayerSkill.NONE)) skill = PlayerSkill.NONE;
                    else skill = StringUtils.capitalize(skill);
//                    obj.put("skill", skill);
//                    convertStats(obj);
//
//                    obj.put("bonuses", convertBonuses(obj));
//                    obj.remove("bonusList");
//
//                    obj.put("showReadyMessage", obj.get("showReadyMessages"));
//                    obj.remove("showReadyMessages");

//                    obj.put("masteries", convertMasteries(obj));
//                    obj.remove("masteriesstr");

//                    obj.put("improvements", convertImprovements(obj));
//                    obj.remove("improvementstr");

//                    obj.put("disabledAbilities", new JSONArray());
//                    obj.put("lastSkillChange", 0);

                    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                        bw.write(JsonUtil.toString(obj));
                        bw.flush();
                    }

                    String name = datas.getName();
                    UUID uuid = FastUUID.fromString(name.substring(0, name.length() - 5));
                    SkilledPlayer info = plugin.getPlayerDataManager().getData(uuid);
                    plugin.getPlayerDataManager().save(info);
                    converted++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        createConvFile(converted);
        if (converted != 0) {
            MessageHandler.sendConsolePluginMessage(" ");
            MessageHandler.sendConsolePluginMessage("&4A total of &e" + converted + " &4players data have been converted.");
            MessageHandler.sendConsolePluginMessage("&4Note that this should only happen once. If this happened again make sure to report this issue.");
            MessageHandler.sendConsolePluginMessage(" ");
        }
    }
//
//    private void convertStats(JSONObject obj) {
//        JSONObject stats = new JSONObject();
//        convertStat(stats, obj, "str");
//        convertStat(stats, obj, "int");
//        convertStat(stats, obj, "dex");
//        convertStat(stats, obj, "def");
//
//        stats.put("points", obj.get("statpoints"));
//        obj.remove("statpoints");
//        obj.put("stats", stats);
//    }
//
//    private void convertStat(JSONObject obj, JSONObject original, String name) {
//        obj.put(name.toUpperCase(), original.get("stat" + name));
//        original.remove("stat" + name);
//    }
//
//    private JSONObject convertBonuses(JSONObject obj) {
//        JSONArray bonuses = (JSONArray) obj.get("bonusList");
//        HashMap<String, JSONObject> convertedBonuses = new HashMap<>();
//
//        for (Object bonus : bonuses) {
//            String str = bonus.toString();
//            String[] split = StringUtils.split(StringUtils.deleteWhitespace(str), ':');
//
//            SkillsEventType type = SkillsEventType.fromString(split[0]);
//            String multiplier = split[3];
//            long time = Long.parseLong(split[2]);
//            long start = Long.parseLong(split[1]);
//
//            JSONObject finalBonus = new JSONObject();
//            finalBonus.put("multiplier", multiplier);
//            finalBonus.put("time", time);
//            finalBonus.put("start", start);
//
//            convertedBonuses.put(type.name(), finalBonus);
//        }
//
//        return new JSONObject(convertedBonuses);
//    }

//    private JSONObject convertMasteries(JSONObject obj) {
//        String masteries = (String) obj.get("masteriesstr");
//        String[] mastery = StringUtils.split(masteries, ',');
//        JSONObject jsonList = new JSONObject();
//
//        for (int i = 0; i < MASTERIES.length; i++) {
//            int lvl = Integer.parseInt(mastery[i]);
//            if (lvl == 0) continue;
//            String mast = MASTERIES[i];
//            jsonList.put(mast, lvl);
//        }
//        return jsonList;
//    }

//    private JSONObject convertImprovements(JSONObject obj) {
//        String improvementstr = (String) obj.get("improvementstr");
//        String[] improvements = StringUtils.split(improvementstr, ',');
//        JSONObject jsonList = new JSONObject();
//        HashMap<String, HashMap<String, Integer>> types = new HashMap<>();
//
//        for (Ability ab : AbilityManager.getAllAbilities()) {
//            int lvl = Integer.parseInt(improvements[ab.saveIndex]);
//            if (lvl == 0) continue;
//            String type = ab.getSkill().getName().toLowerCase();
//
//            if (types.containsKey(type)) {
//                types.get(type).put(ab.getName().toLowerCase(), lvl);
//                continue;
//            }
//            HashMap<String, Integer> abilities = new HashMap<>();
//            abilities.put(ab.getName().toLowerCase(), lvl);
//            types.put(type, abilities);
//        }
//
//        jsonList.putAll(types);
//        return jsonList;
//    }
}
