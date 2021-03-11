package org.skills.data.database.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.FileManager;
import org.skills.main.SLogger;
import org.skills.main.SkillsPro;
import org.skills.main.locale.LanguageManager;
import org.skills.main.locale.MessageHandler;
import org.skills.utils.FastUUID;
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
        this.conv = dbFolder.getParentFile().toPath().resolve("DONT DELETE ME V3.txt");

        //validateConfigs();
        //convertData();
//        convertDataV2();
        convertDataV3();
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
            JSONParser jsonParser = new JSONParser();
            Path path = Paths.get(datas.getPath());

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JSONObject obj = (JSONObject) jsonParser.parse(reader);

                if (!obj.containsKey("skills")) {
                    Map<String, JSONObject> skills = new HashMap<>();
                    JSONObject skill = new JSONObject();

                    String name = (String) obj.get("skill");
                    skill.put("skill", name);
                    skill.put("level", obj.get("level"));
                    skill.put("xp", obj.get("xp"));
                    skill.put("souls", obj.get("souls"));
                    skill.put("showReadyMessage", obj.get("showReadyMessage"));
                    skill.put("abilities", obj.get("improvements"));
                    skill.put("disabledAbilities", obj.get("disabledAbilities"));
                    skill.put("stats", obj.get("stats"));

                    skills.put(name, skill);
                    obj.put("skills", skills);

                    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                        bw.write(obj.toJSONString());
                        bw.flush();
                    }

                    String uuid = datas.getName().replace(".json", "");
                    SkilledPlayer info = plugin.getPlayerDataManager().database.load(uuid);
                    plugin.getPlayerDataManager().save(info);
                    converted++;
                }
            } catch (IOException | ParseException e) {
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
            JSONParser jsonParser = new JSONParser();
            Path path = Paths.get(datas.getPath());

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                JSONObject obj = (JSONObject) jsonParser.parse(reader);

                if (!obj.containsKey("disabledAbilities")) obj.put("disabledAbilities", new JSONArray());
                if (!obj.containsKey("lastSkillChange")) obj.put("lastSkillChange", 0);
                if (!obj.containsKey("friends")) obj.put("friends", new JSONArray());
                if (!obj.containsKey("friendRequests")) obj.put("friendRequests", new JSONArray());
                if (!obj.containsKey("healthScaling")) obj.put("healthScaling", -1);
                if (!obj.containsKey("xp")) {
                    obj.put("xp", obj.get("exp"));
                    obj.remove("exp");
                }
                JSONObject stats = (JSONObject) obj.get("stats");
                if (!stats.containsKey("PTS") && stats.containsKey("points")) stats.put("PTS", stats.get("points"));

                try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                    bw.write(obj.toJSONString());
                    bw.flush();
                }

                String name = datas.getName();
                UUID uuid = FastUUID.fromString(name.substring(0, name.length() - 5));
                SkilledPlayer info = plugin.getPlayerDataManager().getData(uuid);
                plugin.getPlayerDataManager().save(info);
                converted++;
            } catch (IOException | ParseException e) {
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

                JSONParser jsonParser = new JSONParser();
                Path path = Paths.get(newData.getPath());
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    JSONObject obj = (JSONObject) jsonParser.parse(reader);
                    Object id = obj.get("uuid");
                    if (id == null) {
                        SLogger.error("No ID element found. Skipping...");
                        continue;
                    }

                    obj.remove("id");
                    obj.remove("uuid");

                    String skill = (String) obj.get("skill");
                    if (skill.equalsIgnoreCase("none")) skill = "none";
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
                        bw.write(obj.toJSONString());
                        bw.flush();
                    }

                    String name = datas.getName();
                    UUID uuid = FastUUID.fromString(name.substring(0, name.length() - 5));
                    SkilledPlayer info = plugin.getPlayerDataManager().getData(uuid);
                    plugin.getPlayerDataManager().save(info);
                    converted++;
                } catch (IOException | ParseException e) {
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
