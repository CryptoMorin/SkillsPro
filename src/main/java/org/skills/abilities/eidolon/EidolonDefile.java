package org.skills.abilities.eidolon;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.Particles;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EidolonDefile extends ActiveAbility {
    private static final Map<UUID, UUID> IMBALANCED = new HashMap<>();

    public EidolonDefile() {
        super("Eidolon", "defile");
    }

    private static void visuals(Entity entity) {
        Location loc = entity.getLocation();
        World world = entity.getWorld();
        world.playEffect(loc, Effect.STEP_SOUND, Material.LAPIS_BLOCK);
        world.playEffect(loc, Effect.STEP_SOUND, Material.COAL_BLOCK);
        world.playEffect(loc, Effect.STEP_SOUND, Material.IRON_BLOCK);
        Particles.atom(4, 2, 30, ParticleDisplay.of(XParticle.DRIPPING_LAVA).withLocation(loc), ParticleDisplay.of(XParticle.FLAME).withLocation(loc));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttackOrDefend(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;
        Entity victim = event.getEntity();

        if (victim instanceof Player) {
            UUID defender = IMBALANCED.get(victim.getUniqueId());
            if (defender != null && event.getDamager().getUniqueId().equals(defender)) {
                Player player = (Player) victim;
                SkilledPlayer info = this.checkup(player);
                if (info != null) {
                    double shield = this.getScaling(info, "shield");
                    event.setDamage(event.getDamage() - shield);
                }
            }
        }
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.checkup(player);
        if (info == null) return;

        UUID target = IMBALANCED.get(player.getUniqueId());

        if (target != null && victim.getUniqueId().equals(target)) {
            double damage = getScaling(info, "damage", event);
            event.setDamage(event.getDamage() + damage);
            return;
        }

        int time = (int) getScaling(info, "time");

        IMBALANCED.put(player.getUniqueId(), victim.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                IMBALANCED.remove(player.getUniqueId());
            }
        }.runTaskLater(SkillsPro.get(), time * 20L);
        visuals(victim);
    }
}
