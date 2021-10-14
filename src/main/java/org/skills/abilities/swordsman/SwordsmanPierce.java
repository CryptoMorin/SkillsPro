package org.skills.abilities.swordsman;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.utils.MathUtils;

public class SwordsmanPierce extends Ability {
    public SwordsmanPierce() {
        super("Swordsman", "pierce");
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwordsmanAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        if (!SwordsmanPassive.isSword(player)) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (event.getEntity() instanceof Player) {
            String stat = getOptions(info, "stat").getString();
            int attacker = info.getStat(stat);
            SkilledPlayer defenderInfo = SkilledPlayer.getSkilledPlayer((Player) event.getEntity());
            int defdefender = defenderInfo.getStat(stat);
            if (attacker <= defdefender) return;
        } else if (!(event.getEntity() instanceof LivingEntity)) return;

        double extra = MathUtils.percentOfAmount(this.getScaling(info, "damage-percent", event), event.getDamage());
        if (extra == 0) return;
        event.setDamage(event.getDamage() + extra);
    }
}
