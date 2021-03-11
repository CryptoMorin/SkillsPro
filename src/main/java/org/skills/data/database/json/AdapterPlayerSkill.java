package org.skills.data.database.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skills.data.managers.PlayerSkill;
import org.skills.types.Stat;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdapterPlayerSkill implements JsonSerializer<PlayerSkill>, JsonDeserializer<PlayerSkill> {
    @Override
    public PlayerSkill deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String name = json.get("skill").getAsString();
        PlayerSkill skill = new PlayerSkill(name);

        skill.setLevel(json.get("level").getAsInt());
        skill.setAbsoluteXP(json.get("xp").getAsDouble());
        skill.setSouls(json.get("souls").getAsLong());
        skill.setShowReadyMessage(json.get("showReadyMessage").getAsBoolean());

        skill.setImprovements(context.deserialize(json.get("abilities"), new TypeToken<HashMap<String, HashMap<String, Integer>>>() {}.getType()));

        JsonObject stats = json.get("stats").getAsJsonObject();
        for (Map.Entry<String, JsonElement> stat : stats.entrySet()) {
            skill.setStat(stat.getKey(), stat.getValue().getAsInt());
        }

        Set<String> disabled = context.deserialize(json.get("disabledAbilities"), new TypeToken<Set<String>>() {}.getType());
        if (disabled == null) disabled = new HashSet<>();
        skill.setDisabledAbilities(disabled);

        return skill;
    }

    @Override
    public JsonElement serialize(PlayerSkill info, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("skill", info.getSkillName());
        json.addProperty("level", info.getLevel());
        json.addProperty("xp", info.getXP());
        json.addProperty("souls", info.getSouls());
        json.addProperty("showReadyMessage", info.showReadyMessage());


        json.add("abilities", context.serialize(info.getImprovements(), new TypeToken<HashMap<String, HashMap<String, Integer>>>() {
        }.getType()));

        JsonObject stats = new JsonObject();
        for (Stat stat : Stat.STATS.values()) {
            stats.addProperty(stat.getDataNode(), info.getStat(stat));
        }
        json.add("stats", stats);

        json.add("disabledAbilities", context.serialize(info.getDisabledAbilities(), new TypeToken<Set<String>>() {}.getType()));

        return json;
    }
}