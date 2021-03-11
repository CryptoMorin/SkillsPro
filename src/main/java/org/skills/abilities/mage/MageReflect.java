package org.skills.abilities.mage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.managers.LastHitManager;
import org.skills.utils.MathUtils;

public class MageReflect extends Ability {
    public MageReflect() {
        super("Mage", "reflect");
    }

    @EventHandler(ignoreCancelled = true)
    public void onMageDefend(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getEntity() instanceof Player && event.getDamager() instanceof LivingEntity) {
            Player player = (Player) event.getEntity();
            if (!MagePassive.isHoe(player.getItemInHand())) return;

            SkilledPlayer info = this.checkup(player);
            if (info == null) return;

            if (MathUtils.hasChance((int) getExtraScaling(info, "chance", event))) {
                event.setCancelled(true);
                double damage = this.getScaling(info, event);
                LastHitManager.damage((LivingEntity) event.getDamager(), player, damage);
            }
        }
    }
}
