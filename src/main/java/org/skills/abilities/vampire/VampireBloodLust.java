package org.skills.abilities.vampire;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
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
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player p = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        double blood = info.getEnergy();
        double maxBlood = info.getScaling(SkillScaling.MAX_ENERGY);
        int percent = (int) MathUtils.getPercent(blood, maxBlood);
        if (percent < getExtraScaling(info, "energy")) return;
        event.setAmount(event.getAmount() + getExtraScaling(info, "regain"));
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player p = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(p);
        if (info == null) return;

        double blood = info.getEnergy();
        double maxBlood = info.getScaling(SkillScaling.MAX_ENERGY);
        int percent = (int) MathUtils.getPercent(blood, maxBlood);
        if (percent < getExtraScaling(info, "energy", event)) return;
        event.setDamage(event.getDamage() + this.getScaling(info, event));

        if (info.getImprovementLevel(this) > 2) {
            if (MathUtils.hasChance((int) getExtraScaling(info, "chance", event))) {
                VersionSupport.heal(p, getExtraScaling(info, "heal", event));
                ParticleDisplay.colored(p.getLocation(), 20, 255, 0, 1).spawn();
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%chance%", translate(info, "chance"), "%energy%", translate(info, "energy"), "%heal%", translate(info, "heal"),
                "%regain%", translate(info, "regain")};
    }
}
