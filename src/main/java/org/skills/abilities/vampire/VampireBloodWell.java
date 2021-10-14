package org.skills.abilities.vampire;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.skills.abilities.Ability;
import org.skills.api.events.SkillEnergyChangeEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.MathUtils;

public class VampireBloodWell extends Ability {
    public VampireBloodWell() {
        super("Vampire", "blood_well");
    }

    @EventHandler(ignoreCancelled = true)
    public void onVampireEnergyChange(SkillEnergyChangeEvent event) {
        Player player = event.getPlayer();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        double blood = info.getEnergy();
        if (blood < event.getAmount()) return;

        double chance = this.getScaling(info, "chance");
        if (!MathUtils.hasChance((int) chance)) event.setCancelled(true);
    }
}
