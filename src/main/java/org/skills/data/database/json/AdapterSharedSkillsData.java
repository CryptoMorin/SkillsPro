package org.skills.data.database.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.skills.data.managers.PlayerSkill;
import org.skills.types.Stat;

import java.util.Map;

public class AdapterSharedSkillsData {
    public static void deserialize(JsonElement jsonElement, PlayerSkill data, boolean shared) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        if (PlayerSkill.SHARED_LEVELS == shared) {
            JsonElement lvl = json.get("level");
            if (lvl != null) {
                data.setLevel(lvl.getAsInt());
                data.setAbsoluteXP(json.get("xp").getAsDouble());
            }
        }

        if (PlayerSkill.SHARED_SOULS == shared) {
            JsonElement souls = json.get("souls");
            if (souls != null) data.setSouls(souls.getAsLong());
        }

        if (PlayerSkill.SHARED_STATS == shared) {
            JsonObject stats = json.get("stats").getAsJsonObject();
            for (Map.Entry<String, JsonElement> stat : stats.entrySet()) {
                data.setStat(stat.getKey(), stat.getValue().getAsInt());
            }
        }
    }

    public static JsonElement serialize(JsonObject json, PlayerSkill info, boolean shared) {
        if (PlayerSkill.SHARED_LEVELS == shared) {
            json.addProperty("level", info.getLevel());
            json.addProperty("xp", info.getXP());
        }
        if (PlayerSkill.SHARED_SOULS == shared) json.addProperty("souls", info.getSouls());
        if (PlayerSkill.SHARED_STATS == shared) {
            JsonObject stats = new JsonObject();
            for (Stat stat : Stat.STATS.values()) {
                int statLvl = info.getStat(stat);
                if (statLvl != 0) stats.addProperty(stat.getDataNode(), statLvl);
            }
            json.add("stats", stats);
        }

        return json;
    }
}