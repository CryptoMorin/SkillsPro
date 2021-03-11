package org.skills.abilities.priest;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class PriestMindPossession extends Ability {
    public PriestMindPossession() {
        super("Priest", "mind_possession");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageParalyze(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        Player player = (Player) event.getDamager();

        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        int lvl = info.getImprovementLevel(this);

        if (MathUtils.hasChance((int) this.getScaling(info))) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        List<PotionEffect> effects = new ArrayList<>();
        effects.add(new PotionEffect(PotionEffectType.BLINDNESS, (lvl * 5) * 20, 0));
        if (lvl > 1) {
            effects.add(new PotionEffect(PotionEffectType.SLOW, (lvl * 10) * 20, 0));
            player.spawnParticle(Particle.SPELL_MOB_AMBIENT, victim.getEyeLocation(), 30, 0.3, 0, 0.3, 0.3);
        }

        victim.addPotionEffects(effects);
    }
}
