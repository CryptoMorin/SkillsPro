package org.skills.services;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.entity.Player;

public class ServiceFactions {
    public static boolean canFight(Player player, Player other) {
        // Old v1 API
        // com.massivecraft.factions.FPlayer
        // FPlayer factionPlayer = FPlayers.getInstance().getByPlayer(player);
        // FPlayer factionOther = FPlayers.getInstance().getByPlayer(other);
        MPlayer factionPlayer = MPlayer.get(player);
        MPlayer factionOther = MPlayer.get(other);

        if (!factionPlayer.hasFaction()) return true;
        if (!factionOther.hasFaction()) return true;

        Rel relation = factionPlayer.getRelationTo(factionOther);

        // Old v1 API
        // return relation.isNeutral() || relation.isEnemy();

        return relation == Rel.NEUTRAL || relation == Rel.ENEMY;
    }
}
