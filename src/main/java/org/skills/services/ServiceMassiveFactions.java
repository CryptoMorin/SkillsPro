package org.skills.services;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.entity.Player;

public class ServiceMassiveFactions {
    public static boolean canFight(Player player, Player other) {
        MPlayer p1 = MPlayer.get(player.getUniqueId());
        MPlayer p2 = MPlayer.get(other.getUniqueId());
        return p1.getFaction().equals(FactionColl.get().getNone()) || p2.getFaction().equals(FactionColl.get().getNone()) || (!p1.getFaction().getName().equals(p2.getFaction().getName()) && p1.getRelationTo(p2) != Rel.ALLY);
    }
}
