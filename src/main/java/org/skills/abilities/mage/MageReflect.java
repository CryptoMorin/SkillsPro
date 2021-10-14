package org.skills.abilities.mage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.managers.DamageManager;
import org.skills.utils.MathUtils;

public class MageReflect extends Ability {
    public MageReflect() {
        super("Mage", "reflect");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMageDefend(EntityDamageByEntityEvent event) {
        if (commonDamageCheckupReverse(event)) return;
        LivingEntity damager = DamageManager.getDamager(event);
        if (damager == null) return;

        Player player = (Player) event.getEntity();
        if (!MagePassive.isHoe(player.getItemInHand())) return;

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (MathUtils.hasChance((int) getScaling(info, "chance", event))) {
            event.setCancelled(true);
            double damage = this.getScaling(info, "damage", event);
            DamageManager.damage(damager, player, damage);
        }
    }
}
