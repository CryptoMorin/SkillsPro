package org.skills.abilities.eidolon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;

public class EidolonPurify extends Ability {
    public EidolonPurify() {
        super("Eidolon", "purify");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;


        Player p = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        double damage = this.getScaling(info, event) * getExtraScaling(info, "hp", event);
        double max = getExtraScaling(info, "max", event);
        if (damage > max) damage = max;

        event.setDamage(event.getDamage() + damage);
        if (p.isOnline()) info.setEnergy(info.getEnergy() + (event.getDamage() / getExtraScaling(info, "energy-per-damage", event)));
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%max%", getScalingDescription(info, getExtra(info, "max").getString())};
    }
}
