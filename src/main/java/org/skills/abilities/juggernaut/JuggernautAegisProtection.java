package org.skills.abilities.juggernaut;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.main.locale.SkillsLang;
import org.skills.managers.LastHitManager;
import org.skills.utils.Cooldown;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.concurrent.TimeUnit;

public class JuggernautAegisProtection extends Ability {
    private static final String AEGIS = "JUGGERNAUT_AEGIS";

    public JuggernautAegisProtection() {
        super("Juggernaut", "aegis_protection");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onJuggernautDefend(EntityDamageByEntityEvent event) {
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;
        if (event.getEntity() instanceof Player && !(event.getDamager() instanceof EnderPearl)) {
            Player p = (Player) event.getEntity();
            SkilledPlayer info = this.checkup(p);
            if (info == null) return;

            if (!Cooldown.isInCooldown(p.getUniqueId(), AEGIS)) {
                long time = (long) this.getScaling(info, "damage", event.getDamage());
                new Cooldown(p.getUniqueId(), AEGIS, time, TimeUnit.SECONDS);
                event.setCancelled(true);
                int lvl = info.getImprovementLevel(this);

                p.getLocation().getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation(), 30, 1, 1, 1, 0.3);
                XSound.ENTITY_ITEM_BREAK.play(p);
                if (lvl > 1) {
                    Entity entity = event.getDamager();
                    if (entity instanceof Projectile) {
                        entity = (Entity) ((Projectile) entity).getShooter();
                        if (entity == null) entity = event.getDamager();
                    }
                    XSound.ENTITY_GENERIC_EXPLODE.play(p);
                    ParticleDisplay display = ParticleDisplay.simple(entity.getLocation(), Particle.FLAME);
                    display.offset(1, 1, 1);

                    if (entity instanceof LivingEntity) {
                        double dmg = this.getExtraScaling(info, "damage", event);
                        LastHitManager.damage((LivingEntity) entity, p, dmg);
                    }
                }
                if (lvl > 2 && VersionSupport.getHealthPercent(p) < getExtraScaling(info, "knockback.health", event)) {
                    Entity damager = event.getDamager();
                    Vector dir = damager.getLocation().toVector().subtract(p.getLocation().toVector());
                    damager.setVelocity(dir.multiply(getExtraScaling(info, "knockback.velocity", event)));
                }
                SkillsLang.Skill_Juggernaut_Aegis_Success.sendMessage(p);
            }
        }
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%damage%", getScalingDescription(info, getExtra(info, "damage").getString())};
    }
}
