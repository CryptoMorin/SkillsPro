package org.skills.abilities.eidolon;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.locale.SkillsLang;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EidolonSpiritFire extends Ability {
    private static final Map<UUID, Double> DAMAGE_ABSOPRTION = new HashMap<>();

    public EidolonSpiritFire() {
        super("Eidolon", "spirit_fire");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;
        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (info.getForm() != EidolonForm.DARK) return;

        Double absorbed = DAMAGE_ABSOPRTION.remove(player.getUniqueId());
        if (absorbed == null) return;

        double forEachDmg = getScaling(info, "dark-damage-release", event);
        double damage = absorbed / forEachDmg;

        event.setDamage(event.getDamage() + damage);
        SkillsLang.Skill_Eidolon_Spirit_Expell.sendMessage(player, "%damage%", MathUtils.roundToDigits(damage, 2));
        DAMAGE_ABSOPRTION.remove(player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonDamageAbsorption(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);

        if (info == null) return;
        if (info.getForm() != EidolonForm.LIGHT) return;

        double absorption = this.getScaling(info, "light-damage-absorption-percent");
        double damagePercent = MathUtils.percentOfAmount(absorption, event.getDamage());
        double maxAbsorption = this.getScaling(info, "max-damage-absorption");
        event.setDamage(event.getDamage() - damagePercent);

        if (DAMAGE_ABSOPRTION.containsKey(player.getUniqueId())) {
            damagePercent = Math.max(maxAbsorption, DAMAGE_ABSOPRTION.get(player.getUniqueId()) + damagePercent);
        }

        DAMAGE_ABSOPRTION.put(player.getUniqueId(), damagePercent);
    }
}
