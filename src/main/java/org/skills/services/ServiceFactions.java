package org.skills.services;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.entity.Player;

public class ServiceFactions {
    public static boolean canFight(Player player, Player other) {
        FPlayer factionPlayer = FPlayers.getInstance().getByPlayer(player);
        FPlayer factionOther = FPlayers.getInstance().getByPlayer(other);

        if (!factionPlayer.hasFaction()) return true;
        if (!factionOther.hasFaction()) return true;
        return factionPlayer.getRelationTo(factionOther).isNeutral() || factionPlayer.getRelationTo(factionOther).isEnemy();
    }
}
