package org.skills.data.database.json;

import com.google.gson.*;
import org.skills.utils.FastUUID;

import java.lang.reflect.Type;
import java.util.UUID;

public class AdapterUUID implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
    @Override
    public JsonElement serialize(UUID obj, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(FastUUID.toString(obj));
    }

    @Override
    public UUID deserialize(JsonElement obj, Type type, JsonDeserializationContext context) {
        return FastUUID.fromString(obj.getAsString());
    }
}
