package org.skills.services;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

public class ServiceCitizens {
    public static boolean isNPC(Entity entity) {
        return entity.hasMetadata("NPC");
    }

    public static NPC getNPC(Entity entity) {
        return CitizensAPI.getNPCRegistry().getNPC(entity);
    }

    public static String getNPCName(Entity entity) {
        NPC npc = getNPC(entity);
        return npc == null ? null : npc.getName();
    }
}
