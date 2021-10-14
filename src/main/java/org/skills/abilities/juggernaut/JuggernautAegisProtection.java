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
import org.skills.main.locale.SkillsLang;
import org.skills.managers.DamageManager;
import org.skills.utils.Cooldown;
import org.skills.utils.MathUtils;
import org.skills.utils.versionsupport.VersionSupport;

import java.util.concurrent.TimeUnit;

public class JuggernautAegisProtection extends Ability {
    private static final String AEGIS = "JUGGERNAUT_AEGIS";

    public JuggernautAegisProtection() {
        super("Juggernaut", "aegis_protection");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onJuggernautDefend(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getDamager() instanceof EnderPearl) return;

        Player player = (Player) event.getEntity();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;
        if (Cooldown.isInCooldown(player.getUniqueId(), AEGIS)) return;

        long cooldown = (long) this.getScaling(info, "cooldown");
        new Cooldown(player.getUniqueId(), AEGIS, cooldown, TimeUnit.SECONDS);
        event.setCancelled(true);
        int lvl = info.getAbilityLevel(this);

        player.getLocation().getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getLocation(), 30, 1, 1, 1, 0.3);
        XSound.ENTITY_ITEM_BREAK.play(player);
        if (lvl > 1) {
            Entity entity = event.getDamager();
            if (entity instanceof Projectile) {
                entity = (Entity) ((Projectile) entity).getShooter();
                if (entity == null) entity = event.getDamager();
            }
            XSound.ENTITY_GENERIC_EXPLODE.play(player);
            ParticleDisplay display = ParticleDisplay.simple(entity.getLocation(), Particle.FLAME).offset(1);

            if (entity instanceof LivingEntity) {
                double dmg = MathUtils.percentOfAmount(this.getScaling(info, "reflect-damage-percent", event), event.getDamage());
                DamageManager.damage((LivingEntity) entity, player, dmg);
            }
        }
        if (lvl > 2 && VersionSupport.getHealthPercent(player) < getScaling(info, "knockback.health", event)) {
            Entity damager = event.getDamager();
            Vector dir = damager.getLocation().toVector().subtract(player.getLocation().toVector());
            damager.setVelocity(dir.multiply(getScaling(info, "knockback.velocity", event)));
        }
        SkillsLang.Skill_Juggernaut_Aegis_Success.sendMessage(player);
    }
}
