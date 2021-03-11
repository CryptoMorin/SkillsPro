package org.skills.data.database.json;

import com.google.common.base.Enums;
import com.google.gson.*;
import org.skills.events.SkillsEventType;

import java.lang.reflect.Type;

public class AdapterEventType implements JsonSerializer<SkillsEventType>, JsonDeserializer<SkillsEventType> {
    @Override
    public SkillsEventType deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Enums.getIfPresent(SkillsEventType.class, json.getAsString()).orNull();
    }

    @Override
    public JsonElement serialize(SkillsEventType skillsEventType, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(skillsEventType.name());
    }
}
