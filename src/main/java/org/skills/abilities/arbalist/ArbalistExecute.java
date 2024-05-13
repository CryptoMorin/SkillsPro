package org.skills.abilities.arbalist;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.skills.abilities.Ability;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArbalistExecute extends Ability {
    private static final Map<UUID, UUID> EXECUTOR = new HashMap<>();
    private static final Map<UUID, Integer> EXECUTES = new HashMap<>();

    public ArbalistExecute() {
        super("Arbalist", "execute");
    }

    @EventHandler
    public void onMiss(ProjectileHitEvent event) {
        if (event.getHitEntity() != null) return;
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player) arrow.getShooter();

        UUID target = EXECUTOR.remove(player.getUniqueId());
        if (target != null) EXECUTES.remove(target);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity)) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) arrow.getShooter();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        UUID oldTarget = EXECUTOR.get(player.getUniqueId());
        if (!target.getUniqueId().equals(oldTarget)) {
            EXECUTOR.put(player.getUniqueId(), target.getUniqueId());
            EXECUTES.put(oldTarget, 1);
            return;
        }

        int executer = EXECUTES.getOrDefault(oldTarget, 0);
        EXECUTES.put(oldTarget, executer + 1);

        int lvl = info.getAbilityLevel(this);
        int chance = (int) this.getScaling(info, "chance");
        int multiplier = executer * 2;

        if (MathUtils.hasChance(chance + multiplier)) {
            applyEffects(info, (LivingEntity) target);
        }
        if (lvl > 1 && MathUtils.hasChance(chance - 10 + multiplier)) {
            target.setFireTicks(target.getFireTicks() + 5 * 20);
        }
        if (lvl > 2 && MathUtils.hasChance(chance - 15 + multiplier)) {
            target.getWorld().strikeLightning(target.getLocation());
        }

        if (XMaterial.supports(13)) {
            player.playNote(player.getLocation(), Instrument.CHIME, Note.natural(0, Note.Tone.values()
                    [(Math.min(executer, Note.Tone.values().length - 1))]));
        }
        ParticleDisplay.of(XParticle.CRIT).withLocation(target.getLocation()).withCount(10).spawn();
    }
}