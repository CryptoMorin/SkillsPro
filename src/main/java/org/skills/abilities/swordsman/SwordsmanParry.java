package org.skills.abilities.swordsman;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.managers.DamageManager;
import org.skills.utils.MathUtils;

public class SwordsmanParry extends Ability {
    public SwordsmanParry() {
        super("Swordsman", "parry");
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordsmanDefend(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!SwordsmanPassive.isSword(player)) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (!MathUtils.hasChance((int) getScaling(info, "chance"))) return;

        if (event.getDamager() instanceof Player) {
            String stat = getOptions(info, "stat").getString();
            int strdefender = info.getStat(stat);
            SkilledPlayer attackerInfo = SkilledPlayer.getSkilledPlayer((Player) event.getDamager());
            int strattacker = attackerInfo.getStat(stat);
            if (strdefender <= strattacker) return;

            double extra = MathUtils.percentOfAmount(this.getScaling(info, "damage-percent"), event.getDamage());
            if (extra == 0) return;
            DamageManager.damage((LivingEntity) event.getDamager(), player, extra);
        }
        event.setCancelled(true);
    }
}
