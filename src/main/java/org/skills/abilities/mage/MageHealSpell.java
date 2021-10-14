package org.skills.abilities.mage;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.versionsupport.VersionSupport;

public class MageHealSpell extends Ability {
    public MageHealSpell() {
        super("Mage", "heal_spell");
    }

    @EventHandler
    public void onMageAttack(EntityRegainHealthEvent event) {
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED &&
                event.getRegainReason() != EntityRegainHealthEvent.RegainReason.REGEN) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int percent = VersionSupport.getHealthPercent(player);
        if (percent > getScaling(info, "health-percent")) return;
        event.setAmount(event.getAmount() +
                this.getScaling(info, "regain",
                        "regain", event.getAmount()));
    }
}
