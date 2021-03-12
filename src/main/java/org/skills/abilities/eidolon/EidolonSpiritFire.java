package org.skills.abilities.eidolon;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EidolonSpiritFire extends Ability {
    private static final Map<UUID, Integer> prepped = new HashMap<>();

    public EidolonSpiritFire() {
        super("Eidolon", "spirit_fire");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (!(event.getEntity() instanceof Damageable)) return;
        if (event.getDamager() instanceof Player) {
            Player p = (Player) event.getDamager();
            SkilledPlayer info = this.checkup(p);
            if (info == null) return;
            if (info.getForm() != EidolonForm.DARK) return;

            Integer prep = prepped.getOrDefault(p.getUniqueId(), null);
            if (prep != null && prep > 0) {
                double level = getExtraScaling(info, "dark-scaling", event);
                double damage = prepped.get(p.getUniqueId()) / level;

                event.setDamage(event.getDamage() + damage);
                SkillsLang.Skill_Eidolon_Spirit_Expell.sendMessage(p, "%damage%", MathUtils.roundToDigits(damage, 2));
                prepped.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonDefend(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            SkilledPlayer info = this.checkup(p);
            if (info == null) return;

            if (info.getForm() == EidolonForm.LIGHT) {
                double reduc = this.getScaling(info);
                if (reduc > 50) reduc = 50;

                int toStore = (int) (event.getDamage() * (reduc / 100f)) + 1;
                event.setDamage(event.getDamage() * (1 - (reduc / 100f)));
                if (prepped.containsKey(p.getUniqueId())) {
                    prepped.put(p.getUniqueId(), prepped.get(p.getUniqueId()) + toStore);
                } else {
                    prepped.put(p.getUniqueId(), toStore);
                }
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%dark%", getScalingDescription(info, getExtra(info, "dark-scaling").getString())};
    }
}
