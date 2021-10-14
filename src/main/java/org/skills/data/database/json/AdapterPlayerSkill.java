package org.skills.data.database.json;

import com.google.gson.*;
import org.skills.abilities.KeyBinding;
import org.skills.data.managers.PlayerAbilityData;
import org.skills.data.managers.PlayerSkill;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AdapterPlayerSkill implements JsonSerializer<PlayerSkill>, JsonDeserializer<PlayerSkill> {
    @Override
    public PlayerSkill deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String name = json.get("skill").getAsString();
        PlayerSkill skill = new PlayerSkill(name);

        AdapterSharedSkillsData.deserialize(jsonElement, skill, false);
        skill.setShowReadyMessage(json.get("showReadyMessage").getAsBoolean());

        JsonObject abilitiesJson = json.get("abilities").getAsJsonObject();
        Map<String, PlayerAbilityData> abilities = new HashMap<>(abilitiesJson.size());

        for (Map.Entry<String, JsonElement> abEntry : abilitiesJson.entrySet()) {
            PlayerAbilityData data = new PlayerAbilityData();
            if (abEntry.getValue().isJsonObject()) {
                JsonObject abElement = abEntry.getValue().getAsJsonObject();

                JsonElement lvl = abElement.get("level");
                if (lvl != null) data.setLevel(lvl.getAsInt());

                JsonElement disabled = abElement.get("disabled");
                if (disabled != null) data.setDisabled(disabled.getAsBoolean());

                JsonElement binding = abElement.get("key-binding");
                if (binding != null) data.setKeyBinding(binding.getAsString());
            } else {
                data.setLevel(abEntry.getValue().getAsInt());
            }

            abilities.put(abEntry.getKey(), data);
        }

        skill.setAbilities(abilities);

        return skill;
    }

    @Override
    public JsonElement serialize(PlayerSkill info, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("skill", info.getSkillName());
        AdapterSharedSkillsData.serialize(json, info, false);
        json.addProperty("showReadyMessage", info.showReadyMessage());

        JsonObject abilitiesJson = new JsonObject();
        Map<String, PlayerAbilityData> abilities = info.getAbilities();

        for (Map.Entry<String, PlayerAbilityData> ability : abilities.entrySet()) {
            JsonObject abilityJson = new JsonObject();
            PlayerAbilityData data = ability.getValue();

            boolean changed = false;
            if (data.isDisabled()) {
                abilityJson.addProperty("disabled", true);
                changed = true;
            }
            if (data.getKeyBinding() != null) {
                abilityJson.addProperty("key-binding", KeyBinding.toString(data.getKeyBinding()));
                changed = true;
            }
            if (data.getLevel() != 0) {
                abilityJson.addProperty("level", data.getLevel());
                changed = true;
            }

            if (changed) abilitiesJson.add(ability.getKey(), abilityJson);
        }
        json.add("abilities", abilitiesJson);

        return json;
    }
}