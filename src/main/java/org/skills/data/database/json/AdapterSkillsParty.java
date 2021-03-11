package org.skills.data.database.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skills.party.SkillsParty;
import org.skills.utils.FastUUID;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class AdapterSkillsParty implements JsonSerializer<SkillsParty>, JsonDeserializer<SkillsParty> {
    @Override
    public SkillsParty deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String name = json.get("name").getAsString();
        UUID leader = FastUUID.fromString(json.get("leader").getAsString());
        SkillsParty party = new SkillsParty(leader, name, true);
        party.setMembers(context.deserialize(json.get("members"), new TypeToken<List<UUID>>() {
        }.getType()));
        return party;
    }

    @Override
    public JsonElement serialize(SkillsParty party, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("name", party.getName());
        json.add("leader", context.serialize(party.getLeader(), UUID.class));
        json.add("members", context.serialize(party.getMembers(), new TypeToken<List<UUID>>() {
        }.getType()));

        return json;
    }
}