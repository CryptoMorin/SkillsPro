package org.skills.abilities.vampire;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.types.SkillScaling;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

public class VampireBloodLust extends Ability {
    public VampireBloodLust() {
        super("Vampire", "blood_lust");
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireHeal(EntityRegainHealthEvent event) {
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        double blood = info.getEnergy();
        double maxBlood = info.getScaling(SkillScaling.MAX_ENERGY);
        int percent = (int) MathUtils.getPercent(blood, maxBlood);
        if (percent < getScaling(info, "energy")) return;
        event.setAmount(event.getAmount() + getScaling(info, "regain"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        double blood = info.getEnergy();
        double maxBlood = info.getScaling(SkillScaling.MAX_ENERGY);
        int percent = (int) MathUtils.getPercent(blood, maxBlood);
        if (percent < getScaling(info, "energy", event)) return;
        event.setDamage(event.getDamage() + this.getScaling(info, "damage", event));

        if (info.getAbilityLevel(this) > 2) {
            if (MathUtils.hasChance((int) getScaling(info, "chance", event))) {
                VersionSupport.heal(player, getScaling(info, "heal", event));
                ParticleDisplay.colored(player.getLocation(), 20, 255, 0, 1).spawn();
            }
        }
    }
}
