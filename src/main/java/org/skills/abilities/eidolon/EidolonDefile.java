package org.skills.abilities.eidolon;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.skills.abilities.ActiveAbility;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsConfig;
import org.skills.utils.Cooldown;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EidolonDefile extends ActiveAbility {
    private static final String DEFILE = "DEFILE";
    private static final HashMap<Integer, Double> IMBALANCED = new HashMap<>();

    static {
        addDisposableHandler(IMBALANCED);
    }

    public EidolonDefile() {
        super("Eidolon", "defile", false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEidolonAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;
        if (SkillsConfig.isInDisabledWorld(event.getEntity().getLocation())) return;

        Player player = (Player) event.getDamager();
        SkilledPlayer info = this.activeCheckup(player);
        if (info == null) return;

        Entity entity = event.getEntity();
        double amt = this.getScaling(info);
        IMBALANCED.put(entity.getEntityId(), amt);
        int time = (int) getExtraScaling(info, "time");
        if (time <= 0) time = 2;

        new Cooldown(event.getEntity().getUniqueId(), DEFILE, time, TimeUnit.SECONDS);
        event.setDamage(event.getDamage() * getExtraScaling(info, "damage", "imbalance", amt));

        Location loc = entity.getLocation();
        World world = entity.getWorld();
        world.playEffect(loc, Effect.STEP_SOUND, Material.LAPIS_BLOCK);
        world.playEffect(loc, Effect.STEP_SOUND, Material.COAL_BLOCK);
        world.playEffect(loc, Effect.STEP_SOUND, Material.IRON_BLOCK);
        XParticle.atom(4, 2, 30, ParticleDisplay.simple(loc, Particle.DRIP_LAVA), ParticleDisplay.simple(loc, Particle.FLAME));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageReceive(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        if (SkillsConfig.isInDisabledWorld(world)) return;
        boolean inCd = Cooldown.isInCooldown(entity.getUniqueId(), DEFILE);
        Double amt = inCd ? IMBALANCED.get(entity.getEntityId()) : IMBALANCED.remove(entity.getEntityId());
        if (amt == null) return;

        event.setDamage(event.getDamage() * (1 + (amt / 100f)));

        Location location = entity.getLocation();
        world.playEffect(location, Effect.STEP_SOUND, Material.LAPIS_BLOCK);
        world.playEffect(location, Effect.STEP_SOUND, Material.COAL_BLOCK);
        world.playEffect(location, Effect.STEP_SOUND, Material.IRON_BLOCK);
    }

    @Override
    public Object[] applyEdits(SkilledPlayer info) {
        return new Object[]{"%time%", getScalingDescription(info, getExtra(info, "time").getString())};
    }
}
