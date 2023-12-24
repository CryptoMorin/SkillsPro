package org.skills.abilities.vergil;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.skills.abilities.AbilityContext;
import org.skills.abilities.InstantActiveAbility;
import org.skills.main.SkillsPro;
import org.skills.managers.DamageManager;
import org.skills.managers.MoveManager;
import org.skills.utils.Cooldown;
import org.skills.utils.EntityUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class VergilRapidSlash extends InstantActiveAbility {
    private static final String SLASH_MODE = "VERGIL_SLASH_MODE";
    private static final Map<UUID, List<LivingEntity>> SLASHED = new HashMap<>();

    public VergilRapidSlash() {
        super("Vergil", "rapid_slash");
        setPvPBased(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        onAttack(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        // Don't ignore cancelled, we don't care about that.
        if (event.getDamager() instanceof Player) onAttack((Player) event.getDamager());
    }

    private static void onAttack(Player player) {
        if (MoveManager.isMoving(player)) return;
        if (!Cooldown.isInCooldown(player.getUniqueId(), SLASH_MODE)) return;

        double range = 3;
        Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(2));
        ParticleDisplay.of(Particle.SWEEP_ATTACK).withCount(30).offset(1.5).spawn(loc);
        XSound.ENTITY_PLAYER_ATTACK_SWEEP.play(loc);
        List<LivingEntity> entities = SLASHED.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        for (Entity nearby : player.getWorld().getNearbyEntities(loc, range, range, range)) {
            if (EntityUtil.filterEntity(player, nearby)) continue;
            LivingEntity living = (LivingEntity) nearby;
            DamageManager.damage(living, null, 0.5);
            entities.add(living);
            DamageManager.storeDamageTicks(living);
            living.setNoDamageTicks(1);
        }
    }

    @Override
    public void useSkill(AbilityContext context) {
        Player player = context.getPlayer();
        new Cooldown(player.getUniqueId(), SLASH_MODE, 10, TimeUnit.SECONDS);
        Bukkit.getScheduler().runTaskLater(SkillsPro.get(), () -> {
            SLASHED.remove(player.getUniqueId()).forEach(DamageManager::restoreDamageTicks);
            ;
        }, 20 * 10);
    }
}
