package org.skills.data.database.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.skills.data.managers.Cosmetic;
import org.skills.data.managers.CosmeticCategory;
import org.skills.data.managers.PlayerSkill;
import org.skills.data.managers.SkilledPlayer;
import org.skills.events.SkillsEvent;
import org.skills.events.SkillsEventType;
import org.skills.party.PartyRank;
import org.skills.utils.FastUUID;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AdapterSkilledPlayer implements JsonSerializer<SkilledPlayer>, JsonDeserializer<SkilledPlayer> {
    @Override
    public SkilledPlayer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        SkilledPlayer info = new SkilledPlayer();
        JsonObject json = jsonElement.getAsJsonObject();

        Type mapType = new TypeToken<Map<String, PlayerSkill>>() {
        }.getType();
        info.setSkills(context.deserialize(json.get("skills"), mapType));
        PlayerSkill activeSkill = info.getSkills().get(json.get("skill").getAsString());
        if (activeSkill == null) activeSkill = new PlayerSkill(PlayerSkill.NONE);
        AdapterSharedSkillsData.deserialize(jsonElement, activeSkill, true);
        info.setAbsoluteActiveSkill(activeSkill);

        JsonElement showAbEle = json.get("showActionBar");
        if (showAbEle != null) info.setShowActionBar(showAbEle.getAsBoolean());

        JsonElement lastChange = json.get("lastSkillChange");
        info.setLastSkillChange(lastChange == null ? 0 : lastChange.getAsLong());

        Type uuidType = new TypeToken<Set<UUID>>() {
        }.getType();
        Set<UUID> friends = context.deserialize(json.get("friends"), uuidType);
        Set<UUID> friendRequests = context.deserialize(json.get("friendRequests"), uuidType);
        if (friends != null) info.setFriends(friends);
        if (friendRequests != null) info.setFriendRequests(friendRequests);

        JsonElement element = json.get("healthScaling");
        if (element != null) info.setHealthScaling(element.getAsDouble());

        JsonElement partyEle = json.get("party");
        if (partyEle != null) info.setParty(FastUUID.fromString(partyEle.getAsString()));
        info.setRank(context.deserialize(json.get("rank"), PartyRank.class));

        mapType = new TypeToken<HashMap<String, Integer>>() {
        }.getType();
        info.setMasteries(context.deserialize(json.get("masteries"), mapType));

        mapType = new TypeToken<HashMap<SkillsEventType, SkillsEvent>>() {
        }.getType();
        Map<SkillsEventType, SkillsEvent> bonuses = context.deserialize(json.get("bonuses"), mapType);
        Map<SkillsEventType, SkillsEvent> newBonuses = new EnumMap<>(SkillsEventType.class);

        for (Map.Entry<SkillsEventType, SkillsEvent> entries : bonuses.entrySet()) {
            SkillsEventType eventType = entries.getKey();
            if (eventType == null) eventType = SkillsEventType.XP;
            SkillsEvent entry = entries.getValue();
            SkillsEvent newEntry = new SkillsEvent(entry.getId(), eventType, entry.getMultiplier(), entry.getTime(), TimeUnit.MILLISECONDS, entry.getStart());
            newBonuses.put(eventType, newEntry);
        }
        info.setBonuses(newBonuses);

        JsonElement cosmeticsElement = json.get("cosmetics");
        if (cosmeticsElement != null) {
            mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> cosmetics = context.deserialize(cosmeticsElement, mapType);
            Map<String, Cosmetic> realCosmetics = new HashMap<>();
            for (Map.Entry<String, String> cosmetic : cosmetics.entrySet()) {
                CosmeticCategory cat = CosmeticCategory.get(cosmetic.getKey());
                if (cat != null) realCosmetics.put(cat.getName(), cat.getCosmetic(cosmetic.getValue()));
            }
            info.setCosmetics(realCosmetics);
        }

        return info;
    }

    @Override
    public JsonElement serialize(SkilledPlayer info, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        AdapterSharedSkillsData.serialize(json, info.getActiveSkill(), true);
        json.addProperty("skill", info.getSkillName());
        json.addProperty("showActionBar", info.showActionBar());
        json.addProperty("lastSkillChange", info.getLastSkillChange());

        Type uuidType = new TypeToken<Set<UUID>>() {
        }.getType();
        json.addProperty("healthScaling", info.getHealthScaling());
        json.add("friends", context.serialize(info.getFriends(), uuidType));
        json.add("friendRequests", context.serialize(info.getFriendRequests(), uuidType));
        if (info.hasParty()) json.addProperty("party", FastUUID.toString(info.getPartyId()));
        json.addProperty("rank", (info.getRank() == null ? null : info.getRank().name()));

        JsonObject skills = new JsonObject();
        for (Map.Entry<String, PlayerSkill> skill : info.getSkills().entrySet()) {
            //if (skill.getKey().equals(PlayerSkill.NONE)) continue;
            skills.add(skill.getKey(), context.serialize(skill.getValue()));
        }
        json.add("skills", skills);

        Type mapType = new TypeToken<HashMap<String, Integer>>() {
        }.getType();
        json.add("masteries", context.serialize(info.getMasteries(), mapType));

        mapType = new TypeToken<HashMap<SkillsEventType, SkillsEvent>>() {
        }.getType();
        json.add("bonuses", context.serialize(info.getBonuses(), mapType));

        JsonObject cosmetics = new JsonObject();
        for (Map.Entry<String, Cosmetic> cosmetic : info.getCosmetics().entrySet()) {
            cosmetics.addProperty(cosmetic.getKey(), cosmetic.getValue().getName());
        }
        json.add("cosmetics", cosmetics);

        return json;
    }
}