package org.skills.abilities.vampire;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

public class VampireBleed extends Ability {
    public VampireBleed() {
        super("Vampire", "bleed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVampireAttack(EntityDamageByEntityEvent event) {
        if (commonDamageCheckup(event)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (!MathUtils.hasChance((int) getScaling(info, "chance", event))) return;

        ParticleDisplay gain = ParticleDisplay.colored(player.getLocation(), 0, 255, 0, 1).withCount(30).offset(.5);
        ParticleDisplay bleed = ParticleDisplay.colored(player.getLocation(), 255, 0, 0, 1).withCount(30).offset(.5);
        LivingEntity entity = (LivingEntity) event.getEntity();

        new BukkitRunnable() {
            final double damageHeal = getScaling(info, "damage-heal", event);
            int duration = (int) getScaling(info, "duration", event);

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }
                gain.spawn(player.getLocation());
                bleed.spawn(entity.getLocation());

                entity.damage(damageHeal);
                VersionSupport.heal(player, damageHeal);
                if (duration-- <= 0) cancel();
            }
        }.runTaskTimer(SkillsPro.get(), 0, 10L);
    }
}
