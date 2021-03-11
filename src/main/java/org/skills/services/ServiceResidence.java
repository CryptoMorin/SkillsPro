package org.skills.services;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Entity;

public class ServiceResidence {
    public static boolean canFight(Entity e1, Entity e2) {
        ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(e1.getLocation());
        if (res != null && res.getPermissions().has(Flags.pvp, false)) return false;

        res = ResidenceApi.getResidenceManager().getByLoc(e2.getLocation());
        if (res != null) return !res.getPermissions().has(Flags.pvp, false);
        return true;
    }
}
