package org.skills.abilities.priest;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class PriestMindPossession extends Ability {
    public PriestMindPossession() {
        super("Priest", "mind_possession");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageParalyze(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        if (MathUtils.hasChance((int) this.getScaling(info, "chance", event))) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        List<PotionEffect> effects = new ArrayList<>(3);
        int lvl = info.getAbilityLevel(this);

        effects.add(XPotion.BLINDNESS.buildPotionEffect((lvl * 5) * 20, 1));
        if (lvl > 1) {
            effects.add(XPotion.SLOWNESS.buildPotionEffect((lvl * 5) * 20, 1));
            ParticleDisplay.of(XParticle.CLOUD)
                    .withCount(30).offset(0.3)
                    .withLocation(victim.getEyeLocation())
                    .spawn();
        }

        victim.addPotionEffects(effects);
    }
}
