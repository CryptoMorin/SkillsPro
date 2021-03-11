package org.skills.abilities.firemage;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.SkillsPro;
import org.skills.utils.MathUtils;

public class FireMageAbsorbEnergy extends Ability {
    public FireMageAbsorbEnergy() {
        super("FireMage", "absorb_energy");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireMageAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        int chance = (int) this.getScaling(info);
        int lvl = info.getImprovementLevel(this);
        if (MathUtils.hasChance(chance)) {
            Entity entity = event.getEntity();
            entity.setFireTicks((int) (entity.getFireTicks() + (getExtraScaling(info, "fire", event) * 20)));

            if (lvl == 1) {
                player.spawnParticle(Particle.FLAME, entity.getLocation(), chance / 2, 0.5, 0.5, 0.5, 0.2);
            } else {
                XSound.ITEM_FIRECHARGE_USE.play(entity);
                if (lvl == 2) {
                    player.spawnParticle(Particle.FLAME, entity.getLocation(), (chance / 2) + 10, 0.3, 0.3, 0.3, 0.3);
                } else {
                    XParticle.helix(SkillsPro.get(), 3, 0.7, 0.1, 1, 5, 1, false, false, ParticleDisplay.simple(entity.getLocation(), Particle.FLAME).withCount(2));
                }
            }
        }
    }
}
